package nl.martijndwars.webpush;

import com.google.gson.Gson;
import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.MarionetteDriverManager;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.*;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

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
     * Time to wait for arrival of the push message
     */
    private static long GET_MESSAGE_TIMEOUT = 120L;

    /**
     * Time to wait while registering the subscription
     */
    private static long GET_SUBSCRIPTION_TIMEOUT = 20L;


    @BeforeClass
    public static void beforeClass() throws Exception {
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
            desiredCapabilities.setCapability("platform", "macOS 10.12");
            desiredCapabilities.setCapability("version", "57.0");

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
        FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setPreference("dom.push.testing.ignorePermission", true);

        DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();
        desiredCapabilities.setCapability(FirefoxDriver.PROFILE, firefoxProfile);

        if (isCI()) {
            desiredCapabilities.setCapability("platform", "macOS 10.12");
            desiredCapabilities.setCapability("version", "52.0");

            desiredCapabilities.setCapability("tunnel-identifier", System.getenv("TRAVIS_JOB_NUMBER"));
            desiredCapabilities.setCapability("name", "Travis #" + System.getenv("TRAVIS_JOB_NUMBER"));
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
    private Subscription getSubscription(WebDriver webDriver) {
        // Wait until the subscription is set
        WebDriverWait webDriverWait = new WebDriverWait(webDriver, GET_SUBSCRIPTION_TIMEOUT);
        webDriverWait.until(ExpectedConditions.hasSubscription());

        // Get subscription
        String subscriptionJson = scrapeSubscription(webDriver);

        // Extract data from JSON string
        return new Gson().fromJson(subscriptionJson, Subscription.class);
    }

    /**
     * Wait until the messages arrives and get it.
     *
     * @param webDriver
     * @throws Exception
     */
    private String getMessage(WebDriver webDriver) {
        // Wait until the message is set
        WebDriverWait webDriverWait = new WebDriverWait(webDriver, GET_MESSAGE_TIMEOUT);
        webDriverWait.until(ExpectedConditions.hasMessage());

        // Get message
        return scrapeMessage(webDriver);
    }

    /**
     * Generic testing method that can be parametrized with a WebDriver, URL,
     * PushService and function that performs assertions.
     *
     * @param webDriver
     * @param URL
     * @param pushService
     * @param assertions
     * @throws Exception
     */
    private void test(WebDriver webDriver, String URL, PushService pushService, BiConsumer<WebDriver, HttpResponse> assertions) throws Exception {
        webDriver.get(URL);

        Subscription subscription = getSubscription(webDriver);
        Notification notification = createNotification(subscription);
        HttpResponse httpResponse = pushService.send(notification);

        assertions.accept(webDriver, httpResponse);

        webDriver.quit();
    }

//    @Test
    public void testChrome() throws Exception {
        PushService pushService = new PushService();
        pushService.setGcmApiKey("AAAA27DSsIg:APA91bGu9yaGBoOipiedYCQTVg-46SCo8hoHGvmP1_sPlsO0twEJ8mLGni29SUgq9Aus6yPW1ZyojY16spLtvyfxdKCXeTEuJ9a5HzsLIAZGdJJG7wonaKYDUtAfLjF5yeZG2s-3nC7k");

        test(getChromeDriver(), SERVER_URL, pushService, (webDriver, httpResponse) -> {
            Assert.assertEquals("The endpoint accepts the push message", httpResponse.getStatusLine().getStatusCode(), 201);
            Assert.assertTrue("The browser receives the push message", getPayload().equals(getMessage(webDriver)));
        });
    }

    @Test
    public void testChromeVapid() throws Exception {
        PushService pushService = new PushService();
        pushService.setKeyPair(TestUtils.readVapidKeys());
        pushService.setSubject("mailto:admin@domain.com");

        test(getChromeDriver(), SERVER_URL + "?vapid", pushService, (webDriver, httpResponse) -> {
            Assert.assertEquals("The endpoint accepts the push message", httpResponse.getStatusLine().getStatusCode(), 201);
            Assert.assertTrue("The browser receives the push message", getPayload().equals(getMessage(webDriver)));
        });
    }

//    @Test
    public void testFireFox() throws Exception {
        PushService pushService = new PushService();

        test(getFireFoxDriver(), SERVER_URL, pushService, (webDriver, httpResponse) -> {
            Assert.assertEquals("The endpoint accepts the push message", 201, httpResponse.getStatusLine().getStatusCode());
            Assert.assertTrue("The browser receives the push message", getPayload().equals(getMessage(webDriver)));
        });
    }

//    @Test
    public void testFireFoxVapid() throws Exception {
        PushService pushService = new PushService();
        pushService.setKeyPair(TestUtils.readVapidKeys());
        pushService.setSubject("mailto:admin@domain.com");

        test(getFireFoxDriver(), SERVER_URL + "?vapid", pushService, (webDriver, httpResponse) -> {
            Assert.assertEquals("The endpoint accepts the push message", 201, httpResponse.getStatusLine().getStatusCode());
            Assert.assertTrue("The browser receives the push message", getPayload().equals(getMessage(webDriver)));
        });
    }

    /**
     * Create a notification from the given subscription.
     *
     * @param subscription
     * @return
     */
    private Notification createNotification(Subscription subscription) throws GeneralSecurityException {
        return new Notification(
            subscription.endpoint,
            subscription.keys.p256dh,
            subscription.keys.auth,
            getPayload()
        );
    }

    /**
     * Some dummy payload (a JSON object)
     *
     * @return
     */
    private String getPayload() {
        return "Hello, world!";
    }

    /**
     * Scrape the JSON subscription from the DOM.
     *
     * @param webDriver
     * @return
     */
    private String scrapeSubscription(WebDriver webDriver) {
        JavascriptExecutor javascriptExecutor = ((JavascriptExecutor) webDriver);

        return (String) javascriptExecutor.executeScript("return document.getElementById('subscription').value");
    }

    /**
     * Scrape the message from the DOM.
     *
     * @param webDriver
     * @return
     */
    private String scrapeMessage(WebDriver webDriver) {
        JavascriptExecutor javascriptExecutor = ((JavascriptExecutor) webDriver);

        return (String) javascriptExecutor.executeScript("return document.getElementById('message').value");
    }

    /**
     * Check if we are running as CI
     *
     * @return
     */
    private boolean isCI() {
        return Objects.equals(System.getenv("CI"), "true");
    }
}
