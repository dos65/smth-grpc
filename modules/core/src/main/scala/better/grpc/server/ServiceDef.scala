package better.grpc.server

import io.grpc.ServiceDescriptor
import better.grpc.common._

class ServiceDef[F[+_]: EffectLike] {

  def basic(d: ServiceDescriptor): ServiceBuilder[F, Any] =
    ServiceBuilder.basic(d)

  def withBefore[Ctx](d: ServiceDescriptor)(f: BeforeCall => F[Either[Response.Error, Ctx]]): ServiceBuilder[F, Ctx] =
    ServiceBuilder.withBefore(d)(f)
}