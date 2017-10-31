package ru.netradar.server.notify.notification;

import ru.netradar.server.device.notify.NotifyType;
import ru.netradar.server.queue.dao.Task;

import java.sql.Timestamp;

/**
 * Очередь уведомлений
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:06:23
 * To change this template use File | Settings | File Templates.
 */
public class NotificationTask extends Task {
    private final int devId;
    private final int ut;
    private final String info;
    private final Timestamp dttm;
    private final String recipientAddress;
    private final NotifyType notifyType;
    private final int userId;

    public NotificationTask(Long id, Timestamp nextSendDt, int tryN, Timestamp expiryDt,
                            int devId, int ut, String info, Timestamp dttm,
                            String recipientAddress, NotifyType notifyType, int userId
    ) {
        super(id, nextSendDt, tryN, expiryDt);
        this.devId = devId;
        this.ut = ut;
        this.info = info;
        this.dttm = dttm;
        this.recipientAddress = recipientAddress;
        this.notifyType = notifyType;
        this.userId = userId;
    }

    public int getDevId() {
        return devId;
    }

    public int getUt() {
        return ut;
    }

    public String getInfo() {
        return info;
    }

    public Timestamp getDttm() {
        return dttm;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public NotifyType getNotifyType() {
        return notifyType;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "devId=" + devId +
                ", ut=" + ut +
                ", info='" + info + '\'' +
                ", dttm=" + dttm +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", notifyType=" + notifyType +
                ", userId=" + userId +
                "} " + super.toString();
    }
}