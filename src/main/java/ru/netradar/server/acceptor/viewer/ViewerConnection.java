package ru.netradar.server.acceptor.viewer;

import org.apache.log4j.Logger;
import ru.netradar.server.storage.DeviceStorage;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Semaphore;

/**
 * Created with IntelliJ IDEA.
 * User: rfk
 * Date: 28.05.14
 * Time: 1:12
 * To change this template use File | Settings | File Templates.
 */
public class ViewerConnection {
    private final static Logger LOG = Logger.getLogger(ViewerConnection.class);
    private final Socket socket;
    private final long created = System.currentTimeMillis();
    private final Semaphore readSemaphore = new Semaphore(1);
    private final Semaphore writeSemaphore = new Semaphore(1);
    private final ViewerProtocol protocol;

    ViewerConnection(Socket socket, DeviceStorage deviceStorage) throws IOException {
        this.socket = socket;
        this.protocol = new ViewerProtocol(socket, deviceStorage);
    }

    boolean tryAcquireRead() {
        return readSemaphore.tryAcquire();
    }

    void releaseRead() {
        readSemaphore.release();
    }

    boolean tryAcquireWrite() {
        return writeSemaphore.tryAcquire();
    }

    void releaseWrite() {
        writeSemaphore.release();
    }

    public ViewerProtocol getProtocol() {
        return protocol;
    }

    public void closeConnection() {
        protocol.close();
        try {
            socket.close();
        } catch (Exception e) {
            LOG.warn("On closeConn: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "ViewerConnection{" +
                "created=" + created +
                ", socket=" + socket +
                '}';
    }

    public void processWrite() {
        try {
            protocol.doWrite();
        } catch (IOException e) {
            protocol.invalidate();
            LOG.warn("On write: " + e.getMessage() + ", " + this, e);
        } catch (Exception e) {
            protocol.invalidate();
            LOG.error("On write:" + e.getMessage() + ", " + this, e);
        }
    }

    /*public void processRead() {
        try {
            protocol.doRead();
        } catch (IOException e) {
            protocol.invalidate();
            LOG.warn("On read: " + e.getMessage() + ", " + this, e);
        } catch (Exception e) {
            protocol.invalidate();
            LOG.error("On read:" + e.getMessage() + ", " + this, e);
        }
    }*/
}
