package better.grpc

import better.grpc.common.EffectLike
import better.grpc.server._
import cats.effect.IO
import cats.implicits._
import cats.effect.concurrent.Deferred
import cats.effect.Concurrent

class catsEffectIODsl(c: Concurrent[IO]) {

  implicit val effectLikeFuture: EffectLike[IO] = {
    implicit val cc = c
    new EffectLike[IO] {
      def pure[A](a: A): IO[A] = IO.pure(a)
      def raiseError[A](e: Throwable): IO[A] = IO.raiseError(e)
      def map[A, B](fa: IO[A])(f: A => B): IO[B] = fa.map(f)
      def flatMap[A, B](fa: IO[A])(f: A => IO[B]): IO[B] = fa.flatMap(f)
      def attempt[A](fa: IO[A]): IO[Either[Throwable, A]] = fa.attempt
      def run[A](fa: IO[A]): Unit = fa.unsafeRunSync()
      def asyncCompleter[A]: (IO[A], Either[Throwable, A] => Unit) = {
        val defferred = Deferred.unsafe[IO, Either[Throwable, A]]
        val completer: Either[Throwable, A] => Unit = (v) => {
          defferred.complete(v).unsafeRunAsyncAndForget()
        }
        (defferred.get.rethrow, completer)
      }
    }
  }

  object method extends MethodDef[IO]
  object service extends ServiceDef[IO]
}

object catsEffectIODsl {

  def create(implicit c: Concurrent[IO]): catsEffectIODsl = new catsEffectIODsl(c)
}