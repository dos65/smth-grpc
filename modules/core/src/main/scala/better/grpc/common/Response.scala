package better.grpc.common

import io.grpc.Status

sealed trait Response[+A] {
  def status: Status 
  def headers: Headers
}

object Response {

  final case class Success[A](
    payload: A,
    headers: Headers
  ) extends Response[A] {
    def status: Status = Status.OK
  }

  final case class Error(
    status: Status, 
    headers: Headers
  ) extends Response[Nothing]

  private def mkStatus(st: Status, clause: Option[Throwable], description: Option[String]): Status = {
    val addClause = clause.fold(st)(e => st.withCause(e))
    description.fold(addClause)(d => addClause.withDescription(d))
  }
    

  def Ok[A](payload: A, headers: Headers = Headers.Empty): Success[A] = Success(payload, headers)
  def Cancelled(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.CANCELLED, clause, description), headers)
  def Unknown(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.UNKNOWN, clause, description), headers)
  def InvalidArgument(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.INVALID_ARGUMENT, clause, description), headers)
  def DeadlineExceeded(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.DEADLINE_EXCEEDED, clause, description), headers)
  def NotFound(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.NOT_FOUND, clause, description), headers)
  def AlreadyExists(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.ALREADY_EXISTS, clause, description), headers)
  def PermissionDenied(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.PERMISSION_DENIED, clause, description), headers)
  def ResourceExhausted(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.RESOURCE_EXHAUSTED, clause, description), headers)
  def FailedPrecondition(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.FAILED_PRECONDITION, clause, description), headers)
  def Aborted(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.ABORTED, clause, description), headers)
  def OutOfRange(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.OUT_OF_RANGE, clause, description), headers)
  def Unimplemented(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.UNIMPLEMENTED, clause, description), headers)
  def Internal(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.INTERNAL, clause, description), headers)
  def Unavailable(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.UNAVAILABLE, clause, description), headers)
  def DataLoss(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.DATA_LOSS, clause, description), headers)
  def Unauthenticated(clause: Option[Throwable] = None, description: Option[String] = None, headers: Headers = Headers.Empty): Error =
    Error(mkStatus(Status.UNAUTHENTICATED, clause, description), headers)
}