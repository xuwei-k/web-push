package nl.martijndwars.webpush

import com.google.common.io.BaseEncoding
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.interfaces.ECPrivateKey
import org.bouncycastle.jce.interfaces.ECPublicKey
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPrivateKeySpec
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.math.BigInteger
import java.security._
import java.security.spec.InvalidKeySpecException

object Utils {
  def savePublicKey(publicKey: ECPublicKey): Array[Byte] = {
    publicKey.getQ.getEncoded(false)
  }

  def savePrivateKey(privateKey: ECPrivateKey): Array[Byte] = {
    privateKey.getD.toByteArray
  }

  /** Base64-decode a string. Works for both url-safe and non-url-safe encodings.
    */
  def base64Decode(base64Encoded: String): Array[Byte] = {
    if (base64Encoded.contains("+") || base64Encoded.contains("/")) {
      BaseEncoding.base64.decode(base64Encoded)
    }
    else {
      BaseEncoding.base64Url.decode(base64Encoded)
    }
  }

  /**
    * Load the public key from a URL-safe base64 encoded string. Takes into
    * account the different encodings, including point compression.
    */
  @throws[NoSuchProviderException]
  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def loadPublicKey(encodedPublicKey: String): PublicKey = {
    val decodedPublicKey = base64Decode(encodedPublicKey)
    val kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
    // prime256v1 is NIST P-256
    val ecSpec = ECNamedCurveTable.getParameterSpec("prime256v1")
    val point = ecSpec.getCurve.decodePoint(decodedPublicKey)
    val pubSpec = new ECPublicKeySpec(point, ecSpec)
    kf.generatePublic(pubSpec)
  }

  /** Load the private key from a URL-safe base64 encoded string */
  @throws[NoSuchProviderException]
  @throws[NoSuchAlgorithmException]
  @throws[InvalidKeySpecException]
  def loadPrivateKey(encodedPrivateKey: String): PrivateKey = {
    val decodedPrivateKey: Array[Byte] = base64Decode(encodedPrivateKey)
    // prime256v1 is NIST P-256
    val params = ECNamedCurveTable.getParameterSpec("prime256v1")
    val prvkey = new ECPrivateKeySpec(new BigInteger(decodedPrivateKey), params)
    val kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
    kf.generatePrivate(prvkey)
  }
}