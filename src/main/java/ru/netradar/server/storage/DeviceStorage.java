package ru.netradar.server.storage;

import org.apache.log4j.Logger;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRLocation;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.http.SiteClient;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище положения устройств
 * <p/>
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 21.03.2010
 * Time: 15:25:49
 * To change this template use File | Settings | File Templates.
 */
public class DeviceStorage implements DiagInformation {
    private static final Logger LOG = Logger.getLogger(DeviceStorage.class);
    private static final String NOTIFY_POSITION = "NotifyPosition";
    private static final String NOTIFY_INFO = "NotifyInfo";
    private final Map<NRObject, NRDevice> devices = new ConcurrentHashMap<NRObject, NRDevice>();
    private final Set<NRDevice> connected = new HashSet<NRDevice>();
    private final DeviceDao deviceDAO;
    private final LocationNotifier locationNotifier;
    private final InfoNotifier infoNotifier;
    private final TransactionTemplate txTemplate;
    private static final String DBSTORAGE = "DBStorage";


    private final Object syncObj = new Object();
    private final String userCheckUrl;
    private final String userListUrl;
    private boolean shutteddown;

    private SiteClient siteClient;


    public DeviceStorage(DeviceDao deviceDAO, LocationNotifier locationNotifier, InfoNotifier infoNotifier,
                         TransactionTemplate txTemplate, WebMonitorProperties settings) {
        this.deviceDAO = deviceDAO;
        this.locationNotifier = locationNotifier;
        this.infoNotifier = infoNotifier;

        this.txTemplate = txTemplate;
        this.userCheckUrl = settings.getUsercheckurl();
        this.userListUrl = settings.getListuserurl();
    }

    public NRDevice getNRLocation(NRObject nro) {
        NRDevice user;
        user = devices.get(nro);
        if (user == null) {
            user = new NRDevice(nro.userId, nro.userType);
            loadFromDB(user);
            devices.put(user, user);
            LOG.info("" + user.userId + '-' + user.userType + ':' + " is up on request");
        }
        return user;
    }

    public Collection<NRDevice> getDevices() {
        return devices.values();
    }

    public Collection<NRDevice> getConnectedDevices() {
        ArrayList<NRDevice> c = new ArrayList<NRDevice>();
        synchronized (syncObj) {
            c.addAll(connected);
        }
        return c;
    }

    public NRDevice connectNRLocation(int userId, byte userType, Thread lt, String hash) {
        if (shutteddown) {
            return null;
        }
        NRDevice locs = new NRDevice(userId, userType);
        NRDevice dev;
        dev = devices.get(locs);
        if (dev == null) {
            dev = locs;
            dev.setPasswordMD5Hash(hash);
            loadFromDB(dev);
            devices.put(dev, dev);
            LOG.info("" + userId + '-' + userType + ':' + " is up on connect");
        } else {
            dev.setPasswordMD5Hash(hash);
            LOG.info("" + userId + '-' + userType + ':' + " is here already");
        }
        if (lt != null) {
            dev.setConnectedLocThread(lt);
            synchronized (syncObj) {
                connected.add(dev);
            }
        }
        return dev;
    }

    public void disconnectNRLocation(NRDevice dev, Thread lt) {
        dev.setDisconnectedLocThread(lt);
        synchronized (syncObj) {
            if (dev.getLocThread() == null) {
                connected.remove(dev);
            }
        }
    }

    public void notifyPosition(final NRDevice device, final NRLocation location) {
        //проверить последнее время обновления у этого девайса
        final long time = System.currentTimeMillis();
        if (time - device.getLastStoredPosTime() > 3000) {
            device.setLastStoredPosTime(time);
            txTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    locationNotifier.notifyLocation(device, location);
                    deviceDAO.updateDeviceLocation(device, location);
                }
            });
        }
    }

    public void notifyInfo(NRDevice device, String info, long time) {
        infoNotifier.notifyInfo(device, info, time);
    }

    public void init() {
        LOG.info("NR Storage initiated...");
    }

    public void shutdown() {
        shutteddown = true;
        LOG.info("NR Storage shutting down...");
        ArrayList<NRDevice> devicesToDisconnect = new ArrayList<NRDevice>();
        synchronized (syncObj) {
            devicesToDisconnect.addAll(connected);
        }
        for (NRDevice dev : devicesToDisconnect) {
            disconnectNRLocation(dev, null);
        }
        LOG.info("NR Storage shutted down.");
    }

    public synchronized void loadFromDB(NRDevice user) {
        long readStartedTM = System.currentTimeMillis();
        try {
            NRLocation loc = deviceDAO.readLocation(user);
            if (loc == null) {
                deviceDAO.storeEmptyDevice(user);
            } else {
                user.loc = loc;
                LOG.info("Readed (" + (System.currentTimeMillis() - readStartedTM) + " ms) location for " + user + " : " + loc);
            }
            diagStatus.updateStatusOK();

        } catch (Throwable t) {
            diagStatus.updateStatusBAD("Error:" + t.getMessage());
            LOG.error("Load device: " + t.getMessage(), t);
        }
    }

    private final DiagStatus diagStatus = new DiagStatus();

    @Override
    public String getDiagName() {
        return DBSTORAGE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getDiagStatus() {
        return diagStatus.getStatusName();
    }

    @Override
    public String getDiagDescription() {
        return diagStatus.getStatusDesc();
    }

    public SiteClient getSiteClient() {
        return siteClient;
    }

    public void setSiteClient(SiteClient siteClient) {
        this.siteClient = siteClient;
    }

    public String getUserCheckUrl() {
        return userCheckUrl;
    }

    public String getUserListUrl() {
        return userListUrl;
    }
}
