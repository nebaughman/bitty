package net.nyhm.bitty;

import javax.net.ssl.*;
import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

public final class HttpsUtil
{
    private HttpsUtil()
    {
    }

    /**
     * @param chainfiles must start with server's X.509 cert file and end with cert authority's X.509 cert file
     */
    public static SSLContext loadContext(
        char[] pass,
        //String alias,
        File keyfile,
        File[] chainfiles,
        SecureRandom rand)
        throws Exception
    {
        //Log.info("Initializing SSL");

        KeySpec keySpec = new PKCS8EncodedKeySpec(decodeBase64(readKey(keyfile)));

        // TODO: Instead of assuming RSA, determine key type from spec
        //
        KeyFactory kf = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = kf.generatePrivate(keySpec);

        Certificate[] chain = loadChain(chainfiles);

        KeyStore ks = KeyStore.getInstance("JKS");

        ks.load(null, pass); // init empty keystore

        String alias = "cert"; // TODO: configurable?

        ks.setEntry(alias,
            new KeyStore.PrivateKeyEntry(privateKey, chain),
            new KeyStore.PasswordProtection(pass));

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        //KeyManagerFactory.getDefaultAlgorithm());

        kmf.init(ks, pass);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        //TrustManagerFactory.getDefaultAlgorithm());

        tmf.init(ks);

        SSLContext context = SSLContext.getInstance("TLS");

        context.init(
            kmf.getKeyManagers(),
            tmf.getTrustManagers(),
            rand);

        return context;
    }

    public static byte[] decodeBase64(String base64)
    {
        return DatatypeConverter.parseBase64Binary(base64);
    }

    private static Certificate[] loadChain(File[] chainfiles) throws Exception
    {
        Certificate[] chain = new Certificate[chainfiles.length];
        for (int i = 0; i < chainfiles.length; i++)
        {
            chain[i] = loadCert(chainfiles[i]);
        }
        return chain;
    }

    private static Certificate loadCert(File file) throws Exception
    {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return cf.generateCertificate(new FileInputStream(file));
    }

    private static String readKey(File file) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        Scanner scan = new Scanner(file);
        while (scan.hasNextLine())
        {
            String line = scan.nextLine().trim();
            if (line.length() > 0 && !line.startsWith("-----"))
            {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * This method configures the given urlConnection to accept any certificate it is presented with.
     * This has many security ramifications. Do not use this in production.
     */
    public static void disableTrust(HttpsURLConnection https) throws GeneralSecurityException
    {
        SSLContext context = SSLContext.getInstance("TLS");
        //KeyManagerFactory kmf = KeyManagerFactory.getInstance( // "SunX509"
        //    KeyManagerFactory.getDefaultAlgorithm());
        context.init(null, new TrustManager[] { new TrustAny() }, null);
        https.setSSLSocketFactory(context.getSocketFactory());
    }

    /**
     * A bad trust manager that trusts anything. Allows for SSL connections without certificate verification.
     * This has many security ramifications. Do not use this in production.
     */
    private static final class TrustAny implements X509TrustManager
    {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
        {
            // no exception implies trust
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
        {
            // no exception implies trust
        }

        @Override
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }
    }
}
