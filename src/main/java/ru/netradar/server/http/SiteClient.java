package ru.netradar.server.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ru.netradar.config.properties.WebMonitorProperties;
import ru.netradar.profiler.Profiler;

import java.io.IOException;

/**
 * Connector to site
 * Created by IntelliJ IDEA.
 * User: rfk
 * Date: 06.04.2010
 * Time: 19:45:42
 * To change this template use File | Settings | File Templates.
 */
public class SiteClient {
    private static final Logger LOG = Logger.getLogger(SiteClient.class);

    private final HttpClient httpClient;
    private final HttpHost host;

    public SiteClient(WebMonitorProperties siteSettings) {
        this.httpClient = CommonHTTPSCAConnector.createMultiThreadedHttpClient(siteSettings.getTimeout(), 120000, 10, 70000L);
        this.host = new HttpHost(siteSettings.getHost());
    }

    public HttpResponse executeHttpMethod(HttpRequestBase method) throws IOException {
        final String key = "Site_" + method.getURI().getPath();
        Profiler.startSample(key);
        final HttpResponse response;
        try {
            response = httpClient.execute(host, method);
        } finally {
            Profiler.endSample(key);
        }
        return response;
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

}
