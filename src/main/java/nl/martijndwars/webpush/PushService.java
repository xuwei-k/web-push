package nl.martijndwars.webpush;

import com.auth0.jwt.JWTCreator;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicHeader;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.math.ec.ECPoint;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import com.google.crypto.tink.apps.webpush.WebPushHybridEncrypt;
import com.google.crypto.tink.subtle.EllipticCurves;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static nl.martijndwars.webpush.Utils.CURVE;

public class PushService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Subject used in the JWT payload (for VAPID)
     */
    private String subject;

    /**
     * The public key (for VAPID)
     */
    private PublicKey publicKey;

    private String privateKeyString;

    /**
     * The private key (for VAPID)
     */
    private PrivateKey privateKey;

    public PushService() {
    }

    public PushService(String publicKey, String privateKey, String subject) throws GeneralSecurityException {
        this.publicKey = Utils.loadPublicKey(publicKey);
        this.privateKey = Utils.loadPrivateKey(privateKey);
        this.privateKeyString = privateKey;
        this.subject = subject;
    }

    public String vapid_t(Notification message) {
        try {
            URL url = new URL(message.getEndpoint());
            String aud = url.getProtocol() + "://" + url.getHost();

            Date exp = new Date(System.currentTimeMillis() + 12 * 60 * 60 * 1000);

            Algorithm alg = Algorithm.ECDSA256(null, EllipticCurves.getEcPrivateKey(EllipticCurves.CurveType.NIST_P256, privateKeyString.getBytes()));

            return JWT.create().withAudience(aud).withExpiresAt(exp).withSubject(subject).sign(alg);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public byte[] encrypt(Notification message) {
        try {
            byte[] pk = Utils.savePublicKey((ECPublicKey) publicKey);
            WebPushHybridEncrypt aes128gcm = new WebPushHybridEncrypt.Builder()
                    .withAuthSecret(message.getUserAuth())
                    .withRecipientPublicKey(pk)
                    .build();
            return aes128gcm.encrypt(message.getPayload(), null);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Send a notification and wait for the response.
     *
     * @param notification
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws JoseException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public HttpResponse send(Notification notification) throws GeneralSecurityException, IOException, JoseException, ExecutionException, InterruptedException {
        return sendAsync(notification).get();
    }

    /**
     * Send a notification, but don't wait for the response.
     *
     * @param notification
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws JoseException
     */
    public Future<HttpResponse> sendAsync(Notification notification) throws GeneralSecurityException, IOException, JoseException {
        HttpPost httpPost = preparePost(notification);

        final CloseableHttpAsyncClient closeableHttpAsyncClient = HttpAsyncClients.createSystem();
        closeableHttpAsyncClient.start();

        return closeableHttpAsyncClient.execute(httpPost, new ClosableCallback(closeableHttpAsyncClient));
    }


    /**
     * Prepare a HttpPost for Apache async http client
     *
     * @param notification
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws JoseException
     */
    public HttpPost preparePost(Notification notification) throws GeneralSecurityException, IOException, JoseException {
        assert (verifyKeyPair());

        byte[] ciphertext = encrypt(notification);
        String vapid__t = vapid_t(notification);

        HttpPost httpPost = new HttpPost(notification.getEndpoint());
        httpPost.addHeader("TTL", String.valueOf(notification.getTTL()));

        Map<String, String> headers = new HashMap<>();

        if (notification.hasPayload()) {
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Content-Encoding", "aes128gcm");
            httpPost.setEntity(new ByteArrayEntity(ciphertext));
        }

        headers.put("Authorization", "vapid t=" + vapid__t + ",k=" + publicKey); // TODO

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
        }
        return httpPost;
    }

    private boolean verifyKeyPair() {
        ECNamedCurveParameterSpec curveParameters = ECNamedCurveTable.getParameterSpec(CURVE);
        ECPoint g = curveParameters.getG();
        ECPoint sG = g.multiply(((ECPrivateKey) privateKey).getS());

        return sG.equals(((ECPublicKey) publicKey).getQ());
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public KeyPair getKeyPair() {
        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Check if VAPID is enabled
     *
     * @return
     */
    protected boolean vapidEnabled() {
        return publicKey != null && privateKey != null;
    }
}
