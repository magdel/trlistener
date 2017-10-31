/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.netradar.server;

import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author rfk
 */
public class NRServer {
    private final Logger LOG = Logger.getLogger(NRServer.class);
    public static NRServer server;
    public static final long START_TIME = System.currentTimeMillis();

    public NRServer() {
        LOG.debug(getClass().getName() + " is creating...");
    }

    public boolean serverRunning = true;

    AbstractApplicationContext context;

    public void runServer(boolean skipSleep) {
        final long startSleep = skipSleep ? 50 : 25000;

        LOG.info("Sleeping before start for " + startSleep + "ms...");
        try {
            Thread.sleep(startSleep);
        } catch (InterruptedException e) {
            LOG.fatal("On start sleep: " + e.getMessage(), e);
            return;
        }


        LOG.info("DB patches verified successfully");
        try {
            LOG.info("Creating application context...");
            context = new ClassPathXmlApplicationContext("classpath*:META-INF/nr-app-*.xml");
            context.registerShutdownHook();
            LOG.info("Application context created");
        } catch (Throwable t) {
            LOG.error("Error creating context: " + t.getMessage(), t);
            serverRunning = false;
        }

        System.out.println("Server is running...");
        while (serverRunning) {
            LOG.debug("Server is running...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ttt) {
                LOG.warn("Server thread interrupted");
                break;
            }
            serverRunning = context.isActive();
        }

        System.out.println("Server is ending...");
        LOG.info("Server cycle finished");
        if (context != null)
            context.close();


        LOG.info("Server context is closed successfully");

    }
}
