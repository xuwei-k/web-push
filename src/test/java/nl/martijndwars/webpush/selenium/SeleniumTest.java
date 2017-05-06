package nl.martijndwars.webpush.selenium;

import com.google.common.io.BaseEncoding;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * SeleniumTest performs integration testing.
 */
public class SeleniumTest {
    protected static String PUBLIC_KEY = "BNFDO1MUnNpx0SuQyQcAAWYETa2+W8z/uc5sxByf/UZLHwAhFLwEDxS5iB654KHiryq0AxDhFXS7DVqXDKjjN+8=";
    protected static String PRIVATE_KEY = "AM0aAyoIryzARADnIsSCwg1p1aWFAL3Idc8dNXpf74MH";

    protected static TestingService testingService = new TestingService("http://localhost:8090/api/");
    protected static int testSuiteId;

    protected PushService pushService;

    public SeleniumTest() throws GeneralSecurityException {
        pushService = new PushService(PUBLIC_KEY, PRIVATE_KEY, "http://localhost:8090");
    }

    /**
     * Start the test suite.
     *
     * @throws IOException
     */
    @BeforeClass
    public static void setUp() throws IOException {
        Security.addProvider(new BouncyCastleProvider());

        testSuiteId = testingService.startTestSuite();
    }

    /**
     * Run all tests.
     */
    @Test
    public void runTests() throws IOException, InterruptedException, GeneralSecurityException, JoseException, ExecutionException {
        Collection<Configuration> configurations = getConfigurations();

        for (Configuration configuration : configurations) {
            runTest(configuration);
        }
    }

    /**
     * Run test for the given configuration.
     *
     * @param configuration
     */
    protected void runTest(Configuration configuration) throws IOException, GeneralSecurityException, InterruptedException, ExecutionException, JoseException {
        JsonObject test = testingService.getSubscription(testSuiteId, configuration);

        int testId = test.get("testId").getAsInt();

        Subscription subscription = new Gson().fromJson(test.get("subscription").getAsJsonObject(), Subscription.class);

        Notification notification = new Notification(subscription, "Hello, world");

        HttpResponse response = pushService.send(notification);
        Assert.assertEquals(201, response.getStatusLine().getStatusCode());

        JsonArray messages = testingService.getNotificationStatus(testSuiteId, testId);
        Assert.assertEquals(1, messages.size());
        Assert.assertEquals(new JsonPrimitive("Hello, world"), messages.get(0));
    }

    /**
     * End the test suite.
     *
     * @throws IOException
     */
    @AfterClass
    public static void tearDown() throws IOException {
        testingService.endTestSuite(testSuiteId);
    }

    /**
     * Get browser configurations to test.
     *
     * @return
     */
    protected Collection<Configuration> getConfigurations() {
        BaseEncoding base64Encoding = BaseEncoding.base64();

        String PUBLIC_KEY_NO_PADDING = base64Encoding.omitPadding().encode(
                base64Encoding.decode(PUBLIC_KEY)
        );

        return Arrays.asList(
                new Configuration("chrome", "stable", PUBLIC_KEY_NO_PADDING),
                new Configuration("chrome", "beta", "BNFDO1MUnNpx0SuQyQcAAWYETa2+W8z/uc5sxByf/UZLHwAhFLwEDxS5iB654KHiryq0AxDhFXS7DVqXDKjjN+8"),
                new Configuration("chrome", "unstable", "BNFDO1MUnNpx0SuQyQcAAWYETa2+W8z/uc5sxByf/UZLHwAhFLwEDxS5iB654KHiryq0AxDhFXS7DVqXDKjjN+8")
        );
    }
}
