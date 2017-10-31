/*
 * MNAccepterThread.java
 *
 * Created on 19 18:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.sockets.connect.MNLocThread;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.ServerSocket;


/**
 * @author Raev
 */
public class MNAccepterThread extends AccepterThread implements DiagInformation {
    private WebMonitorProperties webSettings;

    @Override
    protected Logger getLogger() {
        return LOG;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private final static Logger LOG = Logger.getLogger(MNAccepterThread.class);


    public MNAccepterThread(AcceptorProperties settings, WebMonitorProperties webSettings,
                            DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry) {
        super("MNAccepterThread", settings.getPortMapnav(), diagThreadRegistry, deviceStorage);
        this.webSettings = webSettings;
    }

    public void run() {
        ServerSocket serverSocket;

        diagThreadRegistry.registerThread(this);
        //try {
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("MN Could not listen on port: " + port);
            listening = false;

            return;
        }

        LOG.info("MN is ready on port " + port);

        try {
            try {
                while (listening) {
                    diagStatus.updateStatusOK();

                    new MNLocThread(serverSocket.accept(), webSettings, deviceStorage).start();
                    Thread.sleep(50);
                }
            } finally {
                serverSocket.close();
                LOG.warn("MNAccepter is closed!!!!!!!");
            }

            diagStatus.updateStatusBAD("Работа завершена!");

        } catch (Throwable ex) {
            LOG.error(ex);
            diagStatus.updateStatusBAD("Ошибка входящих: " + ex.toString());
        }
        //} finally {
        //   diagThreadRegistry.unregisterThread(this);

        //}
    }

    @Override
    public String getDiagName() {
        return "Сервер MapNav";
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
