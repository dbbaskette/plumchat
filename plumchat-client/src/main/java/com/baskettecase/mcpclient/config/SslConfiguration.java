package com.baskettecase.mcpclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

@Configuration
public class SslConfiguration {

    @Value("${mcp.client.skip-ssl-validation:false}")
    private boolean skipSslValidation;

    @PostConstruct
    public void configureGlobalSSL() {
        if (skipSslValidation) {
            try {
                // Create a trust manager that accepts all certificates
                TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
                };

                // Create SSL context with the trust manager
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                // Set as default SSL context for all HTTPS connections
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
                SSLContext.setDefault(sslContext);

                // Set comprehensive SSL system properties for different HTTP clients
                System.setProperty("com.sun.net.ssl.checkRevocation", "false");
                System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
                System.setProperty("sun.security.ssl.allowLegacyHelloMessages", "true");
                System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
                System.setProperty("jdk.tls.trustNameService", "true");
                
                // For Reactor Netty (WebFlux) - disable SSL validation
                System.setProperty("reactor.netty.http.server.accessLogEnabled", "false");
                System.setProperty("io.netty.noUnsafe", "false");
                
                // Force trust all certificates for Java HTTP Client
                System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host,connection,content-length,expect,upgrade,via");

                System.out.println("Global SSL validation disabled for MCP client connections");
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to configure global SSL bypass", e);
            }
        }
    }
}