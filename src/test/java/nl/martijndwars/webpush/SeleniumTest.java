package nl.martijndwars.webpush;

import com.google.common.base.Predicate;
import com.google.common.io.BaseEncoding;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.MarionetteDriverManager;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SeleniumTest {
    /**
     * Port of the server that serves the demo application.
     */
    private static int SERVER_PORT = 8000;

    /**
     * URL of the server that serves the demo application.
     */
    private static String SERVER_URL = "http://localhost:" + SERVER_PORT + "/index.html";

    /**
     * Saucylabs username
     */
    private static String USERNAME = "martijndwars";

    /**
     * Saucylabs access key
     */
    private static String ACCESS_KEY = "58a41adf-8c77-45df-8ef5-40903ce13c81";

    /**
     * Saucylabs remote URL
     */
    private static String REMOTE_DRIVER_URL = "http://" + USERNAME + ":" + ACCESS_KEY + "@localhost:4445/wd/hub";

    /**
     * BaseEncoding service
     */
    private static BaseEncoding base64Url = BaseEncoding.base64Url();

    /**
     * The WebDriver instance used for the test.
     */
    private WebDriver webDriver;

    // TODO: List of drivers and that should be tested, i.e. FireFox with version x, Chrome with version y..


    @BeforeClass
    public static void addSecurityProvider() throws Exception {
        // Set the BouncyCastle provider for cryptographic operations
        Security.addProvider(new BouncyCastleProvider());

        // Run embedded Jetty to serve the demo
        new DemoServer(SERVER_PORT);
    }

    /**
     * Download driver binaries and set path to binaries as system property.
     */
    @Before
    public void setUp() {
        ChromeDriverManager.getInstance().setup();
        MarionetteDriverManager.getInstance().setup();
    }

    /**
     * Get a ChromeWebDriver. In a CI environment, a RemoteDriver for Saucylabs
     * is returned. Otherwise, a ChromeDriver is returned.
     *
     * @return
     * @throws MalformedURLException
     */
    private WebDriver getChromeDriver() throws MalformedURLException {
        Map<String, Object> map = new HashMap<>();
        map.put("profile.default_content_settings.popups", 0);
        map.put("profile.default_content_setting_values.notifications", 1);

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("prefs", map);

        DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);

        if (isCI()) {
            desiredCapabilities.setCapability("version", "52.0");
            desiredCapabilities.setCapability("tunnel-identifier", System.getenv("TRAVIS_JOB_NUMBER"));
            desiredCapabilities.setCapability("name", "Travis #" + System.getenv("TRAVIS_JOB_NUMBER"));
            desiredCapabilities.setCapability("build", System.getenv("TRAVIS_BUILD_NUMBER"));
            desiredCapabilities.setCapability("tags", System.getenv("CI"));

            return new RemoteWebDriver(new URL(REMOTE_DRIVER_URL), desiredCapabilities);
        } else {
            desiredCapabilities.setCapability("marionette", true);

            return new ChromeDriver(desiredCapabilities);
        }
    }

    /**
     * Get a FirefoxDriver. In a CI environment, a RemoteDriver for Saucylabs
     * is returned. Otherwise, a FirefoxDriver is returned.
     *
     * @return
     * @throws MalformedURLException
     */
    private WebDriver getFireFoxDriver() throws URISyntaxException, MalformedURLException {
        // This doesn't work in FireFox 48, so we include a full profile that allows notifications on localhost:8081 instead (see below)
        //FirefoxProfile firefoxProfile = new FirefoxProfile();
        //firefoxProfile.setPreference("notification.prompt.testing", false);
        //firefoxProfile.setPreference("notification.prompt.testing.allow", true);

        // On my machine, I can use the profile in /Application Support/FireFox/Profiles
        //FirefoxProfile firefoxProfile = new ProfilesIni().getProfile("selenium");

        // But for others, we ship the full profile as a resource..
        FirefoxProfile firefoxProfile = new FirefoxProfile(new File(this.getClass().getClassLoader().getResource("firefox").toURI()));

        DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
        desiredCapabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

        if (isCI()) {
            desiredCapabilities.setCapability("tunnel-identifier", System.getenv("TRAVIS_JOB_NUMBER"));
            desiredCapabilities.setCapability("name", "Travis #" + System.getenv("TRAVIS_JOB_NUMBER"));
            desiredCapabilities.setCapability("version", "48.0");
            desiredCapabilities.setCapability("build", System.getenv("TRAVIS_BUILD_NUMBER"));
            desiredCapabilities.setCapability("tags", System.getenv("CI"));

            return new RemoteWebDriver(new URL(REMOTE_DRIVER_URL), desiredCapabilities);
        } else {
            return new FirefoxDriver(desiredCapabilities);
        }
    }

    /**
     * Get a subscription from the browser
     *
     * @param webDriver
     * @throws Exception
     */
    private String[] getSubscription(WebDriver webDriver) throws Exception {
        // Wait until the subscription is set
        (new WebDriverWait(webDriver, 20L)).until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver webDriver) {
                return ((JavascriptExecutor) webDriver)
                    .executeScript("return document.getElementById('subscription').value != ''")
                    .equals(true);
            }
        });

        // Get subscription
        JavascriptExecutor javascriptExecutor = ((JavascriptExecutor) webDriver);

        String subscriptionJson = (String) javascriptExecutor.executeScript("return document.getElementById('subscription').value");

        // Extract data from JSON string
        JSONObject subscription = new JSONObject(subscriptionJson);
        String endpoint = subscription.getString("endpoint");
        String userAuthBase64 = subscription.getJSONObject("keys").getString("auth");
        String userPublicKeyBase64 = subscription.getJSONObject("keys").getString("p256dh");

        return new String[]{
            endpoint,
            userAuthBase64,
            userPublicKeyBase64
        };
    }

    @Test
    public void testChromeNoVapid() throws Exception {
        webDriver = getChromeDriver();
        webDriver.get(SERVER_URL);

        String[] subscription = getSubscription(webDriver);
        String endpoint = subscription[0];
        String userAuthBase64 = subscription[1];
        String userPublicKeyBase64 = subscription[2];

        Notification notification = new Notification(endpoint, userPublicKeyBase64, userAuthBase64, getPayload());

        PushService pushService = new PushService();
        pushService.setGcmApiKey("AIzaSyDSa2bw0b0UGOmkZRw-dqHGQRI_JqpiHug");

        HttpResponse httpResponse = pushService.send(notification);

        assert (httpResponse.getStatusLine().getStatusCode() == 201);
    }

    @Test
    public void testChromeVapid() throws Exception {
        KeyPair keyPair = generateVapidKeys();

        webDriver = getChromeDriver();
        webDriver.get(SERVER_URL + "?" + base64Url.encode(Utils.savePublicKey((ECPublicKey) keyPair.getPublic())));

        String[] subscription = getSubscription(webDriver);
        String endpoint = subscription[0];
        String userAuthBase64 = subscription[1];
        String userPublicKeyBase64 = subscription[2];

        Notification notification = new Notification(endpoint, userPublicKeyBase64, userAuthBase64, getPayload());

        PushService pushService = new PushService();
        pushService.setPublicKey(keyPair.getPublic());
        pushService.setPrivateKey(keyPair.getPrivate());
        pushService.setSubject("mailto:admin@domain.com");

        HttpResponse httpResponse = pushService.send(notification);

        assert (httpResponse.getStatusLine().getStatusCode() == 201);
    }

    @Test
    public void testFireFoxVapid() throws Exception {
        KeyPair keyPair = readVapidKeys();

        webDriver = getFireFoxDriver();
        webDriver.get(SERVER_URL + "?" + base64Url.encode(Utils.savePublicKey((ECPublicKey) keyPair.getPublic())));

        String[] subscription = getSubscription(webDriver);
        String endpoint = subscription[0];
        String userAuthBase64 = subscription[1];
        String userPublicKeyBase64 = subscription[2];

        Notification notification = new Notification(endpoint, userPublicKeyBase64, userAuthBase64, getPayload());

        PushService pushService = new PushService();
        pushService.setPublicKey(keyPair.getPublic());
        pushService.setPrivateKey(keyPair.getPrivate());
        pushService.setSubject("mailto:admin@domain.com");

        HttpResponse httpResponse = pushService.send(notification);

        assert (httpResponse.getStatusLine().getStatusCode() == 201);
    }

    @Test
    public void testFireFoxNoVapid() throws Exception {
        webDriver = getFireFoxDriver();
        webDriver.get(SERVER_URL);

        String[] subscription = getSubscription(webDriver);
        String endpoint = subscription[0];
        String userAuthBase64 = subscription[1];
        String userPublicKeyBase64 = subscription[2];

        Notification notification = new Notification(endpoint, userPublicKeyBase64, userAuthBase64, getPayload());

        PushService pushService = new PushService();

        HttpResponse httpResponse = pushService.send(notification);

        assert (httpResponse.getStatusLine().getStatusCode() == 201);
    }

    /**
     * Generate a public-private keypair on the prime256v1 curve.
     *
     * @return
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException
     */
    private KeyPair generateVapidKeys() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "BC");
        keyPairGenerator.initialize(ECNamedCurveTable.getParameterSpec("prime256v1"), new SecureRandom());

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Read the public-private keypair from the classpath
     *
     * @return
     */
    private KeyPair readVapidKeys() throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(getClass().getResourceAsStream("vapid.pem"))) {
            PEMParser pemParser = new PEMParser(inputStreamReader);

            return (KeyPair) pemParser.readObject();
        } catch (IOException e) {
            throw new IOException("The private key could not be decrypted", e);
        }
    }

    /**
     * Check if we are running as CI
     *
     * @return
     */
    private boolean isCI() {
        return Objects.equals(System.getenv("CI"), "true");
    }

    /**
     * Some dummy payload (a JSON object)
     *
     * @return
     */
    private String getPayload() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.append("title", "Hello");
        jsonObject.append("message", "World");

        return jsonObject.toString();
    }

    @After
    public void tearDown() throws InterruptedException {
        // Leave the browser open, so we actually see the notification arriving. Should be automated at some point..
        Thread.sleep(5000);

        if (webDriver != null) {
            webDriver.quit();
        }
    }
}
