/*
 * MNServerProtocol.java
 *
 * Created on 5 , 22:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.netradar.server.acceptor.sockets.connect;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.MNProtocol;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;

/**
 * @author RFK
 */
public class MNServerProtocol {
    private final static Logger LOG = Logger.getLogger(MNServerProtocol.class);

    public static String emptyString = "";
    MNLocThread servThread;
    private final DeviceStorage deviceStorage;

    /**
     * Creates a new instance of MNServerProtocol
     */
    public MNServerProtocol(MNLocThread servThread, WebMonitorProperties settings, DeviceStorage deviceStorage) {
        this.servThread = servThread;
        this.deviceStorage = deviceStorage;
        userCheckUrl = settings.getUsercheckurl();
        userListUrl = settings.getListuserurl();
    }

    String userCheckUrl;
    String userListUrl;

    byte status = MNProtocol.STATUS_WAITVERSION;
    long lastDBSave;

    void processVersion(DataInputStream sc) throws Exception {
        byte cmd = sc.readByte();
        byte size = sc.readByte();
        byte version = sc.readByte();
        if (version != MNProtocol.COMMAND_VERSION_VALUE)
            throw new Exception("Version wrong: " + version + " instead of " + MNProtocol.COMMAND_VERSION_VALUE);
    }

    void processCommand(byte cmd, int size, DataInputStream dis, DataOutputStream out) throws Exception {
        if (cmd == MNProtocol.COMMAND_PING) {
            dis.readByte();
            //LOG.info("Ping");
            return;
        }

        switch (status) {
            case MNProtocol.STATUS_WAITVERSION:
                if (cmd != MNProtocol.COMMAND_VERSION)
                    throw new Exception("Wrong command - wait for Version ");
                byte version = dis.readByte();
                out.writeByte(MNProtocol.COMMAND_VERSIONANS);
                out.writeInt(MNProtocol.COMMAND_VERSIONANS_SIZE);
                if (version != MNProtocol.COMMAND_VERSION_VALUE) {
                    out.writeByte(MNProtocol.COMMAND_VERSIONANS_BAD);
                    out.flush();
                    throw new Exception("Version wrong: " + version + " instead of " + MNProtocol.COMMAND_VERSION_VALUE);
                }
                out.writeByte(MNProtocol.COMMAND_VERSIONANS_OK);
                out.flush();
                status = MNProtocol.STATUS_WAITAUTHORIZATION;
                break;
            case MNProtocol.STATUS_WAITAUTHORIZATION:
                if (cmd != MNProtocol.COMMAND_AUTHORIZE)
                    throw new Exception("Wrong command - wait for Authorization ");
                String lg = dis.readUTF();
                String ps = dis.readUTF();
                if (lg.length() > 200)
                    throw new IllegalArgumentException("big login");
                servThread.setName("MN " + lg);

                //long __startTime = System.currentTimeMillis();
                //String res = Util.getHTTPContentAsString(userCheckUrl + "?lg=" + Util.urlEncodeString(lg) + "&ps=" + Util.urlEncodeString(ps));
                String res = deviceStorage.getSiteClient().executeURI(userCheckUrl + "?lg=" + Util.urlEncodeString(lg) + "&ps=" + Util.urlEncodeString(ps));
                //LOG.info("Authorize query time = " + ((System.currentTimeMillis() - __startTime)) + " ms");

                //String res = "$AU,93,MagView";
                String[] uinfo = Util.parseString(res, ',');
                int uid = Integer.valueOf(uinfo[0]);

                out.writeByte(MNProtocol.COMMAND_AUTHORIZEANS);
                out.writeInt(MNProtocol.COMMAND_AUTHORIZEANS_SIZE);

                if (uid == 0) {
                    out.writeByte(MNProtocol.COMMAND_AUTHORIZEANS_BAD);
                    out.flush();
                    //System.out.println(lg+':'+" not authorized. "+dtt);
                    throw new Exception(lg + ':' + " not authorized (res=" + res + ")");
                }
                out.writeByte(MNProtocol.COMMAND_AUTHORIZEANS_OK);
                out.flush();
                String un = uinfo[1];
                LOG.info(un + ':' + " authorized");
                final String hash = MD5.getHashString(Util.stringToByteArray(ps, true));

                servThread.remoteAddr = un;
                servThread.dNR = deviceStorage.connectNRLocation(uid, NRObject.MAPNAVUSERTYPE, servThread, hash);
                if (servThread.dNR.name == null)
                    servThread.dNR.name = un;
                //servThread.dNR.passwordMD5Hash = hash;
                LOG.debug("Acquired " + servThread.dNR);
                //http://netradar.ru/cgi-bin/uv.pl?loginame=magdel&logipass=mymap&dt=0
                String sSigned = lg + "0" + MD5.getHashString(ps);
                String sign = MD5.getHashString(sSigned);

                //__startTime = System.currentTimeMillis();
                //res = Util.getHTTPContentAsString(userListUrl + "?lg=" + lg + "&sign=" + sign + "&dt=0&v=1");
                res = deviceStorage.getSiteClient().executeURI(userListUrl + "?lg=" + lg + "&sign=" + sign + "&dt=0&v=1");
                //LOG.info("Registration info query time = " + ((System.currentTimeMillis() - __startTime)) + " ms\nInfo: " + res);
                String[] list = Util.parseString(res, '\n');
                int uc = 0;
                for (int i = 0; i < list.length; i++)
                    if (list[i] != null) {
                        uinfo = Util.parseString("," + list[i], ',');
                        if (uinfo[0].equals("$NR")) uc++;
                    }

                nrobjs = new NRObject[uc];
                nrlocs = new NRDevice[uc];
                uservisidDT = new long[uc];
                usersndDT = new long[uc];
                uc = 0;
                NRObject nr;
                for (int i = 0; i < list.length; i++)
                    if (list[i] != null) {
                        uinfo = Util.parseString("," + list[i], ',');
                        if (uinfo[0].equals("$NR")) {
                            if (uinfo.length > 9)
                                nr = new NRObject(Integer.parseInt(uinfo[8]), Byte.parseByte(uinfo[10]));
                            else nr = new NRObject(Integer.parseInt(uinfo[8]), NRObject.MAPNAVUSERTYPE);
                            nr.name = uinfo[1];
                            nrobjs[uc] = nr;
                            uc++;
                        }
                    }
                String sMon = "Monitors objects (" + nrobjs.length + "): ";
                for (NRObject nrM : nrobjs)
                    sMon += " " + nrM + "\n";
                LOG.info(sMon);
                status = MNProtocol.STATUS_WAITUSERS;

                break;

            case MNProtocol.STATUS_WAITUSERS:
                if (cmd != MNProtocol.COMMAND_USERLIST)
                    throw new Exception("Wrong command - wait for User list ");
                uc = dis.readShort();
                if (uc > 1000) throw new Exception("WR3a!");

                //uservisidL = new Integer[uc];
                //for (int i=0;i<uservisid.length;i++) {
                for (int i = 0; i < uc; i++) {
                    dis.readInt();
                    //NRObject nr = new NRObject(dis.readInt(),NRObject.MAPNAVUSERTYPE);
                    //uservisid[i]=nr;

                    //   uservisid[i]=dis.readInt();
                    //   uservisidL[i]= new Integer(uservisid[i]);
                }


                out.writeByte(MNProtocol.COMMAND_USERLISTANS);
                out.writeInt(MNProtocol.COMMAND_USERLISTANS_SIZE);

                out.writeByte(MNProtocol.COMMAND_USERLISTANS_OK);
                out.flush();
                status = MNProtocol.STATUS_WAITCOORDINATES;
                servThread.nextSendTime = System.currentTimeMillis() + 1000;
                break;

            case MNProtocol.STATUS_WAITCOORDINATES:
                if (cmd == MNProtocol.COMMAND_MYCOORDS) {
                    //NRUser u = servThread.loct;
//          errP=0.14;
                    int lat = dis.readInt();
//          errP=0.15;
                    int lon = dis.readInt();
//          errP=0.16;
                    short alt = dis.readShort();
//          errP=0.17;
                    short spd = dis.readShort();
//          errP=0.18;
                    short crs = dis.readShort();
//          errP=0.19;
                    long dt = dis.readLong();
                    if (Math.abs(dt - System.currentTimeMillis()) > 24 * 3600 * 1000L) {
                        dt = System.currentTimeMillis();
                    }
                    servThread.dNR.loc = new NRLocation(lat, lon, alt, spd, crs, dt);

//more then three second gone
                    if (lastDBSave < System.currentTimeMillis() - 3000) {
                        deviceStorage.notifyPosition(servThread.dNR, servThread.dNR.loc);
                        lastDBSave = System.currentTimeMillis();
                    }

                    //LOG.info("Coords come");
                    break;
                }

                if (cmd == MNProtocol.COMMAND_SOUND) {
                    NRDevice u = servThread.dNR;
                    int sndsize = dis.readInt();
                    byte[] snd = new byte[sndsize];
                    dis.read(snd);
                    u.sound = snd;
                    u.soundFormat = dis.readUTF();
                    u.soundDT = dis.readLong();

                    LOG.info("Sound read from:" + u.name + ':' + servThread.remoteAddr);

                    break;
                }


                throw new Exception("Wrong command - wait for My Coords: " + cmd);

        }

    }

    //int[] uservisid;
    //Integer[] uservisidL;
    NRObject[] nrobjs;
    NRDevice[] nrlocs;
    long[] uservisidDT;
    long[] usersndDT;

    private String getQuestion() {
        return "WHO?";
    }

    private String checkAnswer(String inputLine) {
        return "GOOD.\n";
    }
}
