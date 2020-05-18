package better.grpc

import io.grpc.MethodDescriptor
import io.grpc.Channel
import io.grpc.CallOptions
import better.grpc.common.EffectLike
import better.grpc.common._
import io.grpc.ClientCall
import io.grpc.Metadata
import io.grpc.Status

trait Client[F[_]] {
  def call[In, Out](descriptor: MethodDescriptor[In, Out])(req: Request[In]): F[Response[Out]]
}

object Client {

  import EffectLikeSyntax._  

  def create[F[_]: EffectLike](channel: Channel): Client[F] = {
    new Client[F] {

      override def call[In, Out](descriptor: MethodDescriptor[In,Out])(req: Request[In]): F[Response[Out]] = {
        val call = channel.newCall(descriptor, CallOptions.DEFAULT)
        val (mAsync, mCompleter) = EffectLike[F].asyncCompleter[Out]
        val (stAsync, stCompleter) = EffectLike[F].asyncCompleter[(Status, Metadata)]

        val listener = new ClientCall.Listener[Out] {
          override def onMessage(message: Out): Unit = {
            mCompleter(Right(message))
          }
          override def onHeaders(headers: Metadata): Unit = {

          }
          override def onClose(status: Status, trailers: Metadata): Unit = {
            stCompleter(Right((status, trailers)))
          }
        }
        call.start(listener, req.headers.toGrpc)
        call.request(1)
        call.sendMessage(req.payload)
        call.halfClose()

        stAsync.flatMap({case (st, meta) => 
          if (st == Status.OK) {
            mAsync.map(out => Response.Ok(out, Headers.fromGrpc(meta)))
          } else {
            val x = Response.Error(st, Headers.fromGrpc(meta))
            EffectLike[F].pure(x)
          }
        })
      }

    }
  }
}