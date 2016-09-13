package nl.martijndwars.webpush;

import com.google.common.io.BaseEncoding;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

public class PushService {
    /**
     * The Google Cloud Messaging API key (for pre-VAPID in Chrome)
     */
    private String gcmApiKey;

    /**
     * Subject used in the JWT payload (for VAPID)
     */
    private String subject;

    /**
     * The public key (for VAPID)
     */
    private PublicKey publicKey;

    /**
     * The private key (for VAPID)
     */
    private Key privateKey;

    /**
     * Encrypt the payload using the user's public key using Elliptic Curve
     * Diffie Hellman cryptography over the prime256v1 curve.
     *
     * @return An Encrypted object containing the public key, salt, and
     * ciphertext, which can be sent to the other party.
     */
    public static Encrypted encrypt(byte[] buffer, PublicKey userPublicKey, byte[] userAuth, int padSize) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {
        ECNamedCurveParameterSpec parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1");

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC");
        keyPairGenerator.initialize(parameterSpec);

        KeyPair serverKey = keyPairGenerator.generateKeyPair();

        Map<String, KeyPair> keys = new HashMap<>();
        keys.put("server-key-id", serverKey);

        Map<String, String> labels = new HashMap<>();
        labels.put("server-key-id", "P-256");

        byte[] salt = SecureRandom.getSeed(16);

        HttpEce httpEce = new HttpEce(keys, labels);
        byte[] ciphertext = httpEce.encrypt(buffer, salt, null, "server-key-id", userPublicKey, userAuth, padSize);

        return new Encrypted(serverKey.getPublic(), salt, ciphertext);
    }

    /**
     * Send a notification
     */
    public HttpResponse send(Notification notification) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException, JoseException {
        BaseEncoding base64url = BaseEncoding.base64Url();

        Encrypted encrypted = encrypt(
            notification.payload(),
            notification.userPublicKey(),
            notification.userAuth(),
            notification.getPadSize()
        );

        byte[] dh = Utils.savePublicKey((ECPublicKey) encrypted.publicKey());
        byte[] salt = encrypted.salt().value();

        HttpClient httpClient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(notification.endpoint());
        httpPost.addHeader("TTL", String.valueOf(notification.ttl()));

        Map<String, String> headers = new HashMap<>();

        if (notification.hasPayload()) {
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Content-Encoding", "aesgcm");
            headers.put("Encryption", "keyid=p256dh;salt=" + base64url.omitPadding().encode(salt));
            headers.put("Crypto-Key", "keyid=p256dh;dh=" + base64url.encode(dh));

            httpPost.setEntity(new ByteArrayEntity(encrypted.ciphertext().value()));
        }

        if (notification.isGcm()) {
            if (gcmApiKey == null) {
                throw new IllegalStateException("An GCM API key is needed to send a push notification to a GCM endpint.");
            }

            headers.put("Authorization", "key=" + gcmApiKey);
        }

        if (vapidEnabled() && !notification.isGcm()) {
            JwtClaims claims = new JwtClaims();
            claims.setAudience(notification.getOrigin());
            claims.setExpirationTimeMinutesInTheFuture(12*60);
            claims.setSubject(subject);

            JsonWebSignature jws = new JsonWebSignature();
            jws.setHeader("typ", "JWT");
            jws.setHeader("alg", "ES256");
            jws.setPayload(claims.toJson());
            jws.setKey(privateKey);
            jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256);

            headers.put("Authorization", "Bearer " + jws.getCompactSerialization());

            byte[] pk = Utils.savePublicKey((ECPublicKey) publicKey);

            if (headers.containsKey("Crypto-Key")) {
                headers.put("Crypto-Key", headers.get("Crypto-Key") + ";p256ecdsa=" + base64url.omitPadding().encode(pk));
            } else {
                headers.put("Crypto-Key", "p256ecdsa=" + base64url.encode(pk));
            }
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpPost.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
        }

        return httpClient.execute(httpPost);
    }

    /**
     * Set the Google Cloud Messaging (GCM) API key
     *
     * @param gcmApiKey
     * @return
     */
    public PushService setGcmApiKey(String gcmApiKey) {
        this.gcmApiKey = gcmApiKey;

        return this;
    }

    /**
     * Set the JWT subject (for VAPID)
     *
     * @param subject
     * @return
     */
    public PushService setSubject(String subject) {
        this.subject = subject;

        return this;
    }

    /**
     * Set the public key (for VAPID)
     *
     * @param publicKey
     * @return
     */
    public PushService setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;

        return this;
    }

    /**
     * Set the private key (for VAPID)
     *
     * @param privateKey
     * @return
     */
    public PushService setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;

        return this;
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
