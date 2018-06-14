package com.elsevier.fingerprintengine;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

/**
 * Utility class used to Initialise and Configure the CXF <code>WebClient</code> 
 * Contains methods to create a new instance of the WebClient
 */
public class WebClientFactory {
    private String baseUrl;
    private String username;
    private String password;
    private int receiveTimeoutInSeconds;

    public WebClientFactory(String baseUrl, String username, String password, int receiveTimeoutInSeconds) {
        this.baseUrl = baseUrl;
        this.username= username;
        this.password = password;
        this.receiveTimeoutInSeconds = receiveTimeoutInSeconds;
    }

    public WebClient getWebClient(String requestContentType, String path, String responseContentType) {
        WebClient threadSafeClient = WebClient.create(baseUrl, username, password, null);
        threadSafeClient.type(requestContentType).accept(responseContentType);
        secureClient(threadSafeClient);
        setTimeout(threadSafeClient);
        threadSafeClient.path(path);
        return threadSafeClient;
    }

    private void secureClient(WebClient webClient) {
        HTTPConduit conduit = WebClient.getConfig(webClient).getHttpConduit();
        disableSSLCertificateCheck(conduit);
    }

    private void setTimeout(WebClient webClient) {
        HTTPConduit conduit = WebClient.getConfig(webClient).getHttpConduit();
        HTTPClientPolicy policy = conduit.getClient();
        policy.setReceiveTimeout(receiveTimeoutInMilliSeconds());
    }

    private int receiveTimeoutInMilliSeconds() {
        return receiveTimeoutInSeconds * 1000;
    }

    private void disableSSLCertificateCheck(HTTPConduit conduit) {
        TLSClientParameters params = conduit.getTlsClientParameters();

        if (params == null) {
            params = new TLSClientParameters();
            conduit.setTlsClientParameters(params);
        }

        params.setTrustManagers(new TrustManager[] { acceptAllCertsTM });
        params.setDisableCNCheck(true);
    }

    private final TrustManager acceptAllCertsTM = new X509TrustManager() {
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };

}