package ru.netradar.server.acceptor;

import org.apache.log4j.Logger;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.MD5;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;


public class MNUDPWorker implements Runnable {
    private final static Logger LOG = Logger.getLogger(MNUDPWorker.class);
    private static final String MNUDP_WORKER = "MNUDPWorker";
    private final DatagramPacket p;
    private final DeviceStorage deviceStorage;

    public MNUDPWorker(DatagramPacket p, DeviceStorage deviceStorage) {
        this.p = p;
        this.deviceStorage = deviceStorage;
    }

    @Override
    public void run() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("UDP Processing packet, size " + p.getLength());
        }
        try {
            final DataInputStream s = new DataInputStream(new ByteArrayInputStream(p.getData(), p.getOffset(), p.getLength()));
            final byte command = s.readByte();
            if (command != 1) {
                return;
            }
            final int cmdSize = s.readInt();
            //    LOG.info("CmdSize " + cmdSize + ", ms: " + s.markSupported());

            ByteArrayOutputStream baos = new ByteArrayOutputStream(60);
            DataOutputStream dos = new DataOutputStream(baos);
            final long userId = s.readLong();
            dos.writeLong(userId);
            final int lat = s.readInt();
            dos.writeInt(lat);
            final int lon = s.readInt();
            dos.writeInt(lon);
            final short alt = s.readShort();
            dos.writeShort(alt);
            final short spd = s.readShort();
            dos.writeShort(spd);
            final short crs = s.readShort();
            dos.writeShort(crs);
            final long satTime = s.readLong();
            dos.writeLong(satTime);
            final byte[] sign = new byte[16];
            s.read(sign);

            final NRDevice device = deviceStorage.getNRLocation(new NRObject((int) userId, NRObject.MAPNAVUSERTYPE));
            if (device.getPasswordMD5HashBytes() == null) {
                LOG.warn("UDP uid:" + userId + " Pass hash is absent");
                return;
            }
            LOG.info("UDP uid:" + userId + ",lat:" + lat + ",lon:" + lon + ",alt:" + alt + ",spd:" + spd + ",dt:" + satTime);
            dos.write(device.getPasswordMD5HashBytes());
            //проверяем подпись
            final byte[] ourSignedData = baos.toByteArray();
            final byte[] ourSign = MD5.getHash(ourSignedData);
            if (!MD5.isEqualsHashs(ourSign, sign)) {
                LOG.warn("UDP uid:" + userId + " sign broken");
                return;
            }

            final NRLocation oldLocation = device.loc;
            final NRLocation newLocation = new NRLocation(lat, lon, alt, spd, crs, satTime);
            if (newLocation.dt() < oldLocation.dt()) {
                LOG.warn("UDP Old data come(uid:" + userId + "):  differs for " + ((oldLocation.dt() - newLocation.dt()) / 1000 + " sec"));
                return;
            }

            device.loc = newLocation;
            deviceStorage.notifyPosition(device, device.loc);

        } catch (IOException e) {
            LOG.error("UDP Packet IO: " + e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("UDP: " + e.getMessage(), e);
        }
    }
}
