package ru.netradar.server.monitor;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.DeleteMonitorProperties;
import ru.netradar.config.properties.RecipientMonitorProperties;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.notify.NotifyInfo;
import ru.netradar.server.device.notify.NotifyType;
import ru.netradar.server.device.notify.Recipient;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;

/**
 * Сканирует на наличие получателей уведомлений по устройствам
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 06.04.2010
 * Time: 19:21:35
 * To change this template use File | Settings | File Templates.
 */
public class RecipientsMonitorThread extends AbstractMonitorThread {
    private final static Logger LOG = Logger.getLogger(RecipientsMonitorThread.class);

    private final String uri;
    private final String md5Key;


    public RecipientsMonitorThread(DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry,
                                   RecipientMonitorProperties recipientMonitorSettings,
                                   DeleteMonitorProperties delSettings) {
        super(300000, "RECIPIENT", deviceStorage, diagThreadRegistry);
        this.uri = recipientMonitorSettings.getUri();
        this.md5Key = delSettings.getRspass();
    }

    @Override
    public void monitorBegin() {

    }

    @Override
    public void monitorBefore() throws Exception {

    }

    @Override
    public void monitorDevice(NRDevice nrd) throws Exception {
        String s2sign = "" + nrd.userId + "" + nrd.userType + "" + md5Key;
        String sign = MD5.getHashString(s2sign);
        final String request = uri + "?dev_id=" + nrd.userId + "&ut=" + nrd.userType + "&sign=" + Util.urlEncodeString(sign);
        final String list = deviceStorage.getSiteClient().executeURI(request);
        if (list.startsWith("SIGN")) {
            LOG.error("Sign wrong for request " + request);
            return;
        }
        if (!list.isEmpty()) {
            NotifyInfo ni = new NotifyInfo();

            String[] recipients = Util.parseString('\n' + list, '\n');
            for (String rec : recipients) {
                if ((rec == null) || (!rec.startsWith("$NL")))
                    continue;
                String[] params = Util.parseString(rec, ',');
                ni.addRecipient(new Recipient(Integer.parseInt(params[1]), params[2], params[3], Long.parseLong(params[4]), Long.parseLong(params[5]), NotifyType.NOTIFY_BY_SMS));
            }
            if (ni.getRecipients().size() > 0) {
                nrd.setNotifyInfo(ni);
            } else {
                nrd.setNotifyInfo(null);
            }
        } else {
            nrd.setNotifyInfo(null);
        }
    }

    @Override
    public void monitorAfter() {

    }

    @Override
    public void monitorEnd() {

    }

    @Override
    public String getDiagName() {
        return "Получатели";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
