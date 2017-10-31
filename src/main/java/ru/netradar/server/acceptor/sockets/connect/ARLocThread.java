/*
 * TRServerThread.java
 *
 * Created on 19 ., 18:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor.sockets.connect;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.ARProtocol;
import ru.netradar.server.acceptor.sockets.LocThread;
import ru.netradar.server.storage.DeviceStorage;

import java.io.*;
import java.net.Socket;

/**
 * @author Raev
 */
public class ARLocThread extends LocThread {
    private final static Logger LOG = Logger.getLogger(ARLocThread.class);

    ARServerProtocol sp;
    private byte inBuffer[] = new byte[500];
    byte dataBuffer[] = new byte[1000];
    private int dataBufferEnd;
    public static volatile int conCount;
    public static volatile int tryCount;
    private final WebMonitorProperties webSettings;

    public ARLocThread(Socket socket, WebMonitorProperties webSettings, DeviceStorage deviceStorage) {
        super(socket, deviceStorage);
        this.webSettings = webSettings;
        setName("ARServerThread");
    }

    protected void processData() {

        conCount++;
        tryCount++;
        try {
            socket.setSoTimeout(TIMEOUT);

            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = socket.getInputStream();

            remoteAddr = "" + conCount;

            //Date dtt=new Date();
            LOG.info("Connected some AR.  Total " + conCount + " artals");

            //String inputLine, outputLine;
            sp = new ARServerProtocol(this, webSettings, getDeviceStorage());

            //System.out.println(remoteAddr +':'+outputLine);

            //out.flush();
            bais = new ByteArrayInputStream(dataBuffer);
            dis = new DataInputStream(bais);
            dissc = new DataInputStream(in);
            try {
                try {
                    while ((!isInterrupted()) && waitFor()) {

                    }
                } finally {
                    sp = null;
                    if (dNR != null) {
                        getDeviceStorage().disconnectNRLocation(dNR, this);
                    }
                }
            } finally {
                //out.close();
                //in.close();
                socket.close();
            }
        } catch (Throwable e) {
            LOG.error("AR Error data process: " + e.toString());
        }
        conCount--;
        String s = remoteAddr + ": disconnected (" + (System.currentTimeMillis() - connected) / 1000 + " sec). Left " + conCount + " artals";
        LOG.info(s);
    }

    ByteArrayInputStream bais;
    DataInputStream dis;
    DataInputStream dissc;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
    DataOutputStream dos = new DataOutputStream(baos);
    private byte minReadBytes = 1;
    long timeout;
    long lastReadTime = System.currentTimeMillis();

    private boolean waitFor() {
        int intRead = 0;
        float erp = 0;
        boolean flag = false;
        try {
            erp = 0;
            intRead = in.read(inBuffer);
            if (intRead < 0) {
                LOG.info(remoteAddr + ": disconnect detected");
                return false;
            }

            if ((intRead) > 0) {

                for (int i = 0; i < intRead; i++) {
                    dataBuffer[dataBufferEnd + i] = inBuffer[i];
                }
                dataBufferEnd += intRead;

                boolean again;
                do {
                    again = false;
                    erp = 4f;
                    if (dataBufferEnd >= ARProtocol.COMMAND_HEADERSIZE) {
                        erp = 5f;
                        bais.reset();
                        erp = 6f;
                        byte cmd = dis.readByte();
                        erp = 7f;
                        int cmdsize = dis.readShort();
                        erp = 8f;
                        if (dataBufferEnd - ARProtocol.COMMAND_HEADERSIZE >= cmdsize) {
                            erp = 9f;
                            sp.processCommand(cmd, cmdsize, dis, out);
                            erp = 10f;
                            //  ep1=5;
                            int i = cmdsize + ARProtocol.COMMAND_HEADERSIZE;
                            //  ep1=6;
                            erp = 11f;
                            for (int j = i; j < dataBufferEnd; j++) {
                                dataBuffer[j - i] = dataBuffer[j];
                                //   ep1=7;
                            }
                            erp = 12f;
                            dataBufferEnd -= i;
                            again = true;
                        } else {
                            minReadBytes = 1;
                        }
                        erp = 12f;

                        lastReadTime = System.currentTimeMillis();
                        if (dNR != null) {
                            dNR.lastActivityNET = lastReadTime;
                        }
                    }

                } while (again);

            }
            //15 minutes to wait for info or disconnect

            timeout = System.currentTimeMillis() - lastReadTime;
            if (timeout > 900000) {
                flag = false;
                LOG.info(remoteAddr + ": timed out > " + (timeout / 1000) + " sec of delay");
            } else {
                flag = true;
            }
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            flag = false;
            LOG.warn("Interrupted:" + erp);
        } catch (Throwable t) {
            flag = false;
            LOG.error("Undefined error:" + erp + ':' + t.toString(), t);
        }
        return flag;
    }
}
