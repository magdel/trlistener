/*
 * TRServerProtocol.java
 *
 * Created on 19 18:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.netradar.server.acceptor.sockets.connect;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.TRProtocol;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.Util;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Raev
 */
public class TRServerProtocol {
    private final static Logger LOG = Logger.getLogger(TRServerProtocol.class);

    TRLocThread servThread;
    private final DeviceStorage deviceStorage;
    private static final String UTC = "UTC";


    public TRServerProtocol(TRLocThread servThread, WebMonitorProperties settings, DeviceStorage deviceStorage) {
        this.servThread = servThread;
        this.deviceStorage = deviceStorage;
        this.userCheckUrl = settings.getUsercheckurl();
    }

    String userCheckUrl;
    private byte status = TRProtocol.STATUS_WAITCOORDINATES;
    public static int noFixMesCount;
    /**
     * Set default to current time to avoid store first message
     */
    private long lastDBSave = System.currentTimeMillis();
    //static Random r = new Random();

    static String sep_comma = ",";
    static String S_1 = "1";
    static String S_2 = "2";
    static String S_3 = "3";

    void processCommand(byte[] data, int datasize) throws Exception {
        String rq = "";
        float errpos = 0;
        try {
            switch (status) {
                case TRProtocol.STATUS_WAITCOORDINATES:
                    String s = Util.byteArrayToString(data, 0, datasize, true);
                    //$355632000166323,1,1,040202,093633,E12129.2252,N2459.8891,00161,0.0100,147,07*37!
                    rq = s;
                    errpos = 1;
                    String[] info = Util.parseString(sep_comma + s, ',');
                    errpos = 2;

                    String imei = info[0].substring(1);
                    errpos = 3;
                    if (s.length() < 500) {
                        LOG.info(s);
                    } else {
                        LOG.info("Strange big message: " + s.substring(0, 490) + "...");
                    }
                    errpos = 3.1f;


                    NRDevice user;

                    if (servThread.dNR == null) {

                        servThread.setName("TR " + imei);

                        errpos = 4;
                        LOG.info("IMEI:" + imei + " is authorizing...");
                        //String res = Util.getHTTPContentAsString(userCheckUrl + "?imei=" + imei + "&ut=3");
                        String res = deviceStorage.getSiteClient().executeURI(userCheckUrl + "?imei=" + imei + "&ut=3");

                        LOG.info("Resp: " + res);
                        //res = "$AU,"+(new Random()).nextInt()+","+(new Random()).nextInt();

                        errpos = 5;
                        rq = rq + ":" + res + ":";
                        String[] uinfo = Util.parseString(res, ',');
                        errpos = 6;
                        rq = rq + ":" + uinfo[0];
                        if (uinfo[0].equals("")) {
                            servThread.interrupt();
                            LOG.info("IMEI:" + imei + " is not registered. Disconnected");
                            return;
                        }

                        int uid = Integer.valueOf(uinfo[0]);
                        servThread.remoteAddr = imei;

                        errpos = 10;
                        user = deviceStorage.connectNRLocation(uid, NRObject.TR102USERTYPE, servThread, null);

                        errpos = 10.1f;
                        LOG.info("IMEI:" + imei + " connect confirmed");

                        errpos = 11;
                        user.name = uinfo[1];
                        errpos = 12;
                        user.imei = Long.parseLong(imei);
                        errpos = 13;
                        servThread.dNR = user;
                        servThread.remoteAddr = imei;

                        //final NotifyInfo notifyInfo = new NotifyInfo();
                        //notifyInfo.addRecipient(new Recipient(2, "Tracker", "79119159380", 60000, 900000, NotifyType.NOTIFY_BY_SMS));
                        //notifyInfo.addRecipient(new Recipient(1, "Tracker", "79119159380", 60000, 900000, NotifyType.NOTIFY_BY_SMS));
                        //user.setNotifyInfo(notifyInfo);

                    } else {
                        errpos = 13.1f;

                        if (servThread.dNR.imei != Long.parseLong(imei)) {
                            throw new Exception("Changed IMEI");
                        }
                        errpos = 13.2f;
                        user = servThread.dNR;
                    }

                    errpos = 14;
                    if (info[1].equals(S_2) || info[1].equals(S_3) || info[2].equals(S_1)) {
                        noFixMesCount++;
                        return;
                    }


                    s = info[5];
                    errpos = 15;
                    double LON = Integer.parseInt(s.substring(1, 4)) + Double.parseDouble(s.substring(4, 11)) / 60.0;
                    errpos = 1;
                    if (s.charAt(0) == 'W') {
                        LON = -LON;
                    }

                    errpos = 16;
                    s = info[6];
                    errpos = 17;
                    double LAT = Integer.parseInt(s.substring(1, 3)) + Double.parseDouble(s.substring(3, 10)) / 60.0;
                    errpos = 18;
                    if (s.charAt(0) == 'S') {
                        LAT = -LAT;
                    }

                    errpos = 19;
                    int lat = (int) (LAT * 100000);
                    errpos = 20;
                    int lon = (int) (LON * 100000);
                    errpos = 21;
                    short alt = (short) Double.parseDouble(info[7]);
                    errpos = 22;
                    short crs = (short) Double.parseDouble(info[9]);
                    errpos = 23;
                    short spd = (short) (Double.parseDouble(info[8]) * 18.52);
                    errpos = 24;

                    //long dt = System.currentTimeMillis();
                    long dt = decodeGPSTime(info[3], info[4]);

                    //LOG.info("Authorized at " + (new Date()) + " with GPS date " + new Date(dt) + " delay " + (System.currentTimeMillis() - dt) + "ms");

                    user.loc = new NRLocation(lat, lon, alt, spd, crs, dt);

                    if (lastDBSave < System.currentTimeMillis() - 3000) {
                        deviceStorage.notifyPosition(user, user.loc);
                        lastDBSave = System.currentTimeMillis();
                    }

                    user.lastActivityPOS = System.currentTimeMillis();

                    //NRStorage.addChangedNRLocation(loc);

                    break;
            }
        } catch (Exception e) {
            throw new Exception("TRProtocol:" + rq + ":" + errpos + ":" + e.toString());
        }
    }

    /**
     * decode datetime like
     * 040202093633
     * ddmmyyhhnnss
     * 0123456789
     * 10
     * 11
     *
     * @param gpsDate строка дата GPS
     * @param gpsTime строка время GPS
     * @return дату как long
     */
    public static long decodeGPSTime(String gpsDate, String gpsTime) {
        Calendar calUTC = Calendar.getInstance(TimeZone.getTimeZone(UTC));
        long millis = System.currentTimeMillis();
        calUTC.setTimeInMillis(millis - (millis % 1000));

        int valPos = 4;
        calUTC.set(Calendar.YEAR, 2000 + (gpsDate.charAt(valPos) - '0') * 10 + (gpsDate.charAt(valPos + 1) - '0'));
        valPos = 2;
        calUTC.set(Calendar.MONTH, (gpsDate.charAt(valPos) - '0') * 10 + (gpsDate.charAt(valPos + 1) - '0') - 1);
        valPos = 0;
        calUTC.set(Calendar.DAY_OF_MONTH, (gpsDate.charAt(valPos) - '0') * 10 + (gpsDate.charAt(valPos + 1) - '0'));

        valPos = 0;
        calUTC.set(Calendar.HOUR_OF_DAY, (gpsTime.charAt(valPos) - '0') * 10 + (gpsTime.charAt(valPos + 1) - '0'));
        valPos = 2;
        calUTC.set(Calendar.MINUTE, (gpsTime.charAt(valPos) - '0') * 10 + (gpsTime.charAt(valPos + 1) - '0'));
        valPos = 4;
        calUTC.set(Calendar.SECOND, (gpsTime.charAt(valPos) - '0') * 10 + (gpsTime.charAt(valPos + 1) - '0'));

        return calUTC.getTimeInMillis();
    }


}
