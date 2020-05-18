package better.grpc.server

import better.grpc.common._
import io.grpc.MethodDescriptor

case class RpcMethod[F[_], In, Out, Ctx](
  descriptor: MethodDescriptor[In, Out],
  f: (Request[In], Ctx) => F[Response[Out]]
)

object RpcMethod {

  def plain[F[_], In, Out](d: MethodDescriptor[In, Out], f: Request[In] => F[Response[Out]]): RpcMethod[F, In, Out, Any] =
    RpcMethod(d, (in, ctx) => f(in))

  def withContext[F[_], In, Out, Ctx](d: MethodDescriptor[In, Out], f: (Request[In], Ctx) => F[Response[Out]]): RpcMethod[F, In, Out, Ctx] =
    RpcMethod(d, (in, ctx) => f(in, ctx))

}