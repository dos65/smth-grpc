package better.grpc.server

import better.grpc.common._
import io.grpc.MethodDescriptor
import better.grpc.server.MethodDef.CtxPartiallyApplied

class MethodDef[F[_]] {

  def apply[In, Out](d: MethodDescriptor[In, Out])(f: Request[In] => F[Response[Out]]): RpcMethod[F, In, Out, Any] =
    RpcMethod(d, (in, c) => f(in))

  def withContext[Ctx]: CtxPartiallyApplied[F, Ctx] = new CtxPartiallyApplied[F, Ctx]

}

object MethodDef {

  class CtxPartiallyApplied[F[_], Ctx] {
    def apply[In, Out](d: MethodDescriptor[In, Out])(f: (Request[In], Ctx) => F[Response[Out]]): RpcMethod[F, In, Out, Ctx] =
      RpcMethod(d, (in, ctx) => f(in, ctx))

  }

  def apply[F[_]]: MethodDef[F] = new MethodDef[F]
}