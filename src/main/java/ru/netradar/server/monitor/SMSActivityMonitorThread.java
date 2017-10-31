/*
 * AbstractMonitorThread.java
 *
 * Created on 28 18:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.monitor;

import org.apache.log4j.Logger;
import ru.netradar.server.NRServer;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.notify.NotifyInfo;
import ru.netradar.server.device.notify.Recipient;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.server.storage.NotificationNotifier;
import ru.netradar.util.Util;

/**
 * @author Raev
 */
public class SMSActivityMonitorThread extends AbstractMonitorThread {
    private final static Logger LOG = Logger.getLogger(SMSActivityMonitorThread.class);

    //public static SMSActivityMonitorThread amt;
    private long delayBetweenSMS = 2 * 60 * 60 * 1000;//20 min
    private NotificationNotifier notifier;


    public SMSActivityMonitorThread(NotificationNotifier notifier,
                                    DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry) {
        super(30000, "SMSMON", deviceStorage, diagThreadRegistry);
        this.notifier = notifier;
    }

    private void saveSMS(NRDevice loc, Recipient r) {
        //сохраняем в лок переменную, т.к. инфа может обновится
        final NotifyInfo notifyInfo = loc.getNotifyInfo();
        if (notifyInfo == null)
            return;
        String mes = r.getDeviceName() + " без связи " + getSMSTimeDelay(System.currentTimeMillis() - loc.lastActivityNET) + "! " + Util.getDateTimeString();
        notifier.createNotification(loc, r, mes);
    }

    private String dn = "SMS сервис";

    @Override
    public String getDiagName() {
        return dn;
    }

    public static String getSMSTimeDelay(long time) {
        return "" + (time / 60000) + " мин " +
                (((time - (time / 60000) * 60000)) / 1000) + " сек";
    }

    @Override
    public void monitorBegin() {

    }

    @Override
    public void monitorBefore() {

    }

    @Override
    public void monitorDevice(NRDevice nrd) {

        final NotifyInfo notifyInfo = nrd.getNotifyInfo();
        if ((notifyInfo != null) && (nrd.lastActivityNET > 0)) {
            for (Recipient r : notifyInfo.getRecipients().values()) {
                //проверяем что было восстановление связи (либо смс еще вообще не была отправлена)
                if ((r.getLastSMSSentTime() < nrd.lastActivityNET) ||
                        //либо прошел установленный период уведомления
                        ((r.getLastSMSSentTime() + r.getRemindInterval() < System.currentTimeMillis()) && (r.getRemindInterval() > 0)))
                    //и если устройство не на связи заданное время, то оповещаем
                    if (nrd.lastActivityNET + r.getDeviceTimeout() < System.currentTimeMillis()) {
                        //---------------------now let's create task for SMS send------
                        LOG.info("Detected downlink for " + nrd);
                        //---write file with SMS text inside
                        r.setLastSMSSentTime(System.currentTimeMillis());
                        saveSMS(nrd, r);
                        try {
                            Thread.sleep(10);
                        } catch (Throwable t) {
                            throw new IllegalStateException(t);
                        }

                    }
            }
        }
    }

    @Override
    public void monitorAfter() {
        diagStatus.updateStatusOK();
    }

    @Override
    public void monitorEnd() {
        if (NRServer.server.serverRunning) {
            diagStatus.updateStatusBAD("SMS Monitor ended!!");
        }

    }
}
