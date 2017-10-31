package ru.netradar.server.notify.info;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.DeleteMonitorProperties;
import ru.netradar.config.properties.InfoQueueProperties;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.http.SiteClient;
import ru.netradar.server.queue.TaskExecutor;
import ru.netradar.server.queue.dao.TaskDAO;
import ru.netradar.server.storage.InfoNotifier;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;
import ru.netradar.utils.IdGenerator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;

/**
 * Доставка положения
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:05:07
 * To change this template use File | Settings | File Templates.
 */
public class InfoTaskExecutor implements TaskExecutor<InfoTask>, InfoNotifier {
    private static final Logger LOG = Logger.getLogger(InfoTaskExecutor.class);
    private final String updateURI;
    private final String md5Key;
    private final IdGenerator idGenerator;
    private static final String OK = "OK";
    private static final long MINUTE = 60000;
    private static final long DAY = 24 * 60 * MINUTE;
    private static final long WEEK = 7 * DAY;
    private final TransactionTemplate txTemplate;
    private final TaskDAO<InfoTask> infoTaskTaskDAO;
    private final SiteClient siteClient;

    public InfoTaskExecutor(InfoQueueProperties settings, DeleteMonitorProperties delSettings,
                            TransactionTemplate txTemplate, TaskDAO<InfoTask> positionTaskTaskDAO,
                            SiteClient siteClient,
                            IdGenerator idGenerator) {
        this.txTemplate = txTemplate;
        this.infoTaskTaskDAO = positionTaskTaskDAO;
        this.siteClient = siteClient;
        this.updateURI = settings.getUri();
        this.md5Key = delSettings.getRspass();
        this.idGenerator = idGenerator;
    }

    @Override
    public boolean processTask(InfoTask task) {
        if (task.getExpiryDt().getTime() < System.currentTimeMillis()) {
            LOG.warn("Task expired " + task);
            return true;
        }

        // NameValuePair[] nvp = new NameValuePair[2];
        /*String s2sign = ES + task.getDevId() + S + task.getUt() + S + task.getLat() + S + task.getLon() + S +
                task.getAlt() + S + task.getSpd() + S + task.getCrs() + S + task.getDttm().getTime();
        nvp[0] = new BasicNameValuePair("ul", s2sign);
        final String sign = MD5.getHashString(s2sign + md5Key);
        nvp[1] = new BasicNameValuePair("sign", sign);
        *///GetMethod method = new GetMethod(updateURI);
        final long time = task.getDttm().getTime();
        String s2sign = task.getInfo() + task.getDevId() + "" + task.getUt() + "" + time;
        final String sign = MD5.getHashString(s2sign + md5Key);


        String param;
        try {
            param = "?info=" + URLEncoder.encode(task.getInfo(), "UTF-8") +
                    "&sign=" + Util.urlEncodeString(sign) +
                    "&dev_id=" + task.getDevId() +
                    "&ut=" + task.getUt() +
                    "&dt=" + time;
        } catch (UnsupportedEncodingException e) {
            LOG.error("Err:" + task.getInfo(), e);
            throw new IllegalArgumentException(e);
        }

        String response;
        try {
            response = siteClient.executeURI(updateURI + param);
        } catch (IOException e) {
            return false;
        }
        final boolean b = response.startsWith(OK);
        if (!b) {
            LOG.warn("Request not success: " + param);
        }
        return b;
    }


    @Override
    public void notifyInfo(NRDevice device, String info, long time) {
        final InfoTask task = new InfoTask(
                idGenerator.generate(),
                new Timestamp(System.currentTimeMillis()), 0,
                new Timestamp(System.currentTimeMillis() + WEEK),
                device.userId, device.userType, info,
                new Timestamp(time));

        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                infoTaskTaskDAO.storeTask(task);
            }
        });
    }

}