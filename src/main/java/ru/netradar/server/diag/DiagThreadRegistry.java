/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.netradar.server.diag;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author rfk
 */
public class DiagThreadRegistry {
    private final static Logger LOG = Logger.getLogger(DiagThreadRegistry.class);

    private Map<String, DiagInformation> threadList = new HashMap<String, DiagInformation>();

    public DiagThreadRegistry() {
    }

    public void registerThread(DiagInformation thrd) {
        threadList.put(thrd.getDiagName(), thrd);
        LOG.info("Diag registered: " + thrd.getDiagName());
    }

    public void unregisterThread(DiagInformation thrd) {
        threadList.remove(thrd.getDiagName());
        LOG.info("Diag unregistered:" + thrd.getDiagName() + ":" + thrd.getDiagDescription());
    }

    public Collection<DiagInformation> elements() {
        return threadList.values();
    }

}
