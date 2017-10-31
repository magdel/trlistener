/*
 * TRServerThread.java
 *
 * Created on 19 , 18:08
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package ru.netradar.server.acceptor.sockets.connect;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.SMSQueueProperties;
import ru.netradar.profiler.Profiler;
import ru.netradar.server.NRServer;
import ru.netradar.server.acceptor.sockets.LocThread;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.device.NRObject;
import ru.netradar.server.diag.DiagInformation;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.notify.position.PositionTaskDAO;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.Util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.util.*;

/**
 * @author Raev
 */
public class DGLocThread extends LocThread {
    private final static Logger LOG = Logger.getLogger(DGLocThread.class);
    //ARServerProtocol sp;
    //private byte inBuffer[] = new byte[500];
    private byte dataBuffer[] = new byte[1000];
    //private int dataBufferEnd;
    public static int conCount;
    static int tryCount;
    private final DiagThreadRegistry diagThreadRegistry;
    private final PositionTaskDAO positionTaskDAO;
    private final String smsSender;

    public DGLocThread(Socket socket, DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry,
                       SMSQueueProperties smsQueueSettings,
                       PositionTaskDAO positionTaskDAO) {
        super(socket, deviceStorage);
        this.diagThreadRegistry = diagThreadRegistry;
        this.positionTaskDAO = positionTaskDAO;
        this.smsSender = smsQueueSettings.getSender();
        setName("DGServerThread");
    }

    protected void processData() {

        conCount++;
        tryCount++;
        try {
            out = new DataOutputStream(socket.getOutputStream());
            in = socket.getInputStream();

            remoteAddr = "" + conCount;

            //Date dtt = new Date();
            LOG.info("Connected some DG.  Total " + conCount + " diags");

            //String inputLine, outputLine;
            // sp = new ARServerProtocol(this);

            //System.out.println(remoteAddr +':'+outputLine);

            //out.flush();
            bais = new ByteArrayInputStream(dataBuffer);
            dis = new DataInputStream(bais);
            dissc = new DataInputStream(in);
            try {
                while ((!isInterrupted()) && waitFor()) {
                    //   
                }

            } finally {
                out.close();
                in.close();
                socket.close();
            }
        } catch (Throwable e) {
            // e.printStackTrace();
        }
        conCount--;
        String s = remoteAddr + ": disconnected (" + (System.currentTimeMillis() - connected) / 1000 + " sec). Left " + conCount + " diags.";
        LOG.info(s);
    }

    ByteArrayInputStream bais;
    DataInputStream dis;
    DataInputStream dissc;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(200);
    DataOutputStream dos = new DataOutputStream(baos);
    //private byte minReadBytes = 1;
    long timeout;
    long lastReadTime = System.currentTimeMillis();

    private boolean waitFor() {
        //int intRead = 0;
        float erp = 0;
        boolean flag;
        try {
            erp = 0;
            Random r = new Random();
            byte q = (byte) r.nextInt(123);
            out.writeByte(q);
            int reply = in.read();
            if ((reply + q) != 125) {
                return false;
            }
            int pass = in.read();
            pass = (pass << 8) + in.read();
            if (pass != 4758) {
                return false;
            }

            StringBuilder commonInfo = getCommonInfoStringBuilder(smsSender, positionTaskDAO, getDeviceStorage());

            out.writeUTF(commonInfo.toString());

            //now we write our information
            final List<DiagInformation> diagInformationCollection =
                    new ArrayList<DiagInformation>(diagThreadRegistry.elements());
            int cnt = diagInformationCollection.size();
            out.writeInt(cnt);
            for (int i = 0; i < cnt; i++) {
                out.writeInt(i);
                DiagInformation at = diagInformationCollection.get(i);
                out.writeUTF(at.getDiagName());
                out.writeUTF(at.getDiagStatus());
                out.writeUTF(at.getDiagDescription());
            }

            flag = false;
        } catch (Throwable t) {
            flag = false;
            LOG.error("EXC DG ERP:" + erp + ':' + t.toString());
        }
        return flag;
    }

    public static StringBuilder getCommonInfoStringBuilder(String smsSender, PositionTaskDAO positionTaskDAO, DeviceStorage deviceStorage) {
        long workingTime = System.currentTimeMillis() - NRServer.START_TIME;

        StringBuilder commonInfo = new StringBuilder();
        commonInfo.append("Информация по ").append(smsSender).append(" на ").append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(Calendar.getInstance().getTime())).append("\n");
        commonInfo.append("Сервис работает ").append(Util.getHumanTime(workingTime)).append(" с ").append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG).format(startedDate)).append("\n \n");
        int activeConnCount = ARLocThread.conCount;
        commonInfo.append("Подключено ARTAL: ").append(activeConnCount).append("\n");
        activeConnCount = ARLocThread.tryCount;
        commonInfo.append("Попыток подключения ARTAL: ").append(activeConnCount).append("\n \n");

        activeConnCount = MNLocThread.conCount + TRLocThread.conCount + ARLocThread.conCount;
        commonInfo.append("Всего подключено: ").append(activeConnCount).append("\n");
        activeConnCount = MNLocThread.tryCount + TRLocThread.tryCount + ARLocThread.tryCount;
        commonInfo.append("Всего попыток подключения: ").append(activeConnCount).append("\n \n");

        commonInfo.append("Потоков: ").append(Thread.activeCount()).append("\n");
        commonInfo.append("Памяти всего: ").append(Runtime.getRuntime().totalMemory() / 1024).append("кб\n");
        commonInfo.append("Памяти макс: ").append(Runtime.getRuntime().maxMemory() / 1024).append("кб\n \n");
        commonInfo.append("Процессоров: ").append(Runtime.getRuntime().availableProcessors()).append("\n \n");
        commonInfo.append("Очередь координат: ").append(positionTaskDAO.countTasks()).append("\n \n");

        Collection<NRDevice> conns = deviceStorage.getConnectedDevices();
        int nrucount = conns.size() - 1;
        commonInfo.append("Подключенные опознанные устройства (").append(nrucount + 1).append("):\n");
        //if (nrucount > 199) nrucount = 199;
        for (NRDevice nru : conns) {
            //nru = NRStorage.nrConnectedV.get(i);
            String wt = " (" + Util.getHumanTime(System.currentTimeMillis() - nru.getConnectTime()) + ")";

            if (nru.userType == NRObject.ARTALUSERTYPE) {
                commonInfo.append("AR: ").append(nru.name).append("(").append(nru.connectedTimes).append(":").append(nru.disconnectedTimes).append(")").append(wt).append("\n");
            } else if (nru.userType == NRObject.MAPNAVUSERTYPE) {
                commonInfo.append("MN: ").append(nru.name).append("(").append(nru.connectedTimes).append(":").append(nru.disconnectedTimes).append(")").append(wt).append("\n");
            } else if (nru.userType == NRObject.TR102USERTYPE) {
                commonInfo.append("TR: ").append(nru.imei).append("(").append(nru.connectedTimes).append(":").append(nru.disconnectedTimes).append(")").append(wt).append("\n");
            }
        }
        commonInfo.append("\n----------------\n-              -\n----------------\n");
        commonInfo.append("-=<Profilers>=-\n");
        for (String key: Profiler.getKeys()){
            final String logInfo = Profiler.getLogInfo(key);
            commonInfo.append(logInfo).append('\n');
        }

        commonInfo.append("\n----------------\n-              -\n----------------\n");
        commonInfo.append("\nДетали по устройствам:\n");
        for (NRDevice nru : conns) {
            //nru = NRStorage.nrConnectedV.get(i);
            //String wt = empStr;
            //LocThread lt = (LocThread) nru.getLocThread();

            if (nru.userType == NRObject.ARTALUSERTYPE) {
                commonInfo.append("--------\nAR: ").append(nru.name).append("\n---\n").append(nru.getInfo()).append("\n");
            } else if (nru.userType == NRObject.MAPNAVUSERTYPE) {
                commonInfo.append("--------\nMN: ").append(nru.name).append("\n---\n").append(nru.getInfo()).append("\n");
            } else if (nru.userType == NRObject.TR102USERTYPE) {
                commonInfo.append("--------\nTR: ").append(nru.name).append("\n---\n").append(nru.getInfo()).append("\n");
            }
        }
        return commonInfo;
    }

    private static Date startedDate = Calendar.getInstance().getTime();
}
