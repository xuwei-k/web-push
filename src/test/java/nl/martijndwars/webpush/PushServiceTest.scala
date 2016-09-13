package nl.martijndwars.webpush

import org.apache.commons.io.IOUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.BeforeClass
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.Security

object PushServiceTest{
  @BeforeClass def addSecurityProvider(): Unit = {
    Security.addProvider(new BouncyCastleProvider)
  }

  def main(args: Array[String]): Unit = {
    addSecurityProvider()
    val t = new PushServiceTest()
    //t.testPushFirefox()
    t.testPushFirefoxVapid()
    t.testPushChromeVapid()
  }
}

class PushServiceTest {

  @Test
  def testPushFirefoxVapid(): Unit = {
    val endpoint = "https://updates.push.services.mozilla.com/wpush/v2/gAAAAABX16aXPtldVStjKd9QImFVzmU-qxh3IjYch3i6fdUlY2kbTlh1micq1eFbYdn-utSxV97Oq7EZjIpBE1vLVazaHZnQ0OX3XPtHwWQ-tp34u-NCbXwWWtML-B7_SIy_uYHhVVFG4KfI6w0qvthF2Xf2b98t2Lrvb6088Sjh-HZNRBTcAaE"
    val encodedUserAuth = "2A9zwm6Jwp6l51Jq1sV4Pg=="
    val encodedUserPublicKey = "BEqa1zJCVzU9dr6AR6bXpfYigPx3rYUf2d_o9aVxIt5l0iytTs8d9kyM4oDju_5NxKlSP8X3NoQk_2Z_YOV56zM="

    // Base64 string server public/private key
    val vapidPublicKey = "BJXsuKNd1fxVbUEHVeN3nDdnd3+WlKETa1H4P0+JqYCozY1RroOh7XFyhQgcduEiQeA4K7ZUvayM3Wb+OcqUKxE="
    val vapidPrivateKey = "ANswHZjcOe7BhuMXSSzo6MzS9kD86dGTCaGyjj7fUb3C"
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification = new Notification(endpoint, userPublicKey, userAuth, "firefox vapid".getBytes)
    // Construct push service
    val pushService = new PushService
    val httpResponse = pushService.send(
      notification = notification,
      publicKey = Utils.loadPublicKey(vapidPublicKey),
      privateKey = Utils.loadPrivateKey(vapidPrivateKey)
    )
    println(httpResponse.getStatusLine.getStatusCode)
    println(IOUtils.toString(httpResponse.getEntity.getContent, StandardCharsets.UTF_8))
  }

  @Test
  @throws[Exception]
  def testPushChromeVapid(): Unit = {
    val endpoint = "https://fcm.googleapis.com/fcm/send/e2RHVhnaV2c:APA91bGpDxd6icb8F6eLLFnRvRslrO76pDJ7hvAcEJ6wV6gtA55nO3KAv8aOgxkuBQZzQb8H8BGEz7xALA717ZMvgW2ejjOsFCkeCcTw8FCcYZ0Db3dFKrJFerZ9cQeznl1A1uRxos5b"
    val encodedUserAuth = "mjj26aedI45B2WKB3kA5Ag=="
    val encodedUserPublicKey = "BDhYjjf4Vai_7K5Fnk5Fj2k5Q_P_VBCBbXi4xiYi-vQsBmEQax_eDLecS2-VpAilzdeJ9iuOoIpodtvxdGJZYE4="

    // Base64 string server public/private key
    val vapidPublicKey = "BJXsuKNd1fxVbUEHVeN3nDdnd3+WlKETa1H4P0+JqYCozY1RroOh7XFyhQgcduEiQeA4K7ZUvayM3Wb+OcqUKxE="
    val vapidPrivateKey = "ANswHZjcOe7BhuMXSSzo6MzS9kD86dGTCaGyjj7fUb3C"
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification = new Notification(endpoint, userPublicKey, userAuth, "chrome vapid".getBytes)
    // Construct push service
    val pushService = new PushService
    val httpResponse = pushService.send(
      notification = notification,
      publicKey = Utils.loadPublicKey(vapidPublicKey),
      privateKey = Utils.loadPrivateKey(vapidPrivateKey)
    )
    // Send notification!
    println(httpResponse.getStatusLine.getStatusCode)
    println(IOUtils.toString(httpResponse.getEntity.getContent, StandardCharsets.UTF_8))
  }

}