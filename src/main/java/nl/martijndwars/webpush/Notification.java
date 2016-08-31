package nl.martijndwars.webpush;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.PublicKey;

public class Notification {
    /**
     * The endpoint associated with the push subscription
     */
    private final String endpoint;

    /**
     * The client's public key
     */
    private final PublicKey userPublicKey;

    /**
     * The client's auth
     */
    private final byte[] userAuth;

    /**
     * An arbitrary payload
     */
    private final byte[] payload;

    /**
     * Time in seconds that the push message is retained by the push service
     */
    private final int ttl;

    public Notification(final String endpoint, final PublicKey userPublicKey, byte[] userAuth, final byte[] payload, int ttl) {
        this.endpoint = endpoint;
        this.userPublicKey = userPublicKey;
        this.userAuth = userAuth;
        this.payload = payload;
        this.ttl = ttl;
    }

    public Notification(final String endpoint, final PublicKey userPublicKey, byte[] userAuth, final byte[] payload) {
        this(endpoint, userPublicKey, userAuth, payload, 2419200);
    }

    public String getEndpoint() {
        return endpoint;
    }

    public PublicKey getUserPublicKey() {
        return userPublicKey;
    }

    public byte[] getUserAuth() {
        return userAuth;
    }

    public byte[] getPayload() {
        return payload;
    }

    public boolean hasPayload() {
        return getPayload().length > 0;
    }

    public int getTTL() {
        return ttl;
    }

    public int getPadSize() {
        return 1;
    }

    public String getOrigin() throws MalformedURLException {
        URL url = new URL(getEndpoint());

        return url.getProtocol() + "://" + url.getHost();
    }
}
