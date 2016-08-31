package nl.martijndwars.webpush;

import com.google.common.io.BaseEncoding;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwsHeader;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class PushService {
    private ExecutorService threadpool = Executors.newFixedThreadPool(1);

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
    private byte[] publicKey;

    /**
     * The private key (for VAPID)
     */
    private byte[] privateKey;

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

        return new Encrypted.Builder()
            .withSalt(salt)
            .withPublicKey(serverKey.getPublic())
            .withCiphertext(ciphertext)
            .build();
    }

    /**
     * Send a notification
     */
    public Future<Content> send(Notification notification) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, InvalidAlgorithmParameterException, IOException, InvalidKeySpecException {
        BaseEncoding base64url = BaseEncoding.base64Url();

        Encrypted encrypted = encrypt(
            notification.getPayload(),
            notification.getUserPublicKey(),
            notification.getUserAuth(),
            notification.getPadSize()
        );

        byte[] dh = Utils.savePublicKey((ECPublicKey) encrypted.getPublicKey());
        byte[] salt = encrypted.getSalt();

        Request request = Request
            .Post(notification.getEndpoint())
            .addHeader("TTL", String.valueOf(notification.getTTL()));

        Map<String, String> headers = new HashMap<>();

        if (notification.hasPayload()) {
            headers.put("Content-Type", "application/octet-stream");
            headers.put("Content-Encoding", "aesgcm128");
            headers.put("Encryption", "keyid=p256dh;salt=" + base64url.omitPadding().encode(salt));
            headers.put("Crypto-Key", "keyid=p256dh;dh=" + base64url.encode(dh));

            request.bodyByteArray(encrypted.getCiphertext());
        }

        if (notification instanceof GcmNotification) {
            if (gcmApiKey == null) {
                throw new IllegalStateException("An GCM API key is needed to send a push notification to a GCM endpint.");
            }

            headers.put("Authorization", "key=" + gcmApiKey);
        }

        if (vapidEnabled() && !(notification instanceof GcmNotification)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.HOUR, 12);

            String compactJws = Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "ES256")
                .setAudience(notification.getOrigin())
                .setExpiration(calendar.getTime())
                .setSubject(subject)
                .signWith(SignatureAlgorithm.ES256, privateKey)
                .compact();

            headers.put("Authorization", "Bearer " + compactJws);

            if (headers.containsKey("Crypto-Key")) {
                headers.put("Crypto-Key", headers.get("Crypto-Key") + ";p256ecdsa=" + base64url.encode(publicKey));
            } else {
                headers.put("Crypto-Key", "p256ecdsa=" + base64url.encode(publicKey));
            }
        }

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.addHeader(new BasicHeader(entry.getKey(), entry.getValue()));
        }

        Async async = Async.newInstance().use(threadpool);

        return async.execute(request);
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
    public PushService setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;

        return this;
    }

    /**
     * Set the private key (for VAPID)
     *
     * @param privateKey
     * @return
     */
    public PushService setPrivateKey(byte[] privateKey) {
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
