package ru.netradar.server.acceptor.viewer;

import org.apache.log4j.Logger;
import ru.netradar.server.acceptor.MNProtocol;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRLocation;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;

import java.io.*;
import java.net.Socket;

/**
 * Хранит состояние подключения
 * Created with IntelliJ IDEA.
 * User: rfk
 * Date: 28.05.14
 * Time: 20:04
 * To change this template use File | Settings | File Templates.
 */
public class ViewerProtocol {
    private final static Logger LOG = Logger.getLogger(ViewerProtocol.class);
    private final DeviceStorage deviceStorage;
    private final ByteArrayInputStream bais;
    private final DataInputStream dis;

    private State state = State.connected;
    private long updated = System.currentTimeMillis();
    private long lastWrite = System.currentTimeMillis();
    private long nextCoordSendTime;

    private byte inBuffer[] = new byte[10000];
    private byte dataBuffer[] = new byte[50000];
    private int dataBufferEnd;
    private DataOutputStream out;
    private InputStream in;
    private int minReadBytes = MNProtocol.COMMAND_HEADERSIZE;
    private Thread readThread;

    private NRObject[] nrobjs;
    private NRDevice[] nrlocs;
    private long[] uservisidDT;
    private long[] usersndDT;
    private String remoteAddr = "unkn";
    private NRDevice nrDevice;

    private ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
    private DataOutputStream dos = new DataOutputStream(baos);


    public ViewerProtocol(final Socket socket, DeviceStorage deviceStorage) throws IOException {
        this.deviceStorage = deviceStorage;


        this.bais = new ByteArrayInputStream(dataBuffer);
        this.dis = new DataInputStream(bais);
        this.readThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
                    in = socket.getInputStream();
                } catch (IOException e) {
                    LOG.info("On stream create: " + e.getMessage(), e);
                    invalidate();
                    return;
                }

                while (!isDisconnected()) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LOG.warn("Interrupted");
                        return;
                    }

                    try {
                        doRead();
                    } catch (IOException e) {
                        LOG.warn("On doRead: " + e.getMessage(), e);
                        invalidate();
                    } catch (Exception e) {
                        LOG.error("On doRead: " + e.getMessage(), e);
                        invalidate();
                    }
                }
            }
        });
        this.readThread.setName("SR");
        this.readThread.setDaemon(true);
        this.readThread.start();
    }

    private void doRead() throws IOException {
        if (state == State.disconnected)
            return;
        fillInputBuffer();
        boolean again;
        do {
            again = false;
            if (hasData()) {
                bais.reset();
                dis.reset();
                byte cmd = dis.readByte();
                int cmdsize = dis.readInt();
                LOG.info("cmd=" + cmd + ",cmdsize=" + cmdsize);
                if (dataBufferEnd - MNProtocol.COMMAND_HEADERSIZE >= cmdsize) {
                    again = true;
                    switch (state) {
                        case connected:
                            if (cmd != MNProtocol.COMMAND_VERSION)
                                throw new RuntimeException("Wrong command - wait for Version ");

                            readClientVersion();
                            setState(State.versionchecked);
                            break;
                        case versionchecked:
                            if (cmd != MNProtocol.COMMAND_AUTHORIZE)
                                throw new RuntimeException("Wrong command - wait for Authorization ");
                            readAuthInfo();
                            setState(State.waitusers);
                            break;
                        case waitusers:
                            if (cmd != MNProtocol.COMMAND_USERLIST)
                                throw new RuntimeException("Wrong command - wait for Authorization ");
                            skipOldUserList();
                            setState(State.authorized);
                            break;
                        case authorized:
                            if (cmd == MNProtocol.COMMAND_PING) {
                                dis.readByte();
                                LOG.info("Ping r");//todo comment

                            } else {

                            }
                            break;
                        case disconnected:
                            //нечего уже делать с таким
                            break;
                    }

                    int skip = cmdsize + MNProtocol.COMMAND_HEADERSIZE;
                    for (int j = skip; j < dataBufferEnd; j++) {
                        dataBuffer[j - skip] = dataBuffer[j];
                    }
                    dataBufferEnd -= skip;

                } else {
                    minReadBytes = 1;
                }
            }
        } while (again);
    }

    private void skipOldUserList() throws IOException {
        short uc = dis.readShort();
        if (uc > 1000) throw new RuntimeException("WR3a!");

        for (int i = 0; i < uc; i++) {
            dis.readInt();
        }

        out.writeByte(MNProtocol.COMMAND_USERLISTANS);
        out.writeInt(MNProtocol.COMMAND_USERLISTANS_SIZE);

        out.writeByte(MNProtocol.COMMAND_USERLISTANS_OK);
        out.flush();
        lastWrite = System.currentTimeMillis();
    }

    private void readAuthInfo() throws IOException {
        String lg = dis.readUTF();
        String ps = dis.readUTF();
        if (lg.length() > 200)
            throw new IllegalArgumentException("big login");
        readThread.setName("VW " + lg);

        //long __startTime = System.currentTimeMillis();
        //String res = Util.getHTTPContentAsString(userCheckUrl + "?lg=" + Util.urlEncodeString(lg) + "&ps=" + Util.urlEncodeString(ps));
        String res = deviceStorage.getSiteClient().executeURI(deviceStorage.getUserCheckUrl() + "?lg=" + Util.urlEncodeString(lg) + "&ps=" + Util.urlEncodeString(ps));
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
            throw new RuntimeException(lg + ':' + " not authorized (res=" + res + ")");
        }
        out.writeByte(MNProtocol.COMMAND_AUTHORIZEANS_OK);
        out.flush();
        lastWrite = System.currentTimeMillis();
        String un = uinfo[1];
        LOG.info(un + ':' + " authorized");
        final String hash = MD5.getHashString(Util.stringToByteArray(ps, true));

        this.remoteAddr = un;
        this.nrDevice = deviceStorage.connectNRLocation(uid, NRObject.MAPNAVUSERTYPE, null, hash);

        if (nrDevice.name == null)
            nrDevice.name = un;
        //servThread.dNR.passwordMD5Hash = hash;
        //LOG.debug("Acquired " + servThread.dNR);
        //http://netradar.ru/cgi-bin/uv.pl?loginame=magdel&logipass=mymap&dt=0
        String sSigned = lg + "0" + MD5.getHashString(ps);
        String sign = MD5.getHashString(sSigned);

        res = deviceStorage.getSiteClient().executeURI(deviceStorage.getUserListUrl() + "?lg=" + lg + "&sign=" + sign + "&dt=0&v=1");
        //LOG.info("Registration info query time = " + ((System.currentTimeMillis() - __startTime)) + " ms\nInfo: " + res);
        String[] list = Util.parseString(res, '\n');
        int uc = 0;
        for (String aList : list)
            if (aList != null) {
                uinfo = Util.parseString("," + aList, ',');
                if (uinfo[0].equals("$NR")) uc++;
            }

        nrobjs = new NRObject[uc];
        nrlocs = new NRDevice[uc];
        uservisidDT = new long[uc];
        usersndDT = new long[uc];
        uc = 0;
        NRObject nr;
        for (String aList : list)
            if (aList != null) {
                uinfo = Util.parseString("," + aList, ',');
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
    }

    private void readClientVersion() throws IOException {
        byte version = dis.readByte();

        out.writeByte(MNProtocol.COMMAND_VERSIONANS);
        out.writeInt(MNProtocol.COMMAND_VERSIONANS_SIZE);
        if (version != MNProtocol.COMMAND_VERSION_VALUE) {
            out.writeByte(MNProtocol.COMMAND_VERSIONANS_BAD);
            out.flush();
            throw new RuntimeException("Version wrong: " + version + " instead of " + MNProtocol.COMMAND_VERSION_VALUE);
        }
        out.writeByte(MNProtocol.COMMAND_VERSIONANS_OK);
        out.flush();
        lastWrite = System.currentTimeMillis();
    }

    private void fillInputBuffer() throws IOException {
        int intRead;
        if (in.available() >= minReadBytes) {
            if ((intRead = in.read(inBuffer)) > 0) {
                System.arraycopy(inBuffer, 0, dataBuffer, dataBufferEnd, intRead);
                dataBufferEnd += intRead;
                minReadBytes = 1;
                updated = System.currentTimeMillis();
            }
        }
    }

    private boolean hasData() {
        return dataBufferEnd > MNProtocol.COMMAND_HEADERSIZE;
    }

    public void doWrite() throws IOException {
        switch (state) {
            case authorized:
                //пишем координаты

                if (nextCoordSendTime < System.currentTimeMillis()) {
                    baos.reset();
                    dos.writeShort(nrobjs.length);
                    int devMoved = 0;
                    NRDevice u;
                    for (int ui = nrobjs.length - 1; ui >= 0; ui--) {
                        u = nrlocs[ui];
                        if (u == null) {
                            u = deviceStorage.getNRLocation(nrobjs[ui]);
                            nrlocs[ui] = u;
                        }
                        if ((u.loc.latm() == 0) && (u.loc.lonm() == 0)) {
                            dos.writeBoolean(false);
                            continue;
                        }
                        if (u.loc.dt() == uservisidDT[ui]) {
                            dos.writeBoolean(false);
                            continue;
                        }

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
                        uservisidDT[ui] = loc.dt();
                    }
                    if (devMoved > 0) {
                        out.writeByte(MNProtocol.COMMAND_OTHERCOORDS);
                        final byte[] devicesBytes = baos.toByteArray();
                        out.writeInt(devicesBytes.length);
                        out.write(devicesBytes);
                        out.flush();
                        lastWrite = System.currentTimeMillis();
                        LOG.info("Sent move: " + devMoved);
                    }
                    nextCoordSendTime = System.currentTimeMillis() + 500;
                }
                /*baos.reset();
                dos.writeShort(nrobjs.length);
                for (int ui = nrobjs.length - 1; ui >= 0; ui--) {
                    u = nrlocs[ui];
//            if (u==null) {
//              u = NRStorage.getNRLocation(sp.nrobjs[ui]);
//              sp.nrlocs[ui]=u;
//            }

                    if ((u.sound == null) || (u.soundDT == usersndDT[ui])) {
                        dos.writeBoolean(false);
                        continue;
                    }

                    dos.writeBoolean(true);
                    dos.writeInt(u.sound.length);
                    dos.write(u.sound);
                    dos.writeUTF(u.soundFormat);
                    usersndDT[ui] = u.soundDT;
                    LOG.info("Sound sent for:" + u.name + ':' + remoteAddr);
                }
                out.writeByte(MNProtocol.COMMAND_SOUND);
                final byte[] soundBytes = baos.toByteArray();
                out.writeInt(soundBytes.length);
                out.write(soundBytes);
                out.flush();*/

                //если новых координат не было, то пишем ping
                if (lastWrite + 10000 < System.currentTimeMillis()) {
                    out.writeByte(MNProtocol.COMMAND_PING);
                    out.writeInt(1);
                    out.writeByte(0);
                    out.flush();
                    lastWrite = System.currentTimeMillis();
                    LOG.info("Ping w");//todo comment
                }
                //только в этом случае пишем изменения
                break;
        }
    }

    private void writeHello() throws IOException {
        out.write('V'); //надо отправить хелло и версию
        out.write('W');
        out.write(1);
        out.write(0);
    }

    public boolean isDisconnected() {
        return state == State.disconnected;
    }

    public boolean isTimeouted() {
        return System.currentTimeMillis() - updated > 41000;
    }

    public void close() {
        readThread.interrupt();
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception e) {
            //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void invalidate() {
        setState(State.disconnected);
    }

    synchronized void setState(State state) {
        if (state == State.disconnected)
            return;
        this.state = state;
    }

    public static enum State {
        connected, versionchecked, waitusers, authorized, disconnected
    }
}
