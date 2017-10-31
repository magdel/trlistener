/*
 * KKMultiServerThread.java
 *
 * Created on 5 , 20:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor.sockets.connect;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.MNProtocol;
import ru.netradar.server.acceptor.sockets.LocThread;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRLocation;
import ru.netradar.server.storage.DeviceStorage;

import java.io.*;
import java.net.Socket;
import java.util.Date;

public class MNLocThread extends LocThread {

    private final static Logger LOG = Logger.getLogger(MNLocThread.class);
    private final WebMonitorProperties webSettings;

    public MNLocThread(Socket socket, WebMonitorProperties webSettings, DeviceStorage deviceStorage) {
        super(socket, deviceStorage);
        this.webSettings = webSettings;
        setName("MNLocThread");
    }

    MNServerProtocol sp;
    private byte inBuffer[] = new byte[10000];
    private byte dataBuffer[] = new byte[50000];
    private int dataBufferEnd;
    public static volatile int conCount;
    public static volatile int tryCount;
    private float ep;

    protected void processData() {
        ep = 0;
        try {
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = socket.getInputStream();
            ep = 1;

            conCount++;
            tryCount++;
            remoteAddr = "" + conCount;

            LOG.info("Connected some MN.  Total " + MNLocThread.conCount + " users");
            ep = 2;

//      BufferedReader in = new BufferedReader(
//          new InputStreamReader(
//          socket.getInputStream()));

            String inputLine, outputLine;
            sp = new MNServerProtocol(this, webSettings, getDeviceStorage());

            ep = 3;
            //System.out.println(remoteAddr +':'+outputLine);

            //out.flush();
            bais = new ByteArrayInputStream(dataBuffer);
            dis = new DataInputStream(bais);
            // dissc = new DataInputStream(in);
            ep = 4;
            try {
                try {
                    while ((!isInterrupted()) && waitFor()) ;
                } finally {
                    ep = 5;
                    sp = null;
                    if (dNR != null)
                        getDeviceStorage().disconnectNRLocation(dNR, this);
                }
            } finally {
                ep = 6;
                out.close();
                in.close();
                ep = 7;
                socket.close();
            }
        } catch (Throwable e) {
            LOG.error("EXR MN:" + remoteAddr + ':' + ep + ':' + e.toString(), e);
        }
        conCount--;
        String s = remoteAddr + ": disconnected (" + (System.currentTimeMillis() - connected) / 1000 + " sec). Left " + conCount + " users";
        LOG.info(s);

    }

    ByteArrayInputStream bais;
    DataInputStream dis;
    //DataInputStream dissc;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
    DataOutputStream dos = new DataOutputStream(baos);
    private byte minReadBytes = MNProtocol.COMMAND_HEADERSIZE;
    long nextSendTime;
    long lastReadTime = System.currentTimeMillis();
    float ep1;

    private boolean waitFor() {
        int intRead = 0;
        boolean flag;
        ep1 = 0;
        try {
            if (in.available() >= minReadBytes) {
                if ((intRead = in.read(inBuffer)) > 0) {
                    ep1 = 1;

                    //���������� � �����
                    for (int i = 0; i < intRead; i++) {
                        dataBuffer[dataBufferEnd + i] = inBuffer[i];
                    }
                    //��������� �����
                    ep1 = 2;
                    dataBufferEnd += intRead;
                    //String outputLine;
                    boolean again;
                    do {
                        again = false;
                        if (dataBufferEnd > MNProtocol.COMMAND_HEADERSIZE) {
                            ep1 = 3;
                            bais.reset();
                            dis.reset();
                            byte cmd = dis.readByte();
                            int cmdsize = dis.readInt();
                            ep1 = 4;
                            if (dataBufferEnd - MNProtocol.COMMAND_HEADERSIZE >= cmdsize) {
                                //LOG.info("Processing "+cmd+" from "+dNR);
                                sp.processCommand(cmd, cmdsize, dis, out);
                                ep1 = 5;
                                int i = cmdsize + MNProtocol.COMMAND_HEADERSIZE;
                                ep1 = 6;
                                for (int j = i; j < dataBufferEnd; j++) {
                                    dataBuffer[j - i] = dataBuffer[j];
                                }
                                ep1 = 7;
                                dataBufferEnd -= i;
                                again = true;
                            } else {
                                minReadBytes = 1;
                            }
                            lastReadTime = System.currentTimeMillis() + 30000;
                        }

                    } while (again);

                }
            }

            if (sp.status == MNProtocol.STATUS_WAITCOORDINATES) {

                if (lastReadTime > System.currentTimeMillis()) {
                    if (nextSendTime < System.currentTimeMillis()) {
                        ep1 = 8;

                        baos.reset();
                        ep1 = 9;

                        dos.writeShort(sp.nrobjs.length);
                        int devMoved = 0;
                        ep1 = 10;
                        NRDevice u;
                        for (int ui = sp.nrobjs.length - 1; ui >= 0; ui--) {
                            ep1 = 11;
                            u = sp.nrlocs[ui];
                            if (u == null) {
                                u = getDeviceStorage().getNRLocation(sp.nrobjs[ui]);
                                sp.nrlocs[ui] = u;
                            }
                            ep1 = 12;
                            if ((u.loc.latm() == 0) && (u.loc.lonm() == 0)) {
                                dos.writeBoolean(false);
                                continue;
                            }
                            if (u.loc.dt() == sp.uservisidDT[ui]) {
                                dos.writeBoolean(false);
                                continue;
                            }
                            ep1 = 13;
                            devMoved++;
                            dos.writeBoolean(true);
                            dos.writeInt(u.userId);
                            dos.writeByte(u.userType);
                            //out.writeUTF(u.Name);
                            NRLocation loc = u.loc;
                            dos.writeInt(loc.latm());
                            dos.writeInt(loc.lonm());
                            dos.writeShort(loc.alt());
                            dos.writeShort(loc.spd());
                            dos.writeShort(loc.crs());
                            dos.writeLong(loc.dt());
                            ep1 = 14;
                            sp.uservisidDT[ui] = loc.dt();
                        }
                        out.writeByte(MNProtocol.COMMAND_OTHERCOORDS);
                        final byte[] devicesBytes = baos.toByteArray();
                        out.writeInt(devicesBytes.length);
                        out.write(devicesBytes);
                        //out.flush();


                        baos.reset();
                        dos.writeShort(sp.nrobjs.length);
                        ep1 = 10;
                        for (int ui = sp.nrobjs.length - 1; ui >= 0; ui--) {
                            ep1 = 11;
                            u = sp.nrlocs[ui];
//            if (u==null) {
//              u = NRStorage.getNRLocation(sp.nrobjs[ui]);
//              sp.nrlocs[ui]=u;
//            }
                            ep1 = 12;
                            if ((u.sound == null) || (u.soundDT == sp.usersndDT[ui])) {
                                dos.writeBoolean(false);
                                continue;
                            }
                            ep1 = 13;
                            dos.writeBoolean(true);
                            dos.writeInt(u.sound.length);
                            dos.write(u.sound);
                            dos.writeUTF(u.soundFormat);
                            ep1 = 14;
                            sp.usersndDT[ui] = u.soundDT;
                            LOG.info("Sound sent for:" + u.name + ':' + remoteAddr);
                        }
                        out.writeByte(MNProtocol.COMMAND_SOUND);
                        final byte[] soundBytes = baos.toByteArray();
                        out.writeInt(soundBytes.length);
                        out.write(soundBytes);
                        out.flush();
                        if (devMoved > 0) {
                            LOG.info("Sent move: " + devMoved);
                        }
                        nextSendTime = System.currentTimeMillis() + 600;
                    }
                }

            }
            if (lastReadTime < System.currentTimeMillis() - 60000) {
                LOG.warn("lastreadtime " + new Date(lastReadTime) + " (no data comes), stopping..");
                flag = false;
            } else {
                flag = true;
            }
            Thread.sleep(50);
        } catch (Throwable t) {
            flag = false;
            LOG.error("EXC MN:" + remoteAddr + ':' + ep1 + ':' + t);
        }
        return flag;
    }
}
