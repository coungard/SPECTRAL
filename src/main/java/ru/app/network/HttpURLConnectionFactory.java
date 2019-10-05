package ru.app.network;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
import java.net.URL;

abstract class HttpURLConnectionFactory {
    private static final int READ_TIMEOUT = 60 * 1000;
    private static final int CONNECTION_TIMEOUT = 60 * 1000;

    static HttpURLConnection getHttpConnection(String host) {
        try {
            if (host.startsWith("https")) return getHttpsConnection(host);
            HttpURLConnection conn = (HttpURLConnection) new URL(host).openConnection();
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HttpsURLConnection getHttpsConnection(String host) {
        try {
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    return true;
                }
            };
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            HttpsURLConnection conn = (HttpsURLConnection) new URL(host).openConnection();
            conn.setSSLSocketFactory(sc.getSocketFactory());
            conn.setConnectTimeout(CONNECTION_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}