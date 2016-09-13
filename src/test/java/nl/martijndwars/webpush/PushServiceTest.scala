package nl.martijndwars.webpush

import org.apache.commons.io.IOUtils
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.json.JSONObject
import org.junit.BeforeClass
import org.junit.Test
import java.nio.charset.StandardCharsets
import java.security.Security

object PushServiceTest{
  @BeforeClass def addSecurityProvider(): Unit = {
    Security.addProvider(new BouncyCastleProvider)
  }
}

class PushServiceTest {

  @Test
  def testPushFirefoxVapid(): Unit = {
    val endpoint = "https://updates.push.services.mozilla.com/wpush/v1/gAAAAABX1ZgBNvDz6ZIAh6OqNh3hN4ZLEa57oS22mHI70mnvrDbIi-MnJu7FxFzvMV31L_AnIxP_p1Ot47KP8Xmit3XIQjZDjTahqBPmmntWX8JM6AtRxcAHxmXH6KqhyWwL1QEA0jBp"
    // Base64 string user public key/auth
    val encodedUserPublicKey = "BLLgHYo0xlN3GDSrz4g6SpTDLvJv+oFR0FSLLnncXFojvVyoOePpNXaUpsj4s/huAX7zb+qS1Lxo6qNLXNgWN7k="
    val encodedUserAuth = "wkbtrbgITbb9qPBVOw3ftw=="
    // Base64 string server public/private key
    val vapidPublicKey = "BOH8nTQA5iZhl23+NCzGG9prvOZ5BE0MJXBW+GUkQIvRVTVB32JxmX0V1j6z0r7rnT7+bgi6f2g5fMPpAh5brqM="
    val vapidPrivateKey = "TRlY/7yQzvqcLpgHQTxiU5fVzAAvAw/cdSh5kLFLNqg="
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification = new Notification(endpoint, userPublicKey, userAuth, getPayload)
    // Construct push service
    val pushService = new PushService
    pushService.setSubject("mailto:admin@martijndwars.nl")
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
    val endpoint = "https://fcm.googleapis.com/fcm/send/fAAs_rrnDHQ:APA91bHlqjMZzphwP2xckJa9jL0CwtEvlLTL1OEfmRuwqviGLnqQTvMr4WLiwg7jElESXPLYO7qUc5mWvvv-bqs9lRenEbUSL2R191F-quyhE_fZ6JM3giqMQMhAEifDG-s5eHsRPQUG"
    // Base64 string user public key/auth
    val encodedUserPublicKey = "BM9qL254VsQlM8Zi6Hd0khUYSn8075A+td+/DZELdA2L173DIDz42NbjZC51NRfAuVaxh/vT/+UZr37S55EtY7k="
    val encodedUserAuth = "KaiGaQKMyCW8qEk2NMJwjA=="
    // Base64 string server public/private key
    val vapidPublicKey = "BOH8nTQA5iZhl23+NCzGG9prvOZ5BE0MJXBW+GUkQIvRVTVB32JxmX0V1j6z0r7rnT7+bgi6f2g5fMPpAh5brqM="
    val vapidPrivateKey = "TRlY/7yQzvqcLpgHQTxiU5fVzAAvAw/cdSh5kLFLNqg="
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification = new Notification(endpoint, userPublicKey, userAuth, getPayload)
    // Construct push service
    val pushService = new PushService
    pushService.setSubject("mailto:admin@martijndwars.nl")
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
    val endpoint = "https://updates.push.services.mozilla.com/wpush/v1/gAAAAABX1Y_lvdzIpzBfRnceQdoNa_DiDy2OH7weXClk5ysidEuoPH8xv0Qq9ADFNTAB4e1TOuT50bbpN-bWVymBqy1b6Mecrz_SHf8Hvh620ViAbL5Zuyp5AqlA7i6g4BGX8h1H23zH"
    // Base64 string user public key/auth
    val encodedUserPublicKey = "BNYbTpyTEUFNK9BacT1rgpx7SXuKkLVKOF0LFnK8mLyPeW3SLk3nmXoPXSCkNKovcKChNxbG+q3mGW9J8JRg+6w="
    val encodedUserAuth = "40SZaWpcvu55C+mlWxu0kA=="
    // Converting to other data types...
    val userPublicKey = Utils.loadPublicKey(encodedUserPublicKey)
    val userAuth = Utils.base64Decode(encodedUserAuth)
    // Construct notification
    val notification: Notification = new Notification(endpoint, userPublicKey, userAuth, getPayload)
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
    val notification = new Notification(endpoint, userPublicKey, userAuth, getPayload)
    // Construct push service
    val pushService = new PushService
    pushService.setGcmApiKey("AIzaSyDSa2bw0b0UGOmkZRw-dqHGQRI_JqpiHug")
    // Send notification!
    val httpResponse = pushService.send(notification)
    println(httpResponse.getStatusLine.getStatusCode)
    println(IOUtils.toString(httpResponse.getEntity.getContent, StandardCharsets.UTF_8))
  }

  @Test
  def testSign(): Unit = {
    // Base64 string server public/private key
    val vapidPublicKey = "BOH8nTQA5iZhl23+NCzGG9prvOZ5BE0MJXBW+GUkQIvRVTVB32JxmX0V1j6z0r7rnT7+bgi6f2g5fMPpAh5brqM="
    val vapidPrivateKey = "TRlY/7yQzvqcLpgHQTxiU5fVzAAvAw/cdSh5kLFLNqg="
    val claims = new JwtClaims
    claims.setAudience("https://developer.services.mozilla.com/a476b8ea-c4b8-4359-832a-e2747b6ab88a")
    val jws = new JsonWebSignature
    jws.setPayload(claims.toJson)
    jws.setKey(Utils.loadPrivateKey(vapidPrivateKey))
    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256)
    println(jws.getCompactSerialization)
  }

  /**
    * Some dummy payload (a JSON object)
    */
  private def getPayload: Array[Byte] = {
    val jsonObject = new JSONObject
    jsonObject.append("title", "Hello")
    jsonObject.append("message", "World")
    jsonObject.toString.getBytes(StandardCharsets.UTF_8)
  }
}