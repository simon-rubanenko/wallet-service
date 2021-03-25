package newages.casino.wallet.serializer

import akka.serialization.Serializer

class CirceSerializer extends Serializer {
  override def identifier: Int = ???

  override def toBinary(o: AnyRef): Array[Byte] = ???

  override def includeManifest: Boolean = ???

  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = ???
}
