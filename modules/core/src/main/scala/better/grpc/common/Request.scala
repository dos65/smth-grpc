package better.grpc.common

case class Request[A](
  payload: A,
  headers: Headers
)

object Request {

  def apply[A](a: A): Request[A] = Request(a, Headers.Empty)
}