import cats._
import cats.implicits._
import cats.effect._
import example.example.FooReq
import example.example.FooResp
import scala.concurrent.Future
import io.grpc.Status
import io.grpc.ServerBuilder
import io.grpc.ServerServiceDefinition
import better.grpc.server.BeforeCall
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import better.grpc.common.Request
import better.grpc.common.Headers
import better.grpc.common.Response

object ServerFuture extends IOApp {


  object examples {

    import example.example.ExampleServiceGrpc
    import scala.concurrent.ExecutionContext

    val dsl = better.grpc.catsEffectIODsl.create
    import dsl._

    val getFoo = method(ExampleServiceGrpc.METHOD_GET_FOO)(req => {
      IO.pure(Response.Ok(FooResp()))
    })

    case class AuthContext(token: String)
    val getFoo2 = method.withContext[AuthContext](ExampleServiceGrpc.METHOD_GET_FOO)((req, ctx) => {
      println(s"GET FOO $ctx ${req.headers}")
      val rsp = Response.Ok(FooResp(), Headers("X-Test" -> "hoho"))
      IO.pure(rsp)
    })

    private def authorize(i: BeforeCall): IO[Either[Response.Error, AuthContext]] = {
      i.metadata.get("Authorization") match {
        case None =>
          val err = Response.Unauthenticated()
          IO.pure(err.asLeft)
        case Some(token) =>
          val ctx = AuthContext(token)
          IO.pure(ctx.asRight)
      }
    }

    def mkService: ServerServiceDefinition = {
      service.withBefore(ExampleServiceGrpc.SERVICE)(authorize)
      .add(getFoo2)
      .toDefinition
    } 
  }

  override def run(args: List[String]): IO[ExitCode] = {
    // TODO
    val f = IO {
      val server = ServerBuilder.forPort(8080)
        .addService(examples.mkService)
        .build()
        .start()

      sys.addShutdownHook(server.shutdown())  
      server.awaitTermination  
    }
    f.as(ExitCode.Success)
  }



}

object ClientE extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val chBuilder = ManagedChannelBuilder.forAddress("localhost", 8080)
    chBuilder.usePlaintext()
    val channel = chBuilder.build()

    import example.example.ExampleServiceGrpc

    import scala.concurrent.ExecutionContext
    import scala.concurrent.ExecutionContext.Implicits.global

    val dsl = better.grpc.catsEffectIODsl.create
    import dsl._

    val client = better.grpc.Client.create[IO](channel)

    val f = for {
      rsp <- client.call(ExampleServiceGrpc.METHOD_GET_FOO)(Request(FooReq(), Headers(("Authorization", "yoyo"))))
      _ <- IO(println(rsp))
    } yield ()
    f.as(ExitCode.Success)
  }

}
