package ru.netradar.server.notify.position;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.DeleteMonitorProperties;
import ru.netradar.config.properties.PositionQueueProperties;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRLocation;
import ru.netradar.server.http.SiteClient;
import ru.netradar.server.queue.TaskExecutor;
import ru.netradar.server.queue.dao.TaskDAO;
import ru.netradar.server.storage.LocationNotifier;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;
import ru.netradar.utils.IdGenerator;

import java.io.IOException;
import java.sql.Timestamp;

/**
 * Доставка положения
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:05:07
 * To change this template use File | Settings | File Templates.
 */
public class PositionTaskExecutor implements TaskExecutor<PositionTask>, LocationNotifier {
    private static final Logger LOG = LoggerFactory.getLogger(PositionTaskExecutor.class);
    private final String posUpdateURI;
    private final String md5Key;
    private final IdGenerator idGenerator;
    private static final String S = ":";
    private static final String ES = "";
    private static final String OK = "OK";
    private static final long MINUTE = 60000;
    private static final long DAY = 24 * 60 * MINUTE;
    private static final long WEEK = 7 * DAY;
    private final TransactionTemplate txTemplate;
    private final TaskDAO<PositionTask> positionTaskTaskDAO;
    private final SiteClient siteClient;

    public PositionTaskExecutor(PositionQueueProperties positionQueueProperties, DeleteMonitorProperties delSettings,
                                TransactionTemplate txTemplate, TaskDAO<PositionTask> positionTaskTaskDAO,
                                SiteClient siteClient,
                                IdGenerator idGenerator) {
        this.txTemplate = txTemplate;
        this.positionTaskTaskDAO = positionTaskTaskDAO;
        this.siteClient = siteClient;
        this.posUpdateURI = positionQueueProperties.getUri();
        this.md5Key = delSettings.getRspass();
        this.idGenerator = idGenerator;
        LOG.info("Pos queue inited: uri={}", posUpdateURI);
    }

    @Override
    public boolean processTask(PositionTask task) {
        if (task.getExpiryDt().getTime() < System.currentTimeMillis()) {
            LOG.warn("Task expired " + task);
            return true;
        }

        //NameValuePair[] nvp = new NameValuePair[2];
        String s2sign = ES + task.getDevId() + S + task.getUt() + S + task.getLat() + S + task.getLon() + S +
                task.getAlt() + S + task.getSpd() + S + task.getCrs() + S + task.getDttm().getTime();
        //nvp[0] = new BasicNameValuePair("ul", s2sign);
        final String sign = MD5.getHashString(s2sign + md5Key);
        //nvp[1] = new BasicNameValuePair("sign", sign);
        //GetMethod method = new GetMethod(posUpdateURI);
        String param = "?ul=" + Util.urlEncodeString(s2sign) + "&sign=" + Util.urlEncodeString(sign);

        String response;
        try {
            response = siteClient.executeURI(posUpdateURI + param);
        } catch (IOException e) {
            return false;
        }

        return response.startsWith(OK);
    }


    @Override
    public void notifyLocation(NRDevice device, NRLocation location) {
        final PositionTask task = new PositionTask(idGenerator.generate(),
                new Timestamp(System.currentTimeMillis()), 0,
                new Timestamp(System.currentTimeMillis() + WEEK),
                device.userId, device.userType,
                location.lat(), location.lon(), location.alt(), location.rspd(), location.crs(),
                new Timestamp(location.dt()));
        positionTaskTaskDAO.storeTask(task);
    }
}
