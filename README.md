# WebPush

A Web Push library for Java 7. Supports payloads and VAPID.

[![Build Status](https://travis-ci.org/MartijnDwars/web-push.svg?branch=master)](https://travis-ci.org/MartijnDwars/web-push)
[![Build Status](https://saucelabs.com/buildstatus/martijndwars)](https://saucelabs.com/beta/builds/a1930c691f6143beb5b74a34eb62f516)

## Installation

For Gradle, add the following dependency to `build.gradle`:

```
compile group: 'nl.martijndwars', name: 'web-push', version: '2.0.0'
```

For Maven, add the following dependency to `pom.xml`:

```
<dependency>
    <groupId>nl.martijndwars</groupId>
    <artifactId>web-push</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Usage

See [doc/UsageExample.md](https://github.com/MartijnDwars/web-push/blob/master/doc/UsageExample.md)
for detailed usage instructions. If you plan on using VAPID, read [doc/VAPID.md](https://github.com/MartijnDwars/web-push/blob/master/doc/VAPID.md).

## Testing

We have both unit tests and integration tests. The latter uses Selenium to automate web browser interaction. To run the tests:

```
./gradlew test
```

## Credit

To give credit where credit is due, the PushService is mostly a Java port of marco-c/web-push. The HttpEce class is mostly a Java port of martinthomson/encrypted-content-encoding.

## Documentation

- [Generic Event Delivery Using HTTP Push](https://tools.ietf.org/html/draft-ietf-webpush-protocol-11)
- [Voluntary Application Server Identification for Web Push](https://tools.ietf.org/html/draft-ietf-webpush-vapid-01)

## Related

- For PHP, see [Minishlink/web-push](https://github.com/Minishlink/web-push)
- For nodejs, see [marco-c/web-push](https://github.com/marco-c/web-push) and [GoogleChrome/push-encryption-node](https://github.com/GoogleChrome/push-encryption-node)
- For python, see [mozilla-services/pywebpush](https://github.com/mozilla-services/pywebpush)

