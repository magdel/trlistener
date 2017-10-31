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
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Raev
 */
public class MNUDPAccepterThread extends AccepterThread implements DiagInformation {
    public static final int BUFFER_SIZE = 120;
    private final ExecutorService executorService;

    @Override
    protected Logger getLogger() {
        return LOG;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private final static Logger LOG = Logger.getLogger(MNUDPAccepterThread.class);


    public MNUDPAccepterThread(AcceptorProperties settings,
                               DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry) {
        super("MNUDPAccepterThread", settings.getPortMapnavUdp(), diagThreadRegistry, deviceStorage);
        this.executorService = Executors.newFixedThreadPool(settings.getPortMapnavUdpCount());
    }

    public void run() {
        DatagramSocket serverSocket;

        diagThreadRegistry.registerThread(this);
        //try {
        try {
            serverSocket = new DatagramSocket(port);
        } catch (IOException e) {
            LOG.error("MN UDP Could not listen on UDP port: " + port);
            listening = false;

            return;
        }

        LOG.info("MN UDP is ready on port " + port);

        try {
            try {
                //byte[] incomeBuffer = new byte[64];
                while (listening) {
                    diagStatus.updateStatusOK();

                    DatagramPacket p = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
                    serverSocket.receive(p);
                    final MNUDPWorker worker = new MNUDPWorker(p, deviceStorage);
                    executorService.submit(worker);
                }
            } finally {
                serverSocket.close();
                LOG.warn("MNUDPAccepter is closed!!!!!!!");
            }

            diagStatus.updateStatusBAD("Работа завершена!");

        } catch (Throwable ex) {
            LOG.error(ex);
            diagStatus.updateStatusBAD("Ошибка входящих: " + ex.toString());
        }
        executorService.shutdown();
        //} finally {
        //   diagThreadRegistry.unregisterThread(this);

        //}
    }

    @Override
    public String getDiagName() {
        return "Сервер UDP MapNav";
    }

    @Override
    public String getDiagStatus() {
        return diagStatus.getStatusName();
    }

    @Override
    public String getDiagDescription() {
        return diagStatus.getStatusDesc();
    }

    private final DiagStatus diagStatus = new DiagStatus();
}