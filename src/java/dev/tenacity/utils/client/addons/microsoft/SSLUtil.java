package dev.tenacity.utils.client.addons.microsoft;

import javax.net.ssl.*;
import java.io.InputStream;
import java.security.KeyStore;

public class SSLUtil {
    private static SSLContext ctx;

    static {
        try {
            // Load keystore from resources
            KeyStore myKeyStore = KeyStore.getInstance("JKS");
            InputStream keystoreStream = SSLUtil.class.getResourceAsStream("/accountmanager-ssl.jks");
            if (keystoreStream == null) {
                throw new RuntimeException("Could not find accountmanager-ssl.jks in resources");
            }
            myKeyStore.load(keystoreStream, "changeit".toCharArray());

            // Initialize TrustManagerFactory with the keystore
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(myKeyStore);

            // Set up the SSL context with the trust managers from the keystore
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tmf.getTrustManagers(), null);

            // Set as the default SSL socket factory
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize custom SSLContext", e);
        }
    }

    public static SSLContext getSSLContext() {
        return ctx;
    }
}