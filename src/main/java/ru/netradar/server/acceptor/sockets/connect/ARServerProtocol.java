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
import ru.netradar.server.acceptor.ARProtocol;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRLocation;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Raev
 */
public class ARServerProtocol {
    private final static Logger LOG = Logger.getLogger(ARServerProtocol.class);
    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    private static final long SATTIME_MAXDEVIATION = 60000L * 60 * 24 * 30 * 6; //6 месяцев

    ARLocThread servThread;
    private final DeviceStorage deviceStorage;

    public ARServerProtocol(ARLocThread servThread, WebMonitorProperties settings, DeviceStorage deviceStorage) {
        this.servThread = servThread;
        this.deviceStorage = deviceStorage;
        userCheckUrl = settings.getUsercheckurl();
    }

    String userCheckUrl;
    byte status = ARProtocol.STATUS_WAITAUTHORIZATION;
    public static int noFixMesCount;
    long lastDBSave;
    //static Random r = new Random();
    static String sep_comma = ",";
    static String SQL_insert = "insert into ARTALLOG (DEV_ID,SN,PIN,LAT,LON,ALT,CRS,SPD) VALUES(";
    static String sep_parcl = ")";
    static String S_1 = "1";
    static String S_2 = "2";
    static String S_3 = "3";
    static byte[] bOK = Util.stringLatinToByteArray("OK");
    static byte[] bER = Util.stringLatinToByteArray("ER");
    static String SQL_insert_report = "insert into REPLOG (DEV_ID,SN,PIN,MSG) VALUES(";
    static String emptyString = "";

    void processCommand(byte cmd, int size, DataInputStream data, DataOutputStream out) throws Exception {
        String sc = emptyString;
        if ((cmd > 0) && (cmd <= ARProtocol.COMMANDS_NAMES.length)) {
            sc = ARProtocol.COMMANDS_NAMES[cmd - 1];
        }

        if (servThread.dNR == null) {
            LOG.info("artal sent command " + sc + " " + cmd + ":" + size);
        } else {
            LOG.info("artal SN:" + servThread.dNR.sn + "(ID:" + servThread.dNR.userId + ") sent command " + sc + " " + cmd + ":" + size);
        }

        float ep = 0;
        try {

            switch (status) {
                case ARProtocol.STATUS_WAITAUTHORIZATION:
                    if (cmd != ARProtocol.COMMAND_AUTHORIZE) {
                        throw new Exception("Wrong command - wait for Authorization ");
                    }
                    byte[] b1 = new byte[8];
                    data.read(b1);
                    // LOG.debug("artal sent sn " + b1);
                    ep = 2;
                    String sn = Util.byteArrayUTFToString(b1);
                    ep = 3;
                    b1 = new byte[4];
                    data.read(b1);
                    ep = 4;
                    // LOG.debug("artal sent bl " + b1);
                    String pin = Util.byteArrayUTFToString(b1);
                    ep = 5;
                    Integer.parseInt(sn);
                    ep = 5.1f;
                    Integer.parseInt(pin);
                    ep = 5.2f;
                    servThread.setName("AR " + sn);
                    LOG.info("artal sent sn=" + sn + " pin=" + pin + " and now is authorizing...");

                    //long __startTime = System.currentTimeMillis();
                    //String res = Util.getHTTPContentAsString(userCheckUrl + "?sn=" + sn + "&pin=" + pin + "&ut=2");
                    String res = deviceStorage.getSiteClient().executeURI(userCheckUrl + "?sn=" + sn + "&pin=" + pin + "&ut=2");

                    //__startTime = (System.currentTimeMillis() - __startTime);
                    ep = 6;
                    //String res = "$AU,93,MagView";
                    String[] uinfo = Util.parseString(res, ',');
                    ep = 7;
                    int uid = Integer.valueOf(uinfo[0]);
                    ep = 8;

                    if (uid == 0) {
                        out.write(bER);
                        out.flush();
                        throw new Exception(sn + ':' + pin + " not authorized");
                    }
                    ep = 9;
                    out.write(bOK);
                    out.flush();
                    ep = 10;
                    String un = uinfo[1];
                    LOG.debug(un + "(sn: " + sn + ") artal authorized");
                    ep = 11;
                    servThread.remoteAddr = sn;
                    servThread.dNR = deviceStorage.connectNRLocation(uid, NRObject.ARTALUSERTYPE, servThread, null);
                    ep = 12;
                    servThread.dNR.sn = Integer.valueOf(sn);
                    servThread.dNR.pin = Short.valueOf(pin);
                    servThread.dNR.name = un;
                    status = ARProtocol.STATUS_WAITCOORDINATES;
                    break;

                case ARProtocol.STATUS_WAITCOORDINATES:

                    if (cmd == ARProtocol.COMMAND_AUTHORIZE) {
                        return;
                    }

                    if (cmd == ARProtocol.COMMAND_PING) {
                        servThread.dNR.lastActivityINFO = System.currentTimeMillis();
                        out.write(bOK);
                        out.flush();
                        return;
                    }
                    ep = 13;
                    NRDevice user = servThread.dNR;

                    if (cmd == ARProtocol.COMMAND_REPORT) {
                        if (size > 5000) {
                            throw new Exception("Too big message COMMAND_REPORT >5000 bytes");
                        }
                        if (size > 250) {
                            LOG.error("Too big message >250 bytes:" + cmd);
                        }

                        byte[] sbytes = new byte[size];
                        data.readFully(sbytes);
                        String sb = Util.getDateTimeStringInMsk() + " " + Util.byteArrayToString(sbytes, false).trim();
                        sb = sb.replace('\'', ' ');
                        LOG.debug("artal sent msg " + sb);
                        boolean replaced = true;
                        for (int i = 0; i < 32; i++) {
                            replaced = replaced | (sb.indexOf((char) i) >= 0);
                            sb = sb.replace((char) i, ' ');
                        }
                        if (replaced) {
                            LOG.info("artal msg transformed " + sb);
                        }

                        servThread.dNR.lastActivityINFO = System.currentTimeMillis();
                        servThread.getDeviceStorage().notifyInfo(user, sb, servThread.dNR.lastActivityINFO);
                        user.setInfo(sb);

                        out.write(bOK);
                        out.flush();

                        return;
                    }

                    if (cmd == ARProtocol.COMMAND_REPORT_WITH_DATE) {
                        if (size > 5000) {
                            throw new Exception("Too big message COMMAND_REPORT_WITH_DATE >5000 bytes");
                        }
                        if (size > 250) {
                            LOG.error("Too big message >250 bytes:" + cmd);
                        }

                        byte[] sbytes = new byte[size - 8];
                        long adt = System.currentTimeMillis();
                        adt = readTimeFromStream(data, adt);
                        data.readFully(sbytes);
                        //String sb = Util.getDateTimeStringInMsk()+" "+Util.byteArrayUTFToString(sbytes).trim();
                        String sb = Util.getDateTimeStringInMsk() + " " + Util.byteArrayToString(sbytes, false).trim();
                        sb = sb.replace('\'', ' ');
                        LOG.debug("artal sent msg " + sb);
                        boolean replaced = true;
                        for (int i = 0; i < 32; i++) {
                            replaced = replaced | (sb.indexOf((char) i) >= 0);
                            sb = sb.replace((char) i, ' ');
                        }
                        if (replaced) {
                            LOG.info("artal msg transformed " + sb);
                        }

                        servThread.dNR.lastActivityINFO = System.currentTimeMillis();
                        servThread.getDeviceStorage().notifyInfo(user, sb, adt);
                        user.setInfo(sb);

                        out.write(bOK);
                        out.flush();

                        return;
                    }


                    ep = 13.99f;

                    if (cmd != ARProtocol.COMMAND_COORDINATES && cmd != ARProtocol.COMMAND_DATE_COORDINATES) {
                        LOG.warn("Unknown command come: " + cmd + " with size " + size);
                        return;
                    }
                    long adt = System.currentTimeMillis();

                    if (cmd == ARProtocol.COMMAND_DATE_COORDINATES) {
                        LOG.info("data/time at begin");
                        //mS1-mS2-Sec-Min-Hour-Day-Month-Year
                        adt = readTimeFromStream(data, adt);
                        LOG.info("Hex packet: " + MD5.toHexString(Arrays.copyOf(servThread.dataBuffer, 3 + size)));
                    }

                    ep = 14;

                    int i1,
                            i2,
                            i3,
                            i4;

                    ep = 15;
                    i1 = data.readByte();
                    if (i1 < 0) {
                        i1 += 256;
                    }
                    //  LogWriteThread.printlnlogOK("artal sent i1 "+i1);
                    i2 = data.readByte();
                    if (i2 < 0) {
                        i2 += 256;
                    }
                    // LogWriteThread.printlnlogOK("artal sent i2 "+i2);
                    i3 = data.readByte();
                    if (i3 < 0) {
                        i3 += 256;
                    }
                    //   LogWriteThread.printlnlogOK("artal sent i3 "+i3);
                    i4 = data.readByte();
                    if (i4 < 0) {
                        i4 += 256;
                    }
                    //    LogWriteThread.printlnlogOK("artal sent i4 "+i4);
                    ep = 16;
//
//     H:=(byte(XS[1])+byte(XS[2])*256)-$8000;
//     L:=(byte(XS[3])+byte(XS[4])*256)-$8000;
//     Xhl:=(H*10000+L)/0.6;
//     Xhl:=round(Xhl);
//     Result:=FloatToStr(Xhl/1000000);

                    ep = 17;
                    int H = i1 + i2 * 256 - 0x8000;
                    //       LogWriteThread.printlnlogOK("artal sent H "+H);
                    int L = i3 + i4 * 256 - 0x8000;
                    //       LogWriteThread.printlnlogOK("artal sent L "+L);
                    int C = (int) (((double) (H * 10000 + L)) / 0.6);
                    double lon = (double) C / 1000000.0d;
                    //       LogWriteThread.printlnlogOK("artal sent lonm "+lonm);
                    ep = 18;

                    i1 = data.readByte();
                    if (i1 < 0) {
                        i1 += 256;
                    }
                    //     LogWriteThread.printlnlogOK("artal sent i1 "+i1);
                    i2 = data.readByte();
                    if (i2 < 0) {
                        i2 += 256;
                    }
                    //      LogWriteThread.printlnlogOK("artal sent i2 "+i2);
                    i3 = data.readByte();
                    if (i3 < 0) {
                        i3 += 256;
                    }
                    //     LogWriteThread.printlnlogOK("artal sent i3 "+i3);
                    i4 = data.readByte();
                    if (i4 < 0) {
                        i4 += 256;
                    }
                    //     LogWriteThread.printlnlogOK("artal sent i4 "+i4);
                    H = i1 + i2 * 256 - 0x8000;
                    //     LogWriteThread.printlnlogOK("artal sent H "+H);
                    L = i3 + i4 * 256 - 0x8000;
                    //     LogWriteThread.printlnlogOK("artal sent L "+L);
                    C = (int) (((double) (H * 10000 + L)) / 0.6);
                    double lat = (double) C / 1000000.0d;
                    //     LogWriteThread.printlnlogOK("artal sent latm "+latm);
                    ep = 19;

                    i1 = data.readByte();
                    if (i1 < 0) {
                        i1 += 256;
                    }
                    //     LogWriteThread.printlnlogOK("artal sent i1 "+i1);
                    i2 = data.readByte();
                    if (i2 < 0) {
                        i2 += 256;
                    }
                    //     LogWriteThread.printlnlogOK("artal sent i2 "+i2);
                    ep = 20;

                    int spd = i1 + i2 * 256 - 0x8000;
                    //    LogWriteThread.printlnlogOK("artal sent spd "+spd);

                    i1 = data.readByte();
                    if (i1 < 0) {
                        i1 += 256;
                    }
                    //    LogWriteThread.printlnlogOK("artal sent i1 "+i1);
                    i2 = data.readByte();
                    if (i2 < 0) {
                        i2 += 256;
                    }
                    //    LogWriteThread.printlnlogOK("artal sent i2 "+i2);
                    ep = 21;

                    int crs = i1 + i2 * 256 - 0x8000;
                    //     LogWriteThread.printlnlogOK("artal sent crs "+crs);
                    short alt = 0;

                    if (cmd == ARProtocol.COMMAND_DATE_COORDINATES) {
                        i1 = data.readByte();
                        if (i1 < 0) {
                            i1 += 256;
                        }
                        i2 = data.readByte();
                        if (i2 < 0) {
                            i2 += 256;
                        }
                        ep = 21;

                        alt = (short) (i1 + i2 * 256 - 0x8000);
                        LOG.info("artal sent alt " + alt);

                    }

                    if (cmd == ARProtocol.COMMAND_COORDINATES && size > 14) {
                        LOG.info("data/time come");
                        //mS1-mS2-Sec-Min-Hour-Day-Month-Year
                        adt = readTimeFromStream(data, adt);
                        LOG.info("Hex packet: " + MD5.toHexString(Arrays.copyOf(servThread.dataBuffer, 3 + size)));
                    }


                    if ((lat < -90) || (lat > 90) || (lon < -180) || (lon > 180) || (spd < -1) || (spd > 2000) || (crs > 800) || (crs < -400)) {
                        //       LogWriteThread.printlnlogOK("artal coords incorrect. Ignored.");
                        out.write(bOK);
                        out.flush();
                    }

                    ep = 22;

                    //errpos = 19;
                    int alat = (int) (lat * 100000);
                    //errpos = 20;
                    int alon = (int) (lon * 100000);
                    //errpos = 21;
                    short aspd = (short) (spd * 10);

                    short acrs = (short) crs;
                    ep = 23;

                    user.loc = new NRLocation(alat, alon, alt, aspd, acrs, adt);

                    //more then 
                    if (lastDBSave < System.currentTimeMillis() - 1000) {
                        deviceStorage.notifyPosition(user, user.loc);
                        lastDBSave = System.currentTimeMillis();
                    }
                    ep = 24;
                    out.write(bOK);
                    out.flush();
                    servThread.dNR.lastActivityPOS = System.currentTimeMillis();
                    //NRStorage.addChangedNRLocation(loc);

                    break;
            }
        } catch (Throwable tt) {

            throw new Exception("EP:" + ep + " : " + tt.toString(), tt);
        }
    }

    private static long readTimeFromStream(DataInputStream data, long currentTime) throws IOException {
        int ch1 = data.read();
        int ch2 = data.read();
        if ((ch1 | ch2) < 0) {
            throw new EOFException("ms not found");
        }
        int ms = ((int) ((ch2 << 8) + (ch1 << 0))) * 1000 / 1024;
        int sec = data.read();
        int min = data.read();
        int hour = data.read();
        int day = data.read();
        int month = data.read();
        int year = data.read() + 2000;
        Calendar cal = Calendar.getInstance(UTC);
        cal.setTimeInMillis(currentTime);
        cal.set(Calendar.MILLISECOND, ms);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        long messageTime = cal.getTimeInMillis();
        boolean useMessageTime = Math.abs(messageTime - currentTime) < SATTIME_MAXDEVIATION;
        LOG.info("date from message: " + new Date(messageTime) + ", curTime: " + new Date(currentTime) + ", diff:" + (Math.abs(messageTime - currentTime)) + ", use: " + useMessageTime);
        return (useMessageTime) ? messageTime : currentTime;
    }
}
