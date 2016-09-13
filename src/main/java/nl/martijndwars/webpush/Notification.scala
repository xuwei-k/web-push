package nl.martijndwars.webpush

import java.net.MalformedURLException
import java.net.URL
import java.security.PublicKey

/**
  * @param endpoint The endpoint associated with the push subscription
  * @param userPublicKey The client's public key
  * @param userAuth The client's auth
  * @param payload An arbitrary payload
  * @param ttl Time in seconds that the push message is retained by the push service
  */
final case class Notification(
  endpoint: String,
  userPublicKey: PublicKey,
  userAuth: Array[Byte],
  payload: Array[Byte],
  ttl: Int
) {
  def this(endpoint: String, userPublicKey: PublicKey, userAuth: Array[Byte], payload: Array[Byte]) {
    this(endpoint, userPublicKey, userAuth, payload, 2419200)
  }

  def hasPayload: Boolean = payload.length > 0

  @throws[MalformedURLException]
  def getOrigin: String = {
    val url = new URL(endpoint)
    url.getProtocol + "://" + url.getHost
  }
}