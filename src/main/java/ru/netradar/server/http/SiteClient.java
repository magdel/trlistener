package ru.netradar.server.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.server.bus.domain.DeviceIden;
import ru.netradar.server.bus.handler.tr102.Tr102Iden;
import ru.netradar.util.Util;

import java.io.IOException;
import java.util.Optional;

/**
 * Connector to site
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 06.04.2010
 * Time: 19:45:42
 * To change this template use File | Settings | File Templates.
 */
public class SiteClient {
    private static final Logger LOG = LoggerFactory.getLogger(SiteClient.class);

    private final HttpClient httpClient;
    private final HttpHost host;
    private final String userCheckUrl;

    public SiteClient(WebMonitorProperties siteSettings) {
        this.httpClient = CommonHTTPSCAConnector.createMultiThreadedHttpClient(siteSettings.getTimeout(), 120000, 10, 70000L);
        this.host = new HttpHost(siteSettings.getHost());
        this.userCheckUrl = siteSettings.getUsercheckurl();
    }

    public HttpResponse executeHttpMethod(HttpRequestBase method) throws IOException {
        return httpClient.execute(host, method);
    }

    public String executeURI(String param) throws IOException {
        HttpGet httpUriRequest = new HttpGet(param);
        HttpResponse httpResponse = null;
        String response;
        long startTime = System.currentTimeMillis();
        try {
            httpResponse = executeHttpMethod(httpUriRequest);
            startTime = System.currentTimeMillis() - startTime;
            response = EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            LOG.error("Execute URI: " + e.getMessage() + ": " + e + ", request: " + param);
            throw e;
        } finally {
            if (httpResponse != null) {
                EntityUtils.consumeQuietly(httpResponse.getEntity());
            }
        }
        LOG.info("Response (" + startTime + " ms): " + response + " for " + param);
        return response;
    }

    public Optional<DeviceIden> findTr102Iden(Tr102Iden tr102Iden) throws IOException {
        LOG.info("Finding: {}", tr102Iden);
        String res = executeURI(userCheckUrl + "?imei=" + tr102Iden.getImei() + "&ut=3");

        LOG.info("Resp: " + res);
        //res = "$AU,"+(new Random()).nextInt()+","+(new Random()).nextInt();

        String[] uinfo = Util.parseString(res, ',');
        if (uinfo[0].equals("")) {
            LOG.info("Not registered");
            return Optional.empty();
        }

        int uid = Integer.valueOf(uinfo[0]);
        return Optional.of(new DeviceIden(uid, DeviceIden.Type.tr102));
    }

}
