///////////////////////////////////////////////////////////////////////////////////////////////////
//
// Sample class for sending SMS via SMS Traffic
//
// (c) SMS Traffic, 2008
// www.smstraffic.ru, info@smstraffic.ru, (495)228-3649, (495)642-9569
//
// Please edit login, password, and max_parts below before first use
//
///////////////////////////////////////////////////////////////////////////////////////////////////

package ru.smstraffic.smsclient;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.Charset;


public class Sms {
    private static Charset SEND_CHARSET = Charset.forName("windows-1251");
    private static final Logger LOG = Logger.getLogger(Sms.class);
    // you should receive your login and password from your SMS Traffic manager
    private static String login = "snarus";
    private static String password = "rusnar";

    // automatically split each messaage in max_parts number of parts
    private static int max_parts = 1;

    // use failover if main server is not responding properly
    private static boolean bUseAlternativeServer = true;

    // network timeouts
    private static String connectTimeout = "20000"; // in milliseconds
    private static String readTimeout = "20000"; // in milliseconds

    // do not change this
    private static boolean bSslCertificateCheckEnabled = true;

    /////////////////////////////////////////////////////////////////////////
    // returns sms_id
    public static String send(String phone, String message, String originator, int rus) throws SmsException {
        String params;
        try {
            params = "login=" + URLEncoder.encode(login) + "&password=" + URLEncoder.encode(password)
                    + "&phones=" + URLEncoder.encode(phone) + "&originator=" + URLEncoder.encode(originator)
                    + "&message=" + URLEncoder.encode(message, SEND_CHARSET.name()) + "&rus=" + rus
                    + "&max_parts=" + max_parts + "&want_sms_ids=1&tautotruncate=1";
        } catch (UnsupportedEncodingException e) {
            LOG.error("Encoding: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        try {
            return postAndParseXml("https://www.smstraffic.ru/multi.php", params);
        } catch (TransientSmsException e) {
            if (bUseAlternativeServer) {
                if (bSslCertificateCheckEnabled) {
                    disableSslHostVerification();
                    trustAllHttpsCertificates();
                    bSslCertificateCheckEnabled = false;
                }
                return postAndParseXml("https://server1.smstraffic.ru/multi.php", params);
            } else
                throw e;
        }
    }


    // takes script url and POST params, returns sms_id
    private static String postAndParseXml(String script_url, String params) throws SmsException {
        try {
            InputStream is = post(script_url, params);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document document = parser.parse(is);
            Node resultNode = document.getElementsByTagName("result").item(0);
            if (resultNode == null)
                throw new TransientSmsException("result not found");
            if (resultNode.getFirstChild().getNodeValue().equals("OK"))
                return document.getElementsByTagName("sms_id").item(0).getFirstChild().getNodeValue();
            else {
                Node codeNode = document.getElementsByTagName("code").item(0);
                if (codeNode == null)
                    throw new TransientSmsException("code not found");
                String description = document.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
                if (codeNode.getFirstChild().getNodeValue().equals("1000"))
                    throw new TransientSmsException(description);
                else
                    throw new FatalSmsException(description);
            }
        } catch (SAXException e) {
            throw new FatalSmsException("SAXException: " + e.getMessage());
        } catch (IOException e) {
            throw new TransientSmsException("IOException: " + e.getMessage());
        } catch (FactoryConfigurationError e) {
            throw new FatalSmsException("FactoryConfigurationError: " + e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new FatalSmsException("ParserConfigurationException: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new TransientSmsException("NullPointerException: " + e.getMessage());
        }
    }

    // do HTTP GET
    private static InputStream get(String script_url) throws IOException {
        return httpRequest("GET", script_url, null);
    }


    // do HTTP POST
    private static InputStream post(String script_url, String postParams) throws IOException {
        return httpRequest("POST", script_url, postParams);
    }


    // do HTTP request
    private static InputStream httpRequest(String method, String script_url, String postParams) throws IOException {
        LOG.info("Request " + method + " " + script_url + " " + postParams);
        HttpURLConnection connection = null;

        System.setProperty("sun.net.client.defaultConnectTimeout", connectTimeout);
        System.setProperty("sun.net.client.defaultReadTimeout", readTimeout);
        try {
            URL url = new URL(script_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Connection", "Close");

            if (method.equals("POST")) {
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Length", "" + postParams.length());

                OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), SEND_CHARSET);
                writer.write(postParams);
                writer.flush();
                writer.close();
            }
        } catch (MalformedURLException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        } catch (ProtocolException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        /*
          int code = connection.getResponseCode();

          byte[] response=null;
          InputStream is=connection.getInputStream();
          byte[] buf=new byte[4096];
          ByteArrayOutputStream baosResponse=new ByteArrayOutputStream(256);
          int n;
          if (is!=null)
              while ((n=is.read(buf))>0)
                  baosResponse.write(buf, 0, n);
          response=baosResponse.toByteArray();
          System.out.println("response="+new String(response));
          */

        return connection.getInputStream();

    }


    //////////////////////////////////////////////////////////////////////////////
    // SSL stuff
    //////////////////////////////////////////////////////////////////////////////

    private static void disableSslHostVerification() {
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    //////////////////////////////////////////////////////////////////////////////

    private static void trustAllHttpsCertificates() {

        //  Create a trust manager that does not validate certificate chains:

        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new naiveTrustManager();
        trustAllCerts[0] = tm;
        try {
            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, null);
            javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (java.security.KeyManagementException e) {
            System.out.println("KeyManagementException: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    //////////////////////////////////////////////////////////////////////////////

    public static class naiveTrustManager implements javax.net.ssl.TrustManager, javax.net.ssl.X509TrustManager {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(java.security.cert.X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
        }

        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
                throws java.security.cert.CertificateException {
        }
    }

}
