package nl.martijndwars.webpush

final case class Bytes(value: Array[Byte]) {
  def ===(that: Bytes): Boolean =
    java.util.Arrays.equals(this.value, that.value)

  override def equals(other: Any): Boolean = other match {
    case that: Bytes =>
      this.===(that)
    case _ =>
      false
  }

  override def hashCode(): Int =
    java.util.Arrays.hashCode(value)
}
