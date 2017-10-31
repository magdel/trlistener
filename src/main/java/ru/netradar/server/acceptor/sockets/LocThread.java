/*
 * KKMultiServerThread.java
 *
 * Created on 5 20:54
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor.sockets;

import ru.netradar.server.device.NRDevice;
import ru.netradar.server.dao.DBWriteThread;
import ru.netradar.server.storage.DeviceStorage;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Socket;

public abstract class LocThread extends Thread {

    protected static int TIMEOUT = 900000;

    protected Socket socket = null;
    private final DeviceStorage deviceStorage;

    public LocThread(Socket socket, DeviceStorage deviceStorage) {
        super("MNServerThread");
        setDaemon(true);
        this.socket = socket;
        this.deviceStorage = deviceStorage;
        currentID = nextID++;
    }

    public NRDevice dNR;

    protected DataOutputStream out;
    protected InputStream in;
    public String remoteAddr;
    public long connected = System.currentTimeMillis();
    //public boolean stopped;
    private volatile static long nextID = 1;
    public long currentID;

    public void run() {
        doBeforeRun();
        try {
            processData();
        } finally {
            doAfterRun();
        }
    }

    private void doBeforeRun() {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    abstract protected void processData();

    protected void doAfterRun() {
        //throw new UnsupportedOperationException("Not yet implemented");
    }

    public DeviceStorage getDeviceStorage() {
        return deviceStorage;
    }
}
