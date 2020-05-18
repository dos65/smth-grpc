package better.grpc.server

import io.grpc.MethodDescriptor
import io.grpc.ServerMethodDefinition
import io.grpc.ServerServiceDefinition
import io.grpc.ServiceDescriptor
import io.grpc.ServerCallHandler
import io.grpc.ServerCall
import io.grpc.ServerCall.Listener

import better.grpc.common._
import io.grpc.Metadata

class ServiceBuilder[F[+_], Ctx](
  descriptor: ServiceDescriptor,
  methods: List[ServerMethodDefinition[_, _]],
  before: BeforeCall => F[Either[Response.Error, Ctx]],
  // after: AfterCall => F[AfterCall]
)(implicit effectLike: EffectLike[F]) {

  def add[In, Out, Ctx2 >: Ctx](method: RpcMethod[F, In, Out, Ctx2]):
    ServiceBuilder[F, Ctx] = {
      val converted = ServiceBuilder.grpc.convertMethod(before, method)
      new ServiceBuilder[F, Ctx](descriptor, converted :: methods, before)
    }

  def toDefinition: ServerServiceDefinition = {
    val builder = ServerServiceDefinition.builder(descriptor)
    val withMethods = methods.foldLeft(builder)({case (b, m) => b.addMethod(m)})
    withMethods.build()
  }  
 
}

object ServiceBuilder {

  def basic[F[+_]: EffectLike](d: ServiceDescriptor): ServiceBuilder[F, Any] =
    new ServiceBuilder[F, Any](d, List.empty, _ => EffectLike[F].pure(Right(())))

  def withBefore[F[+_]: EffectLike, Ctx](d: ServiceDescriptor)(f: BeforeCall => F[Either[Response.Error, Ctx]]): ServiceBuilder[F, Ctx] =
    new ServiceBuilder[F, Ctx](d, List.empty, f)

  object grpc {

    import EffectLikeSyntax._

    def convertMethod[F[+_], In, Out, Ctx, Ctx2 >: Ctx](
      before: BeforeCall => F[Either[Response.Error, Ctx]],
      method: RpcMethod[F, In, Out, Ctx2]
    )(implicit EL: EffectLike[F]): ServerMethodDefinition[In, Out] = {

      ServerMethodDefinition.create(
        method.descriptor,
        new ServerCallHandler[In, Out] {
          override def startCall(call: ServerCall[In,Out], headers: io.grpc.Metadata): Listener[In] = {
            call.request(1)
            new ServerCall.Listener[In] {
              override def onMessage(message: In): Unit = {
                val d = call.getMethodDescriptor()
                val betterHeaders = Headers.fromGrpc(headers)
                val beforeIn = BeforeCall(betterHeaders, d)

                val f = before(beforeIn).flatMap({
                  case Left(errRsp) => EL.pure(errRsp)
                  case Right(out) =>
                    val req = Request(message, betterHeaders)
                    method.f(req, out)
                }).map({
                  case Response.Error(st, outHeaders) =>
                    call.close(st, outHeaders.toGrpc)
                  case Response.Success(payload, outHeaders) =>
                    // TODO sendHeadred/trailers ?? how to send headers properly?
                    call.sendHeaders(new Metadata)
                    call.sendMessage(payload)
                    call.close(io.grpc.Status.OK, outHeaders.toGrpc)
                })
                f.run
              }
            }
          }
        }
      )
    }
  }  
}