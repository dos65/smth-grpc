package better.grpc.client

import better.grpc.common.Headers

case class ClientRequest[A](
  payload: A,
  headers: Headers
)

object ClientRequest {
  def apply[A](a: A): ClientRequest[A] = ClientRequest(a, Headers.Empty)
}