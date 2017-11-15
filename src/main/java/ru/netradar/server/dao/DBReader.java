/*
 * DBReader.java
 *
 * Created on 22 21:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.dao;

import org.apache.log4j.Logger;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.bus.domain.NRLocation;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.storage.DeviceDao;

/**
 * @author RFK
 */
public class DBReader implements DiagInformation {
    private final static Logger LOG = Logger.getLogger(DBReader.class);

    Object queueSync = new Object();
    private final DeviceDao deviceDao;
    private final DBWriteThread dbWrite;


    public DBReader(DeviceDao deviceDao, DBWriteThread dbWrite) {
        this.deviceDao = deviceDao;
        this.dbWrite = dbWrite;
    }

    public synchronized void loadFromDB(NRDevice user) {
        long readStartedTM = System.currentTimeMillis();
        try {
            NRLocation loc = deviceDao.readLocation(user);
            if (loc == null) {
                dbWrite.put("INSERT INTO DEVICES (DEV_ID,UT) VALUES (" + user.userId + "," + user.userType + ")");
                LOG.info("R:" + user.userId + '-' + user.userType + ':' + " is inserted");
            } else {
                user.loc = loc;
                LOG.info("Readed (" + (System.currentTimeMillis() - readStartedTM) + " ms) location for " + user + " : " + loc);
            }
            diagStatus.updateStatusOK();

        } catch (Throwable t) {
            diagStatus.updateStatusBAD("Error:" + t.getMessage());
            LOG.error(t);
        }
    }

    public void init() {
        //  DiagThreadRegistry.registerThread(this);
    }

    public void shutdown() {
        // DiagThreadRegistry.unregisterThread(this);
    }

    @Override
    public String getDiagName() {
        return "DB read";
    }

    @Override
    public String getDiagStatus() {
        return diagStatus.getStatusName();
    }

    @Override
    public String getDiagDescription() {
        return diagStatus.getStatusDesc();
    }

    DiagStatus diagStatus = new DiagStatus();
}
