package ru.netradar.server.monitor;

import org.apache.log4j.Logger;
import ru.netradar.config.properties.DeleteMonitorProperties;
import ru.netradar.server.device.NRDevice;
import ru.netradar.server.diag.DiagThreadRegistry;
import ru.netradar.server.storage.DeviceStorage;
import ru.netradar.util.MD5;
import ru.netradar.util.Util;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 01.10.2009
 * Time: 17:56:36
 */
public class DBDeleteMonitorThread extends AbstractMonitorThread {

    @Override
    public String getDiagName() {
        return "Очистка БД";  //To change body of implemented methods use File | Settings | File Templates.
    }

    private static final Logger LOG = Logger.getLogger(DBDeleteMonitorThread.class);
    private final DeleteMonitorProperties settings;


    public DBDeleteMonitorThread(DeleteMonitorProperties settings, String logName,
                                 DeviceStorage deviceStorage, DiagThreadRegistry diagThreadRegistry) {
        super(60000 * 60, logName, false, deviceStorage, diagThreadRegistry);
        this.settings = settings;
    }


    @Override
    public void monitorBegin() {
        LOG.info("Activity =-> DB Clear delUrl=" + settings.getDatadeleteurl());
    }

    private void makeClearRequest(String table, String key, String sv) {
        String sign = MD5.getHashString(table + key + sv + settings.getRspass());
        final String comandURL = settings.getDatadeleteurl() +
                "?tbl=" + Util.urlEncodeString(table) +
                "&key=" + Util.urlEncodeString(key) +
                "&sv=" + Util.urlEncodeString(sv) +
                "&sign=" + sign;
        LOG.debug("URL for clear: " + comandURL);
        long startTime = System.currentTimeMillis();
        //String response = Util.getHTTPContentAsString(comandURL);
        String response;
        try {
            response = deviceStorage.getSiteClient().executeURI(comandURL);
        } catch (IOException e) {
            LOG.error("Delete: " + e.getMessage() + ", command " + comandURL);
            return;
        }
        LOG.info("DB clear response " +
                (System.currentTimeMillis() - startTime) + "ms " + response + " (T: " + table + ";K: " + key + ";D: " + sv + ")");
    }

    @Override
    public void monitorBefore() throws Exception {

        long smallestDate = System.currentTimeMillis() - settings.getStoredays() * 24 * 60 * 60 * 1000L;
        String table = "e107_nt_tracks";
        String key = "dttm";
        String sv = Util.getDBDateTimeString(smallestDate);

        makeClearRequest(table, key, sv);
        table = "e107_nd_tracks";
        makeClearRequest(table, key, sv);
        table = "e107_nd_tracks";
        makeClearRequest(table, key, sv);
        table = "e107_nd_trtracks";
        makeClearRequest(table, key, sv);
        table = "e107_nd_msgs";
        makeClearRequest(table, key, sv);

        //тут хранить два дня более чем
        table = "e107_nd_events";
        smallestDate = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L;
        sv = Util.getDBDateTimeString(smallestDate);
        makeClearRequest(table, key, sv);

    }

    @Override
    public void monitorDevice(NRDevice nrd) throws Exception {
    }

    @Override
    public void monitorAfter() {
    }

    @Override
    public void monitorEnd() {
    }
}
