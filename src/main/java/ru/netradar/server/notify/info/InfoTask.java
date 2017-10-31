package ru.netradar.server.notify.info;

import ru.netradar.server.queue.dao.Task;

import java.sql.Timestamp;

/**
 * Очередь положения
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 20.03.2010
 * Time: 11:06:23
 * To change this template use File | Settings | File Templates.
 */
public class InfoTask extends Task {
    private final int devId;
    private final int ut;
    private final String info;
    private final Timestamp dttm;


    public InfoTask(Long id, Timestamp nextSendDt, int tryN, Timestamp expiryDt,
                        int devId, int ut, String info, Timestamp dttm
    ) {
        super(id, nextSendDt, tryN, expiryDt);
        this.devId = devId;
        this.ut = ut;
        this.info = info;
        this.dttm = dttm;
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

    @Override
    public String toString() {
        return "InfoTask{" +
                "devId=" + devId +
                ", ut=" + ut +
                ", info='" + info + '\'' +
                ", dttm=" + dttm +
                "} " + super.toString();
    }
}