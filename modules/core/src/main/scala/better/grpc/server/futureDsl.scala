package better.grpc.server

import better.grpc.common.EffectLike
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import scala.util._
import scala.concurrent.Promise

class futureDsl(ec: ExecutionContext) {

  implicit val effectLikeFuture: EffectLike[Future] = {
    implicit val eci = ec
    new EffectLike[Future] {
      def pure[A](a: A): Future[A] = Future.successful(a)
      def raiseError[A](e: Throwable): Future[A] = Future.failed(e)
      def map[A, B](fa: Future[A])(f: A => B): Future[B] = fa.map(f)
      def flatMap[A, B](fa: Future[A])(f: A => Future[B]): Future[B] = fa.flatMap(f)
      def attempt[A](fa: Future[A]): Future[Either[Throwable, A]] =
        fa.transformWith(t => {
          val e = t match {
            case Failure(err) => Left(err)
            case Success(v) => Right(v)
          }
          Future.successful(e)
        })
      def run[A](fa: Future[A]): Unit = ()
      def asyncCompleter[A]: (Future[A], Either[Throwable, A] => Unit) = {
        val ps = Promise[A]
        val completer: Either[Throwable, A] => Unit = (v) => {
          v match {
            case Left(err) => ps.failure(err)
            case Right(value) => ps.success(value)
          }
        }
        (ps.future, completer)
      }
    }
  }

  object method extends MethodDef[Future]
  object service extends ServiceDef[Future]

}

object futureDsl {

  def apply(ec: ExecutionContext): futureDsl = new futureDsl(ec)
}