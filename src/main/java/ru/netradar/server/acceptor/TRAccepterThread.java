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
import ru.netradar.server.acceptor.sockets.connect.TRLocThread;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Raev
 */
public class TRAccepterThread extends AccepterThread implements DiagInformation {
    private final static Logger LOG = Logger.getLogger(TRAccepterThread.class);

    private final WebMonitorProperties webSettings;

    public TRAccepterThread(AcceptorProperties settings, DiagThreadRegistry diagThreadRegistry, WebMonitorProperties webSettings, DeviceStorage deviceStorage) {
        super("TRAccepterThread", settings.getPortTr102(), diagThreadRegistry, deviceStorage);
        this.webSettings = webSettings;
    }

    public void run() {
        ServerSocket serverSocket;

        diagThreadRegistry.registerThread(this);
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("TR Could not listen on port " + port);
            listening = false;
//      DiagThreadRegistry.unregisterThread(this);
            return;
        }
        LOG.info("TR is ready on port " + port);
        try {
            try {
                while (listening) {
                    diagStatus.updateStatusOK();
                    new TRLocThread(serverSocket.accept(), webSettings, deviceStorage).start();
                    sleep(100);
                }
            } finally {
                serverSocket.close();
                LOG.warn("TRAccepter is closed!!!!!!!");
            }
            diagStatus.updateStatusBAD("Closed listener!");

        } catch (Throwable ex) {
            LOG.error(ex);
            diagStatus.updateStatusBAD("Error in listener: " + ex.toString());
        }
    }

    @Override
    public String getDiagName() {
        return "Сервер TR-102";
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

    @Override
    protected Logger getLogger() {
        return LOG;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
