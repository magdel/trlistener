package ru.netradar.server.device.notify;

/**
 * Получатель уведомления
 * <p/>
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 24.03.2010
 * Time: 17:39:46
 * To change this template use File | Settings | File Templates.
 */
public class Recipient {
    private final int userId;
    private final String deviceName;
    private final String recipientAddress;
    private final long deviceTimeout;
    private final long remindInterval;
    private final NotifyType notifyType;

    private long lastSMSSentTime;

    public Recipient(int userId, String deviceName, String recipientAddress, long deviceTimeout, long remindInterval, NotifyType notifyType) {
        this.userId = userId;
        this.deviceName=deviceName;
        this.recipientAddress = recipientAddress;
        this.deviceTimeout = deviceTimeout;
        this.remindInterval = remindInterval;
        this.notifyType = notifyType;
    }

    public int getUserId() {
        return userId;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public long getDeviceTimeout() {
        return deviceTimeout;
    }

    public long getRemindInterval() {
        return remindInterval;
    }

    public NotifyType getNotifyType() {
        return notifyType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getLastSMSSentTime() {
        return lastSMSSentTime;
    }

    public void setLastSMSSentTime(long lastSMSSentTime) {
        this.lastSMSSentTime = lastSMSSentTime;
    }

    @Override
    public String toString() {
        return "Recipient{" +
                "userId=" + userId +
                ", deviceName='" + deviceName + '\'' +
                ", recipientAddress='" + recipientAddress + '\'' +
                ", deviceTimeout=" + deviceTimeout +
                ", remindInterval=" + remindInterval +
                ", notifyType=" + notifyType +
                ", lastSMSSentTime=" + lastSMSSentTime +
                '}';
    }
}
