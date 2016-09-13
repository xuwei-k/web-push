package nl.martijndwars.webpush

import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters
import org.bouncycastle.jce.interfaces.ECPublicKey
import javax.crypto._
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.io.IOException
import java.nio.ByteBuffer
import java.security._

import HttpEce._

/**
  * An implementation of HTTP ECE (Encrypted Content Encoding) as described in
  * [[https://tools.ietf.org/html/draft-ietf-httpbis-encryption-encoding-01]]
  */
object HttpEce {

  @throws[IOException]
  private def lengthPrefix(key: Key): Array[Byte] = {
    val bytes = Utils.savePublicKey(key.asInstanceOf[ECPublicKey])
    concat(intToBytes(bytes.length), bytes)
  }

  /**
    * Cast an integer to a two-byte array
    */
  private[this] def intToBytes(x: Int): Array[Byte] = {
    val bytes = new Array[Byte](2)
    bytes(1) = (x & 0xff).toByte
    bytes(0) = (x >> 8).toByte
    bytes
  }

  /**
    * Utility to concat byte arrays
    */
  private def concat(arrays: Array[Byte]*): Array[Byte] = {
    var lastPos = 0
    val combined = new Array[Byte](arrays.foldLeft(0)(_ + _.length))
    var i = 0
    while(i < arrays.length) {
      val array = arrays(i)
      System.arraycopy(array, 0, combined, lastPos, array.length)
      lastPos += array.length
      i += 1
    }
    combined
  }



  /**
    * Future versions might require a null-terminated info string?
    */
  private def buildInfo(`type`: String, context: Array[Byte]): Array[Byte] = {
    val buffer: ByteBuffer = ByteBuffer.allocate(19 + `type`.length + context.length)
    buffer.put("Content-Encoding: ".getBytes, 0, 18)
    buffer.put(`type`.getBytes, 0, `type`.length)
    buffer.put(new Array[Byte](1), 0, 1)
    buffer.put(context, 0, context.length)
    buffer.array
  }

  /**
    * Convenience method for computing the HMAC Key Derivation Function. The
    * real work is offloaded to BouncyCastle.
    */
  @throws[InvalidKeyException]
  @throws[NoSuchAlgorithmException]
  private def hkdfExpand(ikm: Array[Byte], salt: Array[Byte], info: Array[Byte], length: Int): Array[Byte] = {
    val hkdf = new HKDFBytesGenerator(new SHA256Digest)
    hkdf.init(new HKDFParameters(ikm, salt, info))
    val okm = new Array[Byte](length)
    hkdf.generateBytes(okm, 0, length)
    okm
  }
}

final case class HttpEce(keys: Map[String, KeyPair], labels: Map[String, String]) {

  def deriveKey(salt: Array[Byte], key: Array[Byte], keyId: String, dh: PublicKey, authSecret: Array[Byte], padSize: Int): (Array[Byte], Array[Byte]) = {
    var secret: Array[Byte] = null
    var context: Array[Byte] = null
    if (key != null) {
      secret = key
    }
    else if (dh != null) {
      val bytes = deriveDH(keyId, dh)
      secret = bytes._1
      context = bytes._2
    }
    else if (keyId != null) {
      secret = keys(keyId).getPublic.getEncoded
    }
    if (secret == null) {
      throw new IllegalStateException("Unable to determine the secret")
    }
    if (authSecret != null) {
      secret = HttpEce.hkdfExpand(secret, authSecret, HttpEce.buildInfo("auth", new Array[Byte](0)), 32)
    }
    val keyinfo = HttpEce.buildInfo("aesgcm", context)
    val nonceinfo = HttpEce.buildInfo("nonce", context)
    val hkdf_key = HttpEce.hkdfExpand(secret, salt, keyinfo, 16)
    val hkdf_nonce = HttpEce.hkdfExpand(secret, salt, nonceinfo, 12)
    (hkdf_key, hkdf_nonce)
  }

  /**
    * Compute the shared secret using the server's key pair (indicated by
    * keyId) and the client's public key. Also compute context.
    */
  private[this] def deriveDH(keyId: String, publicKey: PublicKey): (Array[Byte], Array[Byte]) = {
    val senderPubKey = keys(keyId).getPublic
    val keyAgreement = KeyAgreement.getInstance("ECDH")
    keyAgreement.init(keys(keyId).getPrivate)
    keyAgreement.doPhase(publicKey, true)
    val secret = keyAgreement.generateSecret
    val context = concat(labels(keyId).getBytes, new Array[Byte](1), lengthPrefix(publicKey), lengthPrefix(senderPubKey))
    (secret, context)
  }

  def encrypt(buffer: Array[Byte], salt: Array[Byte], key: Array[Byte], keyid: String, dh: PublicKey, authSecret: Array[Byte], padSize: Int): Array[Byte] = {
    val derivedKey = deriveKey(salt, key, keyid, dh, authSecret, padSize)
    val key_ = derivedKey._1
    val nonce_ = derivedKey._2
    val cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC")
    cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key_, "AES"), new GCMParameterSpec(16 * 8, nonce_))
    cipher.update(new Array[Byte](padSize))
    cipher.doFinal(buffer)
  }
}