package nl.martijndwars.webpush

import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.junit.BeforeClass
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.Security
import nl.martijndwars.webpush.PushServiceTest.TestParam
import org.asynchttpclient.DefaultAsyncHttpClient
import scala.concurrent.Await
import scala.concurrent.duration._

object PushServiceTest{
  @BeforeClass def addSecurityProvider(): Unit = {
    Security.addProvider(new BouncyCastleProvider)
  }

  final case class TestParam(
    endpoint: String,
    userAuth: String,
    userPublicKey: String,
    vapidPublicKey: String,
    vapidPrivateKey: String,
    payload: String
  )

  def using[A <: AutoCloseable, B](resource: A)(f: A => B): B = {
    try {
      f(resource)
    } finally {
      resource.close()
    }
  }

  def doTest(param: TestParam): Unit = {
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(param.userPublicKey)
    val userAuth = Utils.base64Decode(param.userAuth)
    // Construct notification
    val notification = new Notification(param.endpoint, userPublicKey, userAuth, param.payload.getBytes(StandardCharsets.UTF_8))
    // Construct push service
    val client = new DefaultAsyncHttpClient()
    using(WebpushService.create(client)) { pushService =>
      val future = pushService.send(
        notification = notification,
        publicKey = Utils.loadPublicKey(param.vapidPublicKey),
        privateKey = Utils.loadPrivateKey(param.vapidPrivateKey)
      )
      val response = Await.result(future, 3.seconds)
      println(response.getStatusText)
      println(response.getResponseBody)
    }
  }

}

abstract class WebpushTest{
  def param: TestParam

  final def main(args: Array[String]): Unit = {
    PushServiceTest.addSecurityProvider()
    PushServiceTest.doTest(param)
  }
}

object FirefoxTest extends WebpushTest {
  override val param = TestParam(
    endpoint = "https://updates.push.services.mozilla.com/wpush/v2/gAAAAABX16aXPtldVStjKd9QImFVzmU-qxh3IjYch3i6fdUlY2kbTlh1micq1eFbYdn-utSxV97Oq7EZjIpBE1vLVazaHZnQ0OX3XPtHwWQ-tp34u-NCbXwWWtML-B7_SIy_uYHhVVFG4KfI6w0qvthF2Xf2b98t2Lrvb6088Sjh-HZNRBTcAaE",
    userAuth = "2A9zwm6Jwp6l51Jq1sV4Pg==",
    userPublicKey = "BEqa1zJCVzU9dr6AR6bXpfYigPx3rYUf2d_o9aVxIt5l0iytTs8d9kyM4oDju_5NxKlSP8X3NoQk_2Z_YOV56zM=",
    vapidPublicKey = "BJXsuKNd1fxVbUEHVeN3nDdnd3+WlKETa1H4P0+JqYCozY1RroOh7XFyhQgcduEiQeA4K7ZUvayM3Wb+OcqUKxE=",
    vapidPrivateKey = "ANswHZjcOe7BhuMXSSzo6MzS9kD86dGTCaGyjj7fUb3C",
    payload = s"firefox vapid ${new java.util.Date} ${sys.env.get("HOME")}"
  )
}

object ChromeTest extends WebpushTest {
  override val param = TestParam(
    endpoint = "https://fcm.googleapis.com/fcm/send/e2RHVhnaV2c:APA91bGpDxd6icb8F6eLLFnRvRslrO76pDJ7hvAcEJ6wV6gtA55nO3KAv8aOgxkuBQZzQb8H8BGEz7xALA717ZMvgW2ejjOsFCkeCcTw8FCcYZ0Db3dFKrJFerZ9cQeznl1A1uRxos5b",
    userAuth = "mjj26aedI45B2WKB3kA5Ag==",
    userPublicKey = "BDhYjjf4Vai_7K5Fnk5Fj2k5Q_P_VBCBbXi4xiYi-vQsBmEQax_eDLecS2-VpAilzdeJ9iuOoIpodtvxdGJZYE4=",
    vapidPublicKey = "BJXsuKNd1fxVbUEHVeN3nDdnd3+WlKETa1H4P0+JqYCozY1RroOh7XFyhQgcduEiQeA4K7ZUvayM3Wb+OcqUKxE=",
    vapidPrivateKey = "ANswHZjcOe7BhuMXSSzo6MzS9kD86dGTCaGyjj7fUb3C",
    payload = s"chrome vapid ${new java.util.Date} ${sys.env.get("HOME")}"
  )
}

class PushServiceTest {

  @Test
  def testPushFirefoxVapid(): Unit = {
    PushServiceTest.doTest(FirefoxTest.param)
  }

  @Test
  def testPushChromeVapid(): Unit = {
    PushServiceTest.doTest(ChromeTest.param)
  }

}