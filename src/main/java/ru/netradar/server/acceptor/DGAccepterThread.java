/*
 * TRAccepterThread.java
 *
 * Created on 19 , 18:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package ru.netradar.server.acceptor;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.config.properties.SMSQueueProperties;
import ru.netradar.server.acceptor.sockets.connect.DGLocThread;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.notify.position.PositionTaskDAO;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Raev
 */
public class DGAccepterThread extends AccepterThread {
    private final static Logger LOG = Logger.getLogger(DGAccepterThread.class);
    private final SMSQueueProperties smsQueueSettings;
    private final PositionTaskDAO positionTaskDAO;

    public DGAccepterThread(AcceptorProperties settings, DeviceStorage deviceStorage,
                            DiagThreadRegistry diagThreadRegistry, SMSQueueProperties smsQueueSettings,
                            PositionTaskDAO positionTaskDAO) {
        super("DGAccepterThread", settings.getPortDiag(), diagThreadRegistry, deviceStorage);
        this.smsQueueSettings = smsQueueSettings;
        this.positionTaskDAO = positionTaskDAO;
    }

    public void run() {
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            LOG.error("DG could not listen on port " + port);
            listening = false;
            return;
        }
        LOG.info("DG is ready on port " + port);

        try {
            try {
                while (listening) {
                    new DGLocThread(serverSocket.accept(), deviceStorage, diagThreadRegistry, smsQueueSettings, positionTaskDAO).start();
                    Thread.sleep(50);
                }
            } finally {
                serverSocket.close();
                LOG.warn("DGAccepter is closed!!!!!!!");
            }
        } catch (Throwable ex) {
            LOG.error("Diag port close error: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void init() {
        start();
    }

    public void shutdown() {
        interrupt();
    }
}
