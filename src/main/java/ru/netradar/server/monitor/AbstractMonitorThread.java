/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.netradar.server.monitor;

import org.apache.log4j.Logger;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

/**
 * @author rfk
 */
public abstract class AbstractMonitorThread extends Thread implements DiagInformation {
    private final static Logger LOG = Logger.getLogger(AbstractMonitorThread.class);
    private final long monitorSleep;
    private final boolean processEveryDevice;
    protected final DeviceStorage deviceStorage;
    protected final DiagThreadRegistry diagThreadRegistry;

    public AbstractMonitorThread(long monitorSleep, String logName, boolean processEveryDevice,
                                 DeviceStorage deviceStorage,
                                 DiagThreadRegistry diagThreadRegistry) {
        this.monitorSleep = monitorSleep;
        this.processEveryDevice = processEveryDevice;
        this.deviceStorage = deviceStorage;
        this.diagThreadRegistry = diagThreadRegistry;
        setName(logName);
    }

    public AbstractMonitorThread(long monitorSleep, String logName,
                                 DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry) {
        this(monitorSleep, logName, true, deviceStorage, diagThreadRegistry);
    }

    public void run() {
        LOG.info("Started (period " + monitorSleep + " ms)");
        try {
            monitorBegin();
            try {
                while (true) {
                    diagStatus.updateStatusOK();
                    Thread.sleep(monitorSleep);
                    try {
                        monitorBefore();
                    } catch (Throwable t) {
                        LOG.error("Monitor core error! monitorBefore: " + t.getMessage());
                        diagStatus.updateStatusBAD("MonitorBefore error: " + t);
                    }
                    if (processEveryDevice) {
                        for (NRDevice loc : deviceStorage.getDevices()) {
                            try {
                                monitorDevice(loc);
                            } catch (Throwable tt) {
                                LOG.error("Dev mon: " + loc + ", " + tt.getMessage());
                                diagStatus.updateStatusBAD("Monitor error " + loc.name + "(" + loc.userId + ") tt: " + tt.toString());
                            }
                        }
                    }
                    try {
                        monitorAfter();
                    } catch (Throwable t) {
                        LOG.error("Monitor core error! monitorAfter: " + t.getMessage());
                        diagStatus.updateStatusBAD("MonitorBefore error: " + t);
                    }
                }
            } catch (InterruptedException ex) {
                diagStatus.updateStatusBAD("Прерван поток сервиса: " + ex);
            }
        } catch (Throwable t) {
            diagStatus.updateStatusBAD("Ошибка работы ядра монитора!\n" + t);
            LOG.error("core error: " + t.getMessage(), t);
        }
        try {
            monitorEnd();
        } catch (Throwable t) {
            LOG.error("monitorEnd: " + t.getMessage(), t);
        }
        LOG.info("Ended");
    }

    public abstract void monitorBegin();

    public abstract void monitorBefore() throws Exception;

    public abstract void monitorDevice(NRDevice nrd) throws Exception;

    public abstract void monitorAfter();

    public abstract void monitorEnd();

    protected DiagStatus diagStatus = new DiagStatus();

    public abstract String getDiagName();

    public String getDiagStatus() {
        return diagStatus.getStatusName();
    }

    public String getDiagDescription() {
        return diagStatus.getStatusDesc();
    }

    public final void init() {
        start();
        diagThreadRegistry.registerThread(this);
    }

    public final void shutdown() {
        interrupt();
        diagThreadRegistry.unregisterThread(this);
    }
}
