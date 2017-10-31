/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.netradar.server.device;

/**
 * @author rfk
 */
public class NRLocation {

    int _lat; //mul by 100000;
    int _lon; //mul by 100000;
    short _alt;
    short _spd;  //multiplyed by 10
    short _crs;
    long _dt;

    public NRLocation(int lat, int lon, short alt, short spd, short crs, long dt) {
        _lat = lat; //mul by 100000;
        _lon = lon; //mul by 100000;
        _alt = alt;
        _spd = spd;  //multiplyed by 10
        _crs = crs;
        _dt = dt;
    }

    public NRLocation() {
    }

    public NRLocation clone() {
        return new NRLocation(_lat, _lon, _alt, _spd, _crs, _dt);
    }

    /**
     * mul by 100000;
     */
    public int latm() {
        return _lat;
    }

    /**
     * mul by 100000;
     */
    public int lonm() {
        return _lon;
    }

    /**
     * real lat
     */
    public float lat() {
        return (float) _lat / 100000;
    }

    /**
     * real lon
     */
    public float lon() {
        return (float) _lon / 100000;
    }

    public short alt() {
        return _alt;
    }

    /**
     * mul by 10;
     */
    public short spd() {
        return _spd;
    }

    /**
     * real spd
     */
    public float rspd() {
        return (float)((double) _spd / 10.0);
    }

    public short crs() {
        return _crs;
    }

    public long dt() {
        return _dt;
    }

    @Override
    public String toString() {
        return "NRLocation{" +
                " lat=" + lat() +
                ", lon=" + lon() +
                ", _alt=" + _alt +
                ", _spd=" + _spd +
                ", _crs=" + _crs +
                ", _dt=" + _dt +
                '}';
    }
}
