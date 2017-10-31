package ru.netradar.server.notify.position;

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
public class PositionTask extends Task {
    private final int devId;
    private final int ut;
    private final float lat;
    private final float lon;
    private final int alt;
    private final float spd;
    private final int crs;
    private final Timestamp dttm;


    public PositionTask(Long id, Timestamp nextSendDt, int tryN, Timestamp expiryDt,
                        int devId, int ut, float lat, float lon, int alt,
                        float spd, int crs, Timestamp dttm
    ) {
        super(id, nextSendDt, tryN, expiryDt);
        this.devId = devId;
        this.ut = ut;
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.spd = spd;
        this.crs = crs;
        this.dttm = dttm;
    }

    public int getDevId() {
        return devId;
    }

    public int getUt() {
        return ut;
    }

    public float getLat() {
        return lat;
    }

    public float getLon() {
        return lon;
    }

    public int getAlt() {
        return alt;
    }

    public float getSpd() {
        return spd;
    }

    public int getCrs() {
        return crs;
    }

    public Timestamp getDttm() {
        return dttm;
    }

    @Override
    public String toString() {
        return "PositionTask{" +
                "devId=" + devId +
                ", ut=" + ut +
                ", lat=" + lat +
                ", lon=" + lon +
                ", alt=" + alt +
                ", spd=" + spd +
                ", crs=" + crs +
                ", dttm=" + dttm +
                "} " + super.toString();
    }
}
