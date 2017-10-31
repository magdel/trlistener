/*
 * TRServerThread.java
 *
 * Created on 19 18:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor.sockets.connect;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.acceptor.TRProtocol;
import ru.netradar.server.acceptor.sockets.LocThread;
import ru.netradar.server.storage.DeviceStorage;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;

/**
 * @author Raev
 */
public class TRLocThread extends LocThread {
    private final static Logger LOG = Logger.getLogger(TRLocThread.class);

    TRServerProtocol sp;
    private byte inBuffer[] = new byte[500];
    private byte dataBuffer[] = new byte[1000];
    private int dataBufferEnd;
    public static volatile int conCount;
    public static volatile int tryCount;
    private final WebMonitorProperties webSettings;

    /**
     * Creates a new instance of TRServerThread
     */
    public TRLocThread(Socket socket, WebMonitorProperties webSettings, DeviceStorage deviceStorage) {
        super(socket, deviceStorage);
        this.webSettings = webSettings;
        setName("TRServerThread");
    }

    protected void processData() {
        float erp = 0;
        try {
            socket.setSoTimeout(TIMEOUT);

            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            in = socket.getInputStream();

            conCount++;
            tryCount++;
            remoteAddr = "" + conCount;

            Date dtt = new Date();
            LOG.info("Connected some TR.  Total " + conCount + " trackers");


//      BufferedReader in = new BufferedReader(
//          new InputStreamReader(
//          socket.getInputStream()));

            String inputLine, outputLine;
            sp = new TRServerProtocol(this, webSettings, getDeviceStorage());

            //System.out.println(remoteAddr +':'+outputLine);

            //out.flush();
            //  bais = new ByteArrayInputStream(dataBuffer);
            //  dis = new DataInputStream(bais);
            // dissc = new DataInputStream(in);
            try {
                try {
                    while ((!isInterrupted()) && waitFor()) ;
                } finally {
                    sp = null;
                    if (dNR != null)
                        getDeviceStorage().disconnectNRLocation(dNR, this);
                }
            } finally {
                //out.close();
                ///in.close();
                socket.close();
            }
        } catch (Throwable e) {
            //flag=false;
            LOG.error("EXC TR ERP:" + erp + ':' + e.toString());
        }
        conCount--;
        String s = remoteAddr + ": disconnected (" + (System.currentTimeMillis() - connected) / 1000 + " sec). Left " + conCount + " trackers";
        LOG.info(s);

    }  //ByteArrayInputStream bais;

    //DataInputStream dis;
    //DataInputStream dissc;
    //ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
    //DataOutputStream dos = new DataOutputStream(baos);
    // private byte minReadBytes = 1;
    long timeout;
    long lastReadTime = System.currentTimeMillis();

    private boolean waitFor() {
        int intRead = 0;
        boolean flag = false;
        float et = 0;
        try {

            intRead = in.read(inBuffer);
            if (intRead < 0) {
                LOG.info(remoteAddr + ": disconnect detected");
                return false;
            }

            if ((intRead) > 0) {
                flag = true;
                et = 1;
                //���������� � �����
                for (int i = 0; i < intRead; i++) {
                    dataBuffer[dataBufferEnd + i] = inBuffer[i];
                }
                et = 2;
                //��������� �����
                dataBufferEnd += intRead;
                //���� \n �� ������ ������
                String outputLine;
                boolean again;
                do {
                    again = false;
                    if (dataBufferEnd > TRProtocol.COMMAND_HEADERSIZE) {
                        //      bais.reset();
                        //     dis.reset();
                        et = 3;
                        for (int i = 0; i < dataBufferEnd; i++) {
                            if (dataBuffer[i] == '!') {
                                //try {
                                et = 4;
                                sp.processCommand(dataBuffer, i);
                                if (isInterrupted()) {
                                    break;
                                }
                                //  } catch (Exception ex) {
                                //    stopped=true;
                                //    LogWriteThread.writeLog.putER("WM TR:"+remoteAddr+':'+ex.toString());
                                //  }
                                et = 5;
                                for (int j = i + 1; j < dataBufferEnd; j++) {
                                    dataBuffer[j - i - 1] = dataBuffer[j];
                                }
                                et = 6;
                                dataBufferEnd -= i + 1;

                                again = true;
                                break;
                            }
                        }
                        et = 7;
                        lastReadTime = System.currentTimeMillis();
                        if (dNR != null) {
                            dNR.lastActivityNET = lastReadTime;
                        }
                    }
                    et = 8;
                } while (again);

            }
            //1   }
            et = 9;
            //15 minutes to wait for info or disconnect
            timeout = System.currentTimeMillis() - lastReadTime;
            //must send more often then once in 10 min
            if (timeout > TIMEOUT) {
                flag = false;
                LOG.info(remoteAddr + ": timed out > " + (timeout / 1000) + " sec of delay (max allowed " + TIMEOUT + ")");
            } else {
                flag = true;
            }
            et = 10;
            Thread.sleep(100);
        } catch (InterruptedException ie) {
            flag = false;
            LOG.warn("EXC TR:" + remoteAddr + ": ET:" + et + " " + ie.toString());
        } catch (Throwable t) {
            flag = false;
            LOG.error("EXC TR:" + remoteAddr + ": ET:" + et + " " + t.toString());
        }
        return flag;
    }

}
