/*
 * MNAccepterThread.java
 *
 * Created on 19 18:44
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor.viewer;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.AcceptorProperties;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagStatus;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * @author Raev
 */
public class ViewerService implements DiagInformation {
    private final static Logger LOG = Logger.getLogger(ViewerService.class);
    private final int port;
    private final DiagThreadRegistry diagThreadRegistry;
    private final Runnable acceptorDaemon;
    private Thread acceptorThrd;
    private final Runnable cleanerDaemon;
    private Thread cleanerThrd;

    private final Runnable readProcessorDaemon;
    private final Runnable writeProcessorDaemon;
    private Thread[] readThrds;
    private Thread[] writeThrds;


    private boolean listening = true;
    private final Object connectionsSync = new Object();
    private ArrayList<ViewerConnection> connections = new ArrayList<ViewerConnection>();


    public ViewerService(AcceptorProperties settings, final DiagThreadRegistry diagThreadRegistry,
                         final DeviceStorage deviceStorage) {
        this.diagThreadRegistry = diagThreadRegistry;
        this.port = settings.getPortViewer();
        this.acceptorDaemon = new Runnable() {
            @Override
            public void run() {
                LOG.info("Viewer started");
                ServerSocket serverSocket;

                //try {
                try {
                    serverSocket = new ServerSocket(port);
                } catch (IOException e) {
                    LOG.fatal("Could not listen on port: " + port);
                    listening = false;

                    return;
                }

                LOG.info("Viewer is ready on port " + port);

                try {
                    try {
                        while (listening) {
                            diagStatus.updateStatusOK();
                            Socket newSocket = serverSocket.accept();
                            try {
                                synchronized (connectionsSync) {
                                    ArrayList<ViewerConnection> newConnections = new ArrayList<ViewerConnection>(connections.size() + 1);
                                    newConnections.addAll(connections);
                                    ViewerConnection vc = new ViewerConnection(newSocket, deviceStorage);
                                    newConnections.add(vc);
                                    LOG.info("Connected (total: " + newConnections.size() + ") " + vc);
                                    connections = newConnections;
                                }
                            } catch (IOException e) {
                                LOG.error("On connect: " + e.getMessage(), e);
                            }
                            Thread.sleep(5);
                        }
                    } finally {
                        serverSocket.close();
                        LOG.warn("Viewer is closed!!!!!!!");
                    }

                    diagStatus.updateStatusBAD("Работа завершена!");

                } catch (Throwable ex) {
                    LOG.error(ex);
                    diagStatus.updateStatusBAD("Ошибка входящих: " + ex.toString());
                }

            }
        };

        this.cleanerDaemon = new Runnable() {
            @Override
            public void run() {
                LOG.info("Cleaner started");

                while (listening) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        LOG.warn("Interrupted");
                        return;
                    }
                    boolean hasToClean = false;
                    for (ViewerConnection vc : connections) {
                        if (vc.getProtocol().isDisconnected() || vc.getProtocol().isTimeouted()) {
                            hasToClean = true;
                            break;
                        }
                    }
                    if (hasToClean) {
                        synchronized (connectionsSync) {
                            ArrayList<ViewerConnection> newConnections = new ArrayList<ViewerConnection>(connections);
                            Iterator<ViewerConnection> iterator = newConnections.iterator();
                            while (iterator.hasNext()) {
                                ViewerConnection vc = iterator.next();
                                if (vc.getProtocol().isDisconnected() || vc.getProtocol().isTimeouted()) {
                                    vc.closeConnection();
                                    iterator.remove();
                                    LOG.info("Removed (disc:" + vc.getProtocol().isDisconnected()
                                            + ",to:" + vc.getProtocol().isTimeouted() + ")" + vc);
                                }
                            }
                            connections = newConnections;
                        }
                    }
                }

            }
        };

        this.readProcessorDaemon = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LOG.warn("Exiting..");
                        return;
                    }

                    try {
                       /* for (ViewerConnection vc : connections) {
                            if (vc.tryAcquireRead()) {
                                try {
                                    vc.processRead();
                                } finally {
                                    vc.releaseRead();
                                }
                            }

                        }*/

                    } catch (Throwable t) {
                        LOG.error("Processor daemon: " + t.getMessage(), t);
                    }

                }
            }
        };

        this.writeProcessorDaemon = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        LOG.warn("Exiting..");
                        return;
                    }

                    try {
                        for (ViewerConnection vc : connections) {
                            if (vc.tryAcquireWrite()) {
                                try {
                                    vc.processWrite();
                                } finally {
                                    vc.releaseWrite();
                                }
                            }

                        }

                    } catch (Throwable t) {
                        LOG.error("Processor daemon: " + t.getMessage(), t);
                    }

                }
            }
        };


    }

    void init() {
        diagThreadRegistry.registerThread(ViewerService.this);

        acceptorThrd = new Thread(acceptorDaemon);
        acceptorThrd.setName("ViewerAccepter");
        acceptorThrd.setDaemon(true);
        acceptorThrd.start();

        cleanerThrd = new Thread(cleanerDaemon);
        cleanerThrd.setName("ViewerCleaner");
        cleanerThrd.setDaemon(true);
        cleanerThrd.start();


        readThrds = new Thread[4];
        for (int i = 0; i < readThrds.length; i++) {
            readThrds[i] = new Thread(readProcessorDaemon);
            readThrds[i].setName("VwReader-" + i);
            readThrds[i].start();
        }

        writeThrds = new Thread[4];
        for (int i = 0; i < writeThrds.length; i++) {
            writeThrds[i] = new Thread(writeProcessorDaemon);
            writeThrds[i].setName("VwWriter-" + i);
            writeThrds[i].start();
        }
    }

    void shutdown() {
        listening = false;
        diagThreadRegistry.unregisterThread(ViewerService.this);
        acceptorThrd.interrupt();
        cleanerThrd.interrupt();
        for (int i = 0; i < readThrds.length; i++) {
            readThrds[i].interrupt();
        }
        for (int i = 0; i < writeThrds.length; i++) {
            writeThrds[i].interrupt();
        }
    }

    @Override
    public String getDiagName() {
        return "Сервер Viewer";
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
