# WebPush

A Web Push library for Java 7. Supports payloads and VAPID.

[![Build Status](https://travis-ci.org/MartijnDwars/web-push.svg?branch=master)](https://travis-ci.org/MartijnDwars/web-push)

## Installation

For Gradle, add the following dependency to `build.gradle`:

```
compile group: 'nl.martijndwars', name: 'web-push', version: '3.0.0'
```

For Maven, add the following dependency to `pom.xml`:

```
<dependency>
    <groupId>nl.martijndwars</groupId>
    <artifactId>web-push</artifactId>
    <version>3.0.0</version>
</dependency>
```

## Building

To build the project yourself, clone this repository and build a run:

```
./gradlew assemble
```

To build a fat JAR in `build/libs` (e.g. to use the CLI):

```
./gradlew shadowJar
```

## CLI

A command-line interface is available to easily generate a keypair (for VAPID) and to try sending a notification.

```
Usage: <main class> [command] [command options]
  Commands:
    generate-key      Generate a VAPID keypair
      Usage: generate-key

    send-notification      Send a push notification
      Usage: send-notification [options]
        Options:
          --subscription
            A subscription in JSON format.
          --publicKey
            The public key as base64url encoded string.
          --privateKey
            The private key as base64url encoded string.
          --payload
            The message to send.
            Default: Hello, world!
```

For example, to generate a keypair and output the keys in base64url encoding:

```
$ java -jar build/libs/web-push-3.0.0-all.jar generate-key
PublicKey:
BGgL7I82SAQM78oyGwaJdrQFhVfZqL9h4Y18BLtgJQ-9pSGXwxqAWQudqmcv41RcWgk1ssUeItv4-8khxbhYveM=

PrivateKey:
ANlfcVVFB4JiMYcI74_h9h04QZ1Ks96AyEa1yrMgDwn3
```

Use the public key in the call to `pushManager.subscribe` to get a subscription. Then, to send a notification:

```
$ java -jar build/libs/web-push-3.0.0-all.jar send-notification \
  --subscription="{'endpoint':'https://fcm.googleapis.com/fcm/send/fH-M3xRoLms:APA91bGB0rkNdxTFsXaJGyyyY7LtEmtHJXy8EqW48zSssxDXXACWCvc9eXjBVU54nrBkARTj4Xvl303PoNc0_rwAMrY9dvkQzi9fkaKLP0vlwoB0uqKygPeL77Y19VYHbj_v_FolUlHa','keys':{'p256dh':'BOtBVgsHVWXzwhDAoFE8P2IgQvabz_tuJjIlNacmS3XZ3fRDuVWiBp8bPR3vHCA78edquclcXXYb-olcj3QtIZ4=','auth':'IOScBh9LW5mJ_K2JwXyNqQ=='}}" \
  --publicKey="BGgL7I82SAQM78oyGwaJdrQFhVfZqL9h4Y18BLtgJQ-9pSGXwxqAWQudqmcv41RcWgk1ssUeItv4-8khxbhYveM=" \
  --privateKey="ANlfcVVFB4JiMYcI74_h9h04QZ1Ks96AyEa1yrMgDwn3"
  --payload="Hello, lovely world!"
```

## API

First, create an instance of the push service:

```
pushService = new PushService(...);
```

Then, create a notification based on the user's subscription:

```
Notification notification = new Notification(...);
```

To send a push notification:

```
pushService.send(notification);
```

Use `sendAsync` instead of `send` to get a `Future<HttpResponse>`:

```
pushService.sendAsync(notification);
```

See [doc/UsageExample.md](https://github.com/MartijnDwars/web-push/blob/master/doc/UsageExample.md)
for detailed usage instructions. If you plan on using VAPID, read [doc/VAPID.md](https://github.com/MartijnDwars/web-push/blob/master/doc/VAPID.md).

## Testing

Our integration tests use Web Push Testing Service (WPTS) to automate browser interaction. To install WPTS:

```
npm install web-push-testing-service -g
```

Then, to start WPTS:

```
web-push-testing-service start wpts
```

Finally, to run all tests:

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
- For NodeJS, see [marco-c/web-push](https://github.com/marco-c/web-push) and [GoogleChrome/push-encryption-node](https://github.com/GoogleChrome/push-encryption-node)
- For Python, see [mozilla-services/pywebpush](https://github.com/mozilla-services/pywebpush)
