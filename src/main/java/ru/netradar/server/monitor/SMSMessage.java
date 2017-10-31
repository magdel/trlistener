/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.netradar.server.monitor;

/**
 * @author rfk
 */
public class SMSMessage {

    String mesBody;
    String phoneNumber;
    int dev_id;
    byte userType;
    private long created_time = System.currentTimeMillis();
    private int smsStatus = 0;

    public SMSMessage(String mesBody, String phoneNumber, int dev_id, byte userType) {
        this.mesBody = mesBody;
        this.phoneNumber = phoneNumber;
        this.dev_id = dev_id;
        this.userType = userType;
    }

    public void setSMSStatus(int smsStatus) {
        this.smsStatus = smsStatus;
    }

    public String getInsertSQL() {
        return "INSERT INTO SMS (DEV_ID,UT, SMS_TEXT,DT) VALUES(" +
                dev_id + "," + userType + ",'" + mesBody + "'," + created_time + ")";
    }

    public String getUpdateStatusSQL() {
        return "UPDATE SMS SET SMS_STATUS_ID = " + smsStatus + " where DT=" + created_time;
    }
}
