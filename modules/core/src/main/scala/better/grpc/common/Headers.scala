package better.grpc.common

import scala.collection.JavaConverters._

trait Headers {
  def get(key: String): Option[String]
  def keys: List[String]
  def toGrpc: io.grpc.Metadata
}

object Headers {

  val Empty = Headers()

  def fromGrpc(underlying: io.grpc.Metadata): Headers = {
    new Headers {
      def get(key: String): Option[String] = {
        val grpcKey = io.grpc.Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER)
        Option(underlying.get(grpcKey))
      }
      def keys: List[String] = underlying.keys().asScala.toList
      def toGrpc: io.grpc.Metadata = underlying
      override def toString(): String = {
        keys.flatMap(k => get(k).map(v => s"$k=$v")).mkString("Headers(", ", ", ")")
      }
    }

  }

  def apply(kv: (String, String)*): Headers = {
    val meta = new io.grpc.Metadata()
    kv.foreach({case (key, v) => {
      val grpcKey = io.grpc.Metadata.Key.of(key, io.grpc.Metadata.ASCII_STRING_MARSHALLER)
      meta.put(grpcKey, v)
    }})
    fromGrpc(meta)
  }
}
