package nl.martijndwars.webpush

import com.google.common.io.BaseEncoding
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import org.asynchttpclient._
import scala.concurrent.{Future, Promise}
import java.security._
import scala.util.control.NonFatal

object WebpushService {
  /**
    * Encrypt the payload using the user's public key using Elliptic Curve
    * Diffie Hellman cryptography over the prime256v1 curve.
    *
    * @return An Encrypted object containing the public key, salt, and
    *         ciphertext, which can be sent to the other party.
    */
  def encrypt(buffer: Array[Byte], userPublicKey: PublicKey, userAuth: Array[Byte]): Encrypted = {
    val parameterSpec = ECNamedCurveTable.getParameterSpec("prime256v1")
    val keyPairGenerator = KeyPairGenerator.getInstance("ECDH", "BC")
    keyPairGenerator.initialize(parameterSpec)
    val serverKey = keyPairGenerator.generateKeyPair
    val keys = Map("server-key-id" -> serverKey)
    val labels = Map("server-key-id" -> "P-256")
    val salt = SecureRandom.getSeed(16)
    val httpEce = new HttpEce(keys, labels)
    val ciphertext = httpEce.encrypt(buffer, salt, null, "server-key-id", userPublicKey, userAuth)
    new Encrypted(publicKey = serverKey.getPublic, salt = salt, ciphertext = ciphertext)
  }

  def create(client: AsyncHttpClient): WebpushService =
    new WebpushServiceImpl(client)
}
trait WebpushService extends AutoCloseable{
  def send(notification: Notification, publicKey: PublicKey, privateKey: Key, subject: Option[String] = None): Future[Response]
}

final class WebpushServiceImpl(client: AsyncHttpClient) extends WebpushService{

  def send(notification: Notification, publicKey: PublicKey, privateKey: Key, subject: Option[String] = None): Future[Response] = {
    val promise = Promise[Response]()

    try {
      val base64url = BaseEncoding.base64Url
      val encrypted = WebpushService.encrypt(
        buffer = notification.payload,
        userPublicKey = notification.userPublicKey,
        userAuth = notification.userAuth
      )
      val dh = Utils.savePublicKey(encrypted.publicKey.asInstanceOf[ECPublicKey])
      val salt = encrypted.salt.value
      val pk = Utils.savePublicKey(publicKey.asInstanceOf[ECPublicKey])

      val builder = client.preparePost(notification.endpoint)
      builder.addHeader("TTL", String.valueOf(notification.ttl))
      if (notification.hasPayload) {
        builder.addHeader("Content-Type", "application/octet-stream")
        builder.addHeader("Content-Encoding", "aesgcm")
        builder.addHeader("Encryption", "keyid=p256dh;salt=" + base64url.omitPadding.encode(salt))
        val cryptoKey = "keyid=p256dh;dh=" + base64url.encode(dh) + ";p256ecdsa=" + base64url.omitPadding.encode(pk)
        builder.addHeader("Crypto-Key", cryptoKey)
        builder.setBody(encrypted.ciphertext.value)
      } else {
        builder.addHeader("Crypto-Key", "p256ecdsa=" + base64url.encode(pk))
      }

      val claims = new JwtClaims
      claims.setAudience(notification.getOrigin)
      claims.setExpirationTimeMinutesInTheFuture(12 * 60)
      claims.setSubject(subject.orNull)
      val jws = new JsonWebSignature
      jws.setHeader("typ", "JWT")
      jws.setHeader("alg", "ES256")
      jws.setPayload(claims.toJson)
      jws.setKey(privateKey)
      jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256)
      builder.addHeader("Authorization", "Bearer " + jws.getCompactSerialization)

      builder.execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response): Unit = {
          promise.success(response)
        }
        override def onThrowable(t: Throwable): Unit = {
          promise.failure(t)
        }
      })
    }catch {
      case NonFatal(e) =>
        promise.failure(e)
    }

    promise.future
  }

  override def close(): Unit = client.close()
}
