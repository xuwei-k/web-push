package nl.martijndwars.webpush

import com.google.common.io.BaseEncoding
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.jose4j.jws.AlgorithmIdentifiers
import org.jose4j.jws.JsonWebSignature
import org.jose4j.jwt.JwtClaims
import java.security._

object PushService {
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
}

final class PushService {
  /**
    * The Google Cloud Messaging API key (for pre-VAPID in Chrome)
    */
  private[this] var gcmApiKey: String = null
  /**
    * Subject used in the JWT payload (for VAPID)
    */
  private[this] var subject: String = null
  /**
    * The public key (for VAPID)
    */
  private[this] var publicKey: PublicKey = null
  /**
    * The private key (for VAPID)
    */
  private[this] var privateKey: Key = null

  /**
    * Send a notification
    */
  def send(notification: Notification): HttpResponse = {
    val base64url = BaseEncoding.base64Url
    val encrypted: Encrypted = PushService.encrypt(
      buffer = notification.payload,
      userPublicKey =  notification.userPublicKey,
      userAuth = notification.userAuth
    )
    val dh = Utils.savePublicKey(encrypted.publicKey.asInstanceOf[ECPublicKey])
    val salt = encrypted.salt.value
    val httpPost = new HttpPost(notification.endpoint)
    httpPost.addHeader("TTL", String.valueOf(notification.ttl))
    val headers = new java.util.HashMap[String, String]
    if (notification.hasPayload) {
      headers.put("Content-Type", "application/octet-stream")
      headers.put("Content-Encoding", "aesgcm")
      headers.put("Encryption", "keyid=p256dh;salt=" + base64url.omitPadding.encode(salt))
      headers.put("Crypto-Key", "keyid=p256dh;dh=" + base64url.encode(dh))
      httpPost.setEntity(new ByteArrayEntity(encrypted.ciphertext.value))
    }
    if (notification.isGcm) {
      if (gcmApiKey == null) {
        throw new IllegalStateException("An GCM API key is needed to send a push notification to a GCM endpint.")
      }
      headers.put("Authorization", "key=" + gcmApiKey)
    }
    if (vapidEnabled && !notification.isGcm) {
      val claims = new JwtClaims
      claims.setAudience(notification.getOrigin)
      claims.setExpirationTimeMinutesInTheFuture(12 * 60)
      claims.setSubject(subject)
      val jws = new JsonWebSignature
      jws.setHeader("typ", "JWT")
      jws.setHeader("alg", "ES256")
      jws.setPayload(claims.toJson)
      jws.setKey(privateKey)
      jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256)
      headers.put("Authorization", "Bearer " + jws.getCompactSerialization)
      val pk = Utils.savePublicKey(publicKey.asInstanceOf[ECPublicKey])
      if (headers.containsKey("Crypto-Key")) {
        headers.put("Crypto-Key", headers.get("Crypto-Key") + ";p256ecdsa=" + base64url.omitPadding.encode(pk))
      }
      else {
        headers.put("Crypto-Key", "p256ecdsa=" + base64url.encode(pk))
      }
    }
    import scala.collection.JavaConverters._
    for (entry <- headers.entrySet.asScala) {
      httpPost.addHeader(new BasicHeader(entry.getKey, entry.getValue))
    }
    val httpClient = HttpClients.createDefault
    httpClient.execute(httpPost)
  }

  /** Set the Google Cloud Messaging (GCM) API key
    */
  def setGcmApiKey(gcmApiKey: String): this.type = {
    this.gcmApiKey = gcmApiKey
    this
  }

  /** Set the JWT subject (for VAPID) */
  def setSubject(subject: String): this.type = {
    this.subject = subject
    this
  }

  /** Set the public key (for VAPID) */
  def setPublicKey(publicKey: PublicKey): this.type = {
    this.publicKey = publicKey
    this
  }

  /** Set the private key (for VAPID) */
  def setPrivateKey(privateKey: PrivateKey): this.type = {
    this.privateKey = privateKey
    this
  }

  /** Check if VAPID is enabled */
  private[this] def vapidEnabled: Boolean = {
    publicKey != null && privateKey != null
  }
}