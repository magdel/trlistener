package ru.netradar.server.notify.sms;

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
public class SMSTask extends Task {
    private final String info;
    private final String recipientAddress;
    private final long nId;

    public SMSTask(Long id, Timestamp nextSendDt, int tryN, Timestamp expiryDt,
                            String info, String recipientAddress, long nId) {
        super(id, nextSendDt, tryN, expiryDt);
        this.info = info;
        this.recipientAddress=recipientAddress;
        this.nId=nId;
    }

    public String getInfo() {
        return info;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public long getNId() {
        return nId;
    }

    @Override
    public String toString() {
        return "SMSTask{" +
                "info='" + info + '\'' +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", nId=" + nId +
                "} " + super.toString();
    }
}