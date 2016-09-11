package nl.martijndwars.webpush;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Security;

public class PushServiceTest {
    @BeforeClass
    public static void addSecurityProvider() {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Test
    public void testPushFirefoxVapid() throws Exception {
        String endpoint = "https://updates.push.services.mozilla.com/wpush/v1/gAAAAABX1XArWkICQ5ZkxJ13aYHBnlgCNYXGBlyaC_GDCm8SZwi_19Rm4c44gBD4YL8Dw3OwOVIol7LL-h8-lkMWzjXmJXmGE7fWjQ6udP3kLZLHAfv7Usd_HxftFTtzRN-VRV6Zl89h";

        // Base64 string user public key/auth
        String encodedUserPublicKey = "BMvxJXX5zYeikss3eG7B3JE/KDIBJbCWI5QZDvQqBDzBUYyZf4lxWwwjM7ekF+DnbZ+UNQN9w3ccgLsEbrxz8hI=";
        String encodedUserAuth = "KM+OrA7hnlz4UX98YGfheg==";

        // Base64 string server public/private key
        String vapidPublicKey = "BOH8nTQA5iZhl23+NCzGG9prvOZ5BE0MJXBW+GUkQIvRVTVB32JxmX0V1j6z0r7rnT7+bgi6f2g5fMPpAh5brqM=";
        String vapidPrivateKey = "TRlY/7yQzvqcLpgHQTxiU5fVzAAvAw/cdSh5kLFLNqg=";

        // Converting to other data types...
        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);
        byte[] userAuth = Utils.base64Decode(encodedUserAuth);

        // Construct notification
        Notification notification = new Notification(
            endpoint,
            userPublicKey,
            userAuth,
            getPayload()
        );

        // Construct push service
        PushService pushService = new PushService();
        pushService.setSubject("mailto:admin@martijndwars.nl");
        pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey));
        pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey));

        // Send notification!
        HttpResponse httpResponse = pushService.send(notification);

        System.out.println(httpResponse.getStatusLine().getStatusCode());
        System.out.println(IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
    }

    @Test
    public void testPushChromeVapid() throws Exception {
        String endpoint = "https://fcm.googleapis.com/fcm/send/fAAs_rrnDHQ:APA91bHlqjMZzphwP2xckJa9jL0CwtEvlLTL1OEfmRuwqviGLnqQTvMr4WLiwg7jElESXPLYO7qUc5mWvvv-bqs9lRenEbUSL2R191F-quyhE_fZ6JM3giqMQMhAEifDG-s5eHsRPQUG";

        // Base64 string user public key/auth
        String encodedUserPublicKey = "BM9qL254VsQlM8Zi6Hd0khUYSn8075A+td+/DZELdA2L173DIDz42NbjZC51NRfAuVaxh/vT/+UZr37S55EtY7k=";
        String encodedUserAuth = "KaiGaQKMyCW8qEk2NMJwjA==";

        // Base64 string server public/private key
        String vapidPublicKey = "BOH8nTQA5iZhl23+NCzGG9prvOZ5BE0MJXBW+GUkQIvRVTVB32JxmX0V1j6z0r7rnT7+bgi6f2g5fMPpAh5brqM=";
        String vapidPrivateKey = "TRlY/7yQzvqcLpgHQTxiU5fVzAAvAw/cdSh5kLFLNqg=";

        // Converting to other data types...
        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);
        byte[] userAuth = Utils.base64Decode(encodedUserAuth);

        // Construct notification
        Notification notification = new Notification(
            endpoint,
            userPublicKey,
            userAuth,
            getPayload()
        );

        // Construct push service
        PushService pushService = new PushService();
        pushService.setSubject("mailto:admin@martijndwars.nl");
        pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey));
        pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey));

        // Send notification!
        HttpResponse httpResponse = pushService.send(notification);

        System.out.println(httpResponse.getStatusLine().getStatusCode());
        System.out.println(IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
    }

    @Test
    public void testPushFirefox() throws Exception {
        String endpoint = "https://updates.push.services.mozilla.com/wpush/v1/gAAAAABX1Y_lvdzIpzBfRnceQdoNa_DiDy2OH7weXClk5ysidEuoPH8xv0Qq9ADFNTAB4e1TOuT50bbpN-bWVymBqy1b6Mecrz_SHf8Hvh620ViAbL5Zuyp5AqlA7i6g4BGX8h1H23zH";

        // Base64 string user public key/auth
        String encodedUserPublicKey = "BNYbTpyTEUFNK9BacT1rgpx7SXuKkLVKOF0LFnK8mLyPeW3SLk3nmXoPXSCkNKovcKChNxbG+q3mGW9J8JRg+6w=";
        String encodedUserAuth = "40SZaWpcvu55C+mlWxu0kA==";

        // Converting to other data types...
        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);
        byte[] userAuth = Utils.base64Decode(encodedUserAuth);

        // Construct notification
        Notification notification = new Notification(
            endpoint,
            userPublicKey,
            userAuth,
            getPayload()
        );

        // Construct push service
        PushService pushService = new PushService();

        // Send notification!
        HttpResponse httpResponse = pushService.send(notification);

        System.out.println(httpResponse.getStatusLine().getStatusCode());
        System.out.println(IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
    }

    @Test
    public void testPushChrome() throws Exception {
        String endpoint = "https://android.googleapis.com/gcm/send/fIYEoSib764:APA91bGLILlBB9XnndQC-fWWM1D-Ji2reiVnRS-sM_kfHQyVssWadi6XRCfd9Dxf74fL6y3-Zaazohhl_W4MCLaqhdr5-WucacYjQS6B5-VyOwYQxzEkU2QABvUUxBcZw91SHYDGmkIt";

        // Base64 string user public key/auth
        String encodedUserPublicKey = "BA7JhUzMirCMHC94XO4ODFb7sYzZPMERp2AFfHLs1Hi1ghdvUfid8dlNseAsXD7LAF+J33X+ViRJ/APpW8cnrko=";
        String encodedUserAuth = "8wtwPHBdZ7LWY4p4WWJIzA==";

        // Converting to other data types...
        PublicKey userPublicKey = Utils.loadPublicKey(encodedUserPublicKey);
        byte[] userAuth = Utils.base64Decode(encodedUserAuth);

        // Construct notification
        Notification notification = new Notification(
            endpoint,
            userPublicKey,
            userAuth,
            getPayload()
        );

        // Construct push service
        PushService pushService = new PushService();
        pushService.setGcmApiKey("AIzaSyDSa2bw0b0UGOmkZRw-dqHGQRI_JqpiHug");

        // Send notification!
        HttpResponse httpResponse = pushService.send(notification);

        System.out.println(httpResponse.getStatusLine().getStatusCode());
        System.out.println(IOUtils.toString(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
    }

    /**
     * Some dummy payload (a JSON object)
     *
     * @return
     */
    private byte[] getPayload() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("title", "Hello");
        jsonObject.append("message", "World");

        return jsonObject.toString().getBytes();
    }
}
