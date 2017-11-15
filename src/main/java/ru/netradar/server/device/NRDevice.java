/*
 * NRLocation.java
 *
 * Created on 19 14:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.device;

import org.apache.log4j.Logger;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.device.notify.NotifyInfo;
import ru.netradar.server.device.notify.Recipient;

/**
 * Location
 *
 * @author Raev
 */
public class NRDevice extends NRObject {
    private final static Logger LOG = Logger.getLogger(NRDevice.class);

    public NRLocation loc = new NRLocation();
    public long imei;

    private NotifyInfo notifyInfo;

    /* check last activity time */
    public boolean checkActivity;

    /* time of last message from device */
    public long lastActivityNET = System.currentTimeMillis();
    /* phone number to message from device */
    public String phone = "";
    /**
     * Thread working for the location object
     */
    private Thread locThread;
    private long connectTime;
    private long lastStoredPosTime;

    public synchronized void setConnectedLocThread(Thread lt) {
        if (locThread != lt) {
            connectedTimes++;
            if (locThread != null) {
                locThread.interrupt();
                LOG.info("" + userId + '-' + userType + ':' + " second connect. First alarmed to close (" +
                        ((System.currentTimeMillis() - connectTime) / 1000) + " sec worked)");

            }
            locThread = lt;
            connectTime = System.currentTimeMillis();
        }
    }

    public synchronized void setDisconnectedLocThread(Thread lt) {
        disconnectedTimes++;
        if ((locThread == lt) && (locThread != null)) {
            locThread = null;
            LOG.info("" + userId + '-' + userType + ':' + " disconnected itself (" +
                    ((System.currentTimeMillis() - connectTime) / 1000) + " sec worked)");
        } else {
            if ((locThread != null) && (lt == null)) {
                try {
                    locThread.interrupt();
                } catch (Throwable t) {
                    LOG.warn(t);
                }
                locThread = null;
                LOG.info("" + userId + '-' + userType + ':' + " disconnected from previous thread");
            } else {
                LOG.info("" + userId + '-' + userType + ':' + " cleared from previous thread");
            }
        }
    }

    public Thread getLocThread() {
        return locThread;
    }

    public long getConnectTime() {
        return connectTime;
    }

    /**
     * Sound sent from mapnav
     */
    public byte[] sound;
    /**
     * Sound format sent from mapnav
     */
    public String soundFormat;
    /**
     * Sound time mark from mapnav
     */
    public long soundDT;
    /**
     * Field sts from DB
     */
    public String sts = es;
    /**
     * additional info by ARTAL device
     */
    private String info = es;

    public String getInfo() {
        return info;
    }

    /**
     * time of any last DB activity
     */
    public long lastActivityPOS;
    /**
     * time of last DB status activity
     */
    public long lastActivityINFO;
    private static String es = "";
    public int connectedTimes;
    public int disconnectedTimes;

    public void setInfo(String info) {
        this.info = info;
    }

    private String sentInfo = es;

    public void setSentInfo(String sentInfo) {
        this.sentInfo = sentInfo;
    }

    public String needSendInfo() {
        return (sentInfo.equals(info)) ? es : info;
    }

    /**
     * Creates a new instance of NRLocation
     *
     * @param id       id пользователя
     * @param userType тип
     */
    public NRDevice(int id, byte userType) {
        super(id, userType);

        if ((id == 212) && (userType == NRObject.ARTALUSERTYPE)) {
            phone = "+79119159380";
            checkActivity = true;
        }

//    if ((id==1)&&(userType==NRObject.TR102USERTYPE)){
//      //phone="+79112237893";
//      phone="+79119159380";
//      checkActivity=true;
//    }
//    if ((id==2)&&(userType==NRObject.TR102USERTYPE)){
//      phone="+79119159380";
//      checkActivity=true;
//    }
//    if ((id==71)&&(userType==NRObject.ARTALUSERTYPE)){
//      phone="+79112237893";
//      checkActivity=true;
//    }
    }


    public NotifyInfo getNotifyInfo() {
        return notifyInfo;
    }

    public void setNotifyInfo(NotifyInfo notifyInfo) {
        if (notifyInfo == null) {
            this.notifyInfo = null;
            LOG.info("NotifyInfo cleared for " + super.toString());
            return;
        }

        final NotifyInfo locNI = this.notifyInfo;
        if (locNI == null)
            this.notifyInfo = notifyInfo;
        else {
            for (Recipient r : locNI.getRecipients().values()) {
                notifyInfo.getRecipients().get(r.getUserId()).setLastSMSSentTime(r.getLastSMSSentTime());
            }
            this.notifyInfo = notifyInfo;
        }
        LOG.info("NotifyInfo updated: " + this);
    }

    public long getLastStoredPosTime() {
        return lastStoredPosTime;
    }

    public void setLastStoredPosTime(long lastStoredPosTime) {
        this.lastStoredPosTime = lastStoredPosTime;
    }

    @Override
    public String toString() {
        return "NRDevice{" +
                "loc=" + loc +
                ", imei=" + imei +
                ", info='" + info + '\'' +
                ", connectedTimes=" + connectedTimes +
                ", disconnectedTimes=" + disconnectedTimes +
                ", sentInfo='" + sentInfo + '\'' +
                ", notifyInfo=" + notifyInfo +
                '}' + '>' + super.toString();
    }
}
