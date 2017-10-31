/*
 * TRAccepterThread.java
 *
 * Created on 19 18:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.sockets.connect.ARLocThread;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Raev
 */
public class ARAccepterThread extends AccepterThread implements DiagInformation {
    @Override
    protected Logger getLogger() {
        return LOG;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private final static Logger LOG = Logger.getLogger(ARAccepterThread.class);
    private final WebMonitorProperties webSettings;

    /**
     * Creates a new instance of TRAccepterThread
     */
    public ARAccepterThread(AcceptorProperties settings, DiagThreadRegistry diagThreadRegistry,
                            WebMonitorProperties webSettings, DeviceStorage deviceStorage) {
        super("ARAccepterThread", settings.getPortArtal(), diagThreadRegistry, deviceStorage);
        this.webSettings = webSettings;
    }

    public void run() {
        ServerSocket serverSocket = null;
        diagThreadRegistry.registerThread(this);

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("AR Could not listen on port " + port);
            listening = false;

            return;
        }
        LOG.info("AR is ready on port " + port);

        try {
            try {
                while (listening) {
                    diagStatus.updateStatusOK();
                    new ARLocThread(serverSocket.accept(), webSettings, deviceStorage).start();
                    sleep(50);
                }
            } finally {
                serverSocket.close();
                LOG.warn("ARAccepter is closed!!!!!!!");
            }
            diagStatus.updateStatusBAD("Работа завершена!");
        } catch (Throwable ex) {
            LOG.error(ex);
            diagStatus.updateStatusBAD("Ошибка ex: " + ex.toString());
        }
        //  DiagThreadRegistry.unregisterThread(this);

    }

    @Override
    public String getDiagName() {
        return "Сервер Artal";
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
