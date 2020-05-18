package better.grpc.server

import better.grpc.common.Headers 
import io.grpc.MethodDescriptor

case class BeforeCall(
  metadata: Headers,
  descriptor: MethodDescriptor[_, _]
)