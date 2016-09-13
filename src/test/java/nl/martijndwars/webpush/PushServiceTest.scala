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
    pushService.setSubject("hoge hoge")
    pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey))
    pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey))
    // Send notification!
    val httpResponse = pushService.send(notification)
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
    pushService.setSubject("hoge hoge")
    pushService.setPublicKey(Utils.loadPublicKey(vapidPublicKey))
    pushService.setPrivateKey(Utils.loadPrivateKey(vapidPrivateKey))
    // Send notification!
    val httpResponse = pushService.send(notification)
    println(httpResponse.getStatusLine.getStatusCode)
    println(IOUtils.toString(httpResponse.getEntity.getContent, StandardCharsets.UTF_8))
  }

  @Test
  @throws[Exception]
  def testPushFirefox(): Unit = {

    val endpoint = "https://updates.push.services.mozilla.com/wpush/v1/gAAAAABX149UV6uGhOLjUm0RttESZXojQW0d1FNJfESIJiZ7sPHmznANktBsrbs1oW5Z5n3yymravMrZkJIvJoDU6f2aT-bm5Ei7oLHmNF_LXru_flL9WrJVGxy5ZFRdEGYXLpx-jFoh"
    val encodedUserAuth = "THXSUTo0vF-0-TKXC23f3A=="
    val encodedUserPublicKey = "BDDD-m2_cqcZHFqphi8yqyl6VtgMb4WAi4MVwaI8_ctckM3zkUOe509QJO7M0V1YhqGUnGrgkXkbI4MA_jfEyzw="

    /*
    val endpoint = "https://updates.push.services.mozilla.com/wpush/v1/gAAAAABX1Y_lvdzIpzBfRnceQdoNa_DiDy2OH7weXClk5ysidEuoPH8xv0Qq9ADFNTAB4e1TOuT50bbpN-bWVymBqy1b6Mecrz_SHf8Hvh620ViAbL5Zuyp5AqlA7i6g4BGX8h1H23zH"
    // Base64 string user public key/auth
    val encodedUserPublicKey = "BNYbTpyTEUFNK9BacT1rgpx7SXuKkLVKOF0LFnK8mLyPeW3SLk3nmXoPXSCkNKovcKChNxbG+q3mGW9J8JRg+6w="
    val encodedUserAuth = "40SZaWpcvu55C+mlWxu0kA=="
    */

    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification: Notification = new Notification(endpoint, userPublicKey, userAuth, "firefox".getBytes)
    // Construct push service
    val pushService = new PushService
    // Send notification!
    val httpResponse = pushService.send(notification)
    println(httpResponse.getStatusLine.getStatusCode)
    println(IOUtils.toString(httpResponse.getEntity.getContent, StandardCharsets.UTF_8))
  }

  @Test
  @throws[Exception]
  def testPushChrome(): Unit = {
    val endpoint = "https://android.googleapis.com/gcm/send/fIYEoSib764:APA91bGLILlBB9XnndQC-fWWM1D-Ji2reiVnRS-sM_kfHQyVssWadi6XRCfd9Dxf74fL6y3-Zaazohhl_W4MCLaqhdr5-WucacYjQS6B5-VyOwYQxzEkU2QABvUUxBcZw91SHYDGmkIt"
    // Base64 string user public key/auth
    val encodedUserPublicKey = "BA7JhUzMirCMHC94XO4ODFb7sYzZPMERp2AFfHLs1Hi1ghdvUfid8dlNseAsXD7LAF+J33X+ViRJ/APpW8cnrko="
    val encodedUserAuth = "8wtwPHBdZ7LWY4p4WWJIzA=="
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth  = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification = new Notification(endpoint, userPublicKey, userAuth, "chrome".getBytes)
    // Construct push service
    val pushService = new PushService
    pushService.setGcmApiKey("AIzaSyDSa2bw0b0UGOmkZRw-dqHGQRI_JqpiHug")
    // Send notification!
    val httpResponse = pushService.send(notification)
    println(httpResponse.getStatusLine.getStatusCode)
    println(IOUtils.toString(httpResponse.getEntity.getContent, StandardCharsets.UTF_8))
  }

}