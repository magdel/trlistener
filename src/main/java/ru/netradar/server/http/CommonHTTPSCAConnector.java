package ru.netradar.server.http;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

/**
 * HTTPS-коннектор для выполнения https-запросов c CA и проверкой сервера
 * к одному и тому же хосту, либо с явным указанием uri в каждом запросе
 * Для отправки запроса нужно только вызывать метод executeMethod
 * User: magdel
 * Date: 11.11.2009
 * Time: 10:30:23
 */
public class CommonHTTPSCAConnector {
    private final static Logger LOG = Logger.getLogger(CommonHTTPSCAConnector.class);

    private final DefaultHttpClient defaultHttpClient;
    private final HttpHost hostHost;

    /**
     * Создает экземпляр коннектора, работающего по протоколу TLS
     *
     * @param uri            uri, по которому будет обращаться коннектор. Если null, то предполагается использование
     *                       executeMethod(String uri, HttpMethod method). Иначе используйте executeMethod(HttpMethod method).
     * @param maxConnections максимальное количество одновременных запросов
     * @param timeoutMillis  тайм-аут, в мсек
     * @param keypair        ключ клиента
     * @param trustManagers  трастменеджер
     * @throws java.security.KeyManagementException
     *          кривой ключ
     * @throws java.security.NoSuchAlgorithmException
     *          нет требуемого алгоритма
     */
    public CommonHTTPSCAConnector(String uri, int maxConnections, int timeoutMillis, KeyManager[] keypair, TrustManager[] trustManagers) throws NoSuchAlgorithmException, KeyManagementException {
        this(uri, maxConnections, timeoutMillis, keypair, trustManagers, SSLSocketFactory.TLS);
    }

    /**
     * Создает экземпляр коннектора
     *
     * @param uri            uri, по которому будет обращаться коннектор. Если null, то предполагается использование
     *                       executeMethod(String uri, HttpMethod method). Иначе используйте executeMethod(HttpMethod method).
     * @param maxConnections максимальное количество одновременных запросов
     * @param timeoutMillis  тайм-аут, в мсек
     * @param keypair        ключ клиента
     * @param trustManagers  трастменеджер
     * @param protocolName   версия протокола
     * @throws java.security.KeyManagementException
     *          кривой ключ
     * @throws java.security.NoSuchAlgorithmException
     *          нет требуемого алгоритма
     */
    public CommonHTTPSCAConnector(String uri, int maxConnections, int timeoutMillis, KeyManager[] keypair, TrustManager[] trustManagers,
                                  String protocolName) throws NoSuchAlgorithmException, KeyManagementException {
        if (maxConnections <= 0) {
            throw new IllegalArgumentException("Max connections must be more then 0 (" + maxConnections + ")");
        }
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeout must be more then 0 msec (" + timeoutMillis + ")");
        }

        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeoutMillis);
        HttpConnectionParams.setSoTimeout(httpParams, timeoutMillis);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);

        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        final SSLContext sslContext = SSLContext.getInstance(protocolName);
        sslContext.init(keypair, trustManagers, null);
        final SSLSocketFactory socketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        schemeRegistry.register(new Scheme("https", 443, socketFactory));
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        final ThreadSafeClientConnManager connectionManager = new ThreadSafeClientConnManager(schemeRegistry, 3600000L, TimeUnit.MILLISECONDS);
        connectionManager.setMaxTotal(maxConnections * 20);
        connectionManager.setDefaultMaxPerRoute(maxConnections);
        this.defaultHttpClient = new DefaultHttpClient(connectionManager, httpParams);

        if (uri != null) {
            URI hostUri;
            try {
                hostUri = new URI(uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Uri " + uri + " is not valid: " + e.getMessage());
            }

            if (!hostUri.isAbsolute()) {
                throw new IllegalArgumentException("Scheme for uri must be absolute");
            }
            if (hostUri.getUserInfo() != null && !hostUri.getUserInfo().equals("")) {
                defaultHttpClient.getCredentialsProvider().setCredentials(
                        new AuthScope(hostUri.getHost(), hostUri.getPort()),
                        new UsernamePasswordCredentials(hostUri.getUserInfo())
                );
                LOG.info("Initialized with basic authorization");
            }
            this.hostHost = new HttpHost(hostUri.getHost(), hostUri.getPort(), hostUri.getScheme());
        } else {
            this.hostHost = null;
        }
        LOG.info("Created client on " + uri + " for " + maxConnections + " conns with timeout " + timeoutMillis);
    }

    public DefaultHttpClient getDefaultHttpClient() {
        return defaultHttpClient;
    }

    public void setKeepAliveTimeout(final long timeout) {
        defaultHttpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                return timeout;
            }
        });
    }

    /**
     * Выполнить запрос указанным методом
     *
     * @param request запрос
     * @return статус-код
     * @throws java.io.IOException в случае ошибок связи
     */

    public HttpResponse execute(HttpUriRequest request) throws IOException {
        final long startTime = System.currentTimeMillis();
        try {
            if (hostHost != null) {
                return defaultHttpClient.execute(hostHost, request);
            } else {
                return defaultHttpClient.execute(request);
            }
        } finally {
            LOG.info("Response time " + (System.currentTimeMillis() - startTime) + " ms for " + request.getURI());
        }

    }

    /**
     * Method for creating multithreaded HTTP client. The Http calls will be stateless.
     *
     * @param tcpTimeout          тайм-аут сетевого вызова и соединения, мсек
     * @param maxTotalConnections разрешенная ширина пула соедининей
     * @return new HttpClient with applied MultiThreadedHttpConnectionManager
     */
    public static HttpClient createMultiThreadedHttpClient(final int tcpTimeout, final int maxTotalConnections) {
        return createMultiThreadedHttpClient(tcpTimeout, tcpTimeout, maxTotalConnections, 0L);
    }

    public static HttpClient createMultiThreadedHttpClient(final int tcpTimeout, final int maxTotalConnections, final Long keepAliveTimeout) {
        return createMultiThreadedHttpClient(tcpTimeout, tcpTimeout, maxTotalConnections, keepAliveTimeout);
    }

    public static HttpClient createMultiThreadedHttpClient(final int tcpConnectionTimeout, final int tcpSoTimeout, final int maxTotalConnections, final Long keepAliveTimeout) {
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, tcpConnectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParams, tcpSoTimeout);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(maxTotalConnections * 5);
        connectionManager.setDefaultMaxPerRoute(maxTotalConnections);
        DefaultHttpClient client = new DefaultHttpClient(connectionManager, httpParams);
        client.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                return keepAliveTimeout;
            }
        });
        return client;
    }

    /**
     * Задать обработчик повторов запроса.
     *
     * @param httpRequestRetryHandler обработчик повторов запроса.
     */
    public void setHttpRequestRetryHandler(HttpRequestRetryHandler httpRequestRetryHandler) {
        defaultHttpClient.setHttpRequestRetryHandler(httpRequestRetryHandler);
    }

}