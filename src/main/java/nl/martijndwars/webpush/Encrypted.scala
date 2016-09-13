package nl.martijndwars.webpush

import java.security.PublicKey

final case class Encrypted(publicKey: PublicKey, salt: Bytes, ciphertext: Bytes) {
  def this(publicKey: PublicKey, salt: Array[Byte], ciphertext: Array[Byte]) {
    this(publicKey, Bytes(salt), Bytes(ciphertext))
  }
}

object Encrypted{
}
