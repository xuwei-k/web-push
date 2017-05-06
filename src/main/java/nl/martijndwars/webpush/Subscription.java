package nl.martijndwars.webpush;

public class Subscription {
    public String endpoint;

    public Keys keys;

    public class Keys {
        public String p256dh;

        public String auth;
    }
}
