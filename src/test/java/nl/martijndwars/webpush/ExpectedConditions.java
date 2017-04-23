package nl.martijndwars.webpush;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import java.util.function.Function;

public class ExpectedConditions {
    /**
     * Check if the browser has aquired a subscription.
     *
     * @return
     */
    public static Function<WebDriver, Boolean> hasSubscription() {
        return webDriver -> {
            JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;

            return javascriptExecutor
                    .executeScript("return document.getElementById('subscription').value != ''")
                    .equals(true);
        };
    }

    /**
     * Check if the browser has received a message.
     *
     * @return
     */
    public static Function<WebDriver, Boolean> hasMessage() {
        return webDriver -> {
            JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;

            return javascriptExecutor
                    .executeScript("return document.getElementById('message').value != ''")
                    .equals(true);
        };
    }
}
