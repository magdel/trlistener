/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ru.netradar.server.acceptor;

import org.apache.log4j.Logger;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

/**
 * @author rfk
 */
public abstract class AccepterThread extends Thread {
    public int port;
    public boolean listening = true;
    protected final DiagThreadRegistry diagThreadRegistry;
    protected final DeviceStorage deviceStorage;

    protected AccepterThread(String name, int port, DiagThreadRegistry diagThreadRegistry, DeviceStorage deviceStorage) {
        super(name);
        this.port = port;
        this.diagThreadRegistry = diagThreadRegistry;
        this.deviceStorage = deviceStorage;
    }

    protected abstract Logger getLogger();

    public void init() {
        start();
        getLogger().info("Started...");
    }

    public void shutdown() {
        listening = false;
        interrupt();
        getLogger().info("Interrupted...");
    }
}
