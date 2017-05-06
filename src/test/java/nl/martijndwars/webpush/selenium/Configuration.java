package nl.martijndwars.webpush.selenium;

public class Configuration {
    protected final String browser;
    protected final String version;
    protected final String publicKey;

    Configuration(String browser, String version) {
        this(browser, version, "");
    }

    Configuration(String browser, String version, String publicKey) {
        this.browser = browser;
        this.version = version;
        this.publicKey = publicKey;
    }
}
