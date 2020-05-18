package better.grpc.common

trait EffectLike[F[_]] {
  def pure[A](a: A): F[A]
  def raiseError[A](e: Throwable): F[A]
  def map[A, B](fa: F[A])(f: A => B): F[B]
  def flatMap[A, B](fa: F[A])(f: A => F[B]): F[B]
  def attempt[A](fa: F[A]): F[Either[Throwable, A]]
  def run[A](fa: F[A]): Unit
  def asyncCompleter[A]: (F[A], Either[Throwable, A] => Unit)
}

object EffectLike {
  def apply[F[_]](implicit el: EffectLike[F]): EffectLike[F] = el
}

object EffectLikeSyntax {

  implicit class Syntax[F[_], A](val fa: F[A]) extends AnyVal {
    def map[B](f: A => B)(implicit El: EffectLike[F]): F[B] = El.map(fa)(f)
    def flatMap[B](f: A => F[B])(implicit El: EffectLike[F]): F[B] = El.flatMap(fa)(f)
    def attempt(implicit El: EffectLike[F]): F[Either[Throwable, A]] = El.attempt(fa)
    def run(implicit El: EffectLike[F]): Unit = El.run(fa)
  }
}
