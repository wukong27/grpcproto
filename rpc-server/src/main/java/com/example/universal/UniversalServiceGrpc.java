package com.example.universal;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.59.0)",
    comments = "Source: universal.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class UniversalServiceGrpc {

  private UniversalServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "UniversalService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.universal.Universal.CallRequest,
      com.example.universal.Universal.CallResponse> getInvokeMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Invoke",
      requestType = com.example.universal.Universal.CallRequest.class,
      responseType = com.example.universal.Universal.CallResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.universal.Universal.CallRequest,
      com.example.universal.Universal.CallResponse> getInvokeMethod() {
    io.grpc.MethodDescriptor<com.example.universal.Universal.CallRequest, com.example.universal.Universal.CallResponse> getInvokeMethod;
    if ((getInvokeMethod = UniversalServiceGrpc.getInvokeMethod) == null) {
      synchronized (UniversalServiceGrpc.class) {
        if ((getInvokeMethod = UniversalServiceGrpc.getInvokeMethod) == null) {
          UniversalServiceGrpc.getInvokeMethod = getInvokeMethod =
              io.grpc.MethodDescriptor.<com.example.universal.Universal.CallRequest, com.example.universal.Universal.CallResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Invoke"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.universal.Universal.CallRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.universal.Universal.CallResponse.getDefaultInstance()))
              .setSchemaDescriptor(new UniversalServiceMethodDescriptorSupplier("Invoke"))
              .build();
        }
      }
    }
    return getInvokeMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static UniversalServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UniversalServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UniversalServiceStub>() {
        @java.lang.Override
        public UniversalServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UniversalServiceStub(channel, callOptions);
        }
      };
    return UniversalServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static UniversalServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UniversalServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UniversalServiceBlockingStub>() {
        @java.lang.Override
        public UniversalServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UniversalServiceBlockingStub(channel, callOptions);
        }
      };
    return UniversalServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static UniversalServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<UniversalServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<UniversalServiceFutureStub>() {
        @java.lang.Override
        public UniversalServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new UniversalServiceFutureStub(channel, callOptions);
        }
      };
    return UniversalServiceFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     */
    default void invoke(com.example.universal.Universal.CallRequest request,
        io.grpc.stub.StreamObserver<com.example.universal.Universal.CallResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInvokeMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service UniversalService.
   */
  public static abstract class UniversalServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return UniversalServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service UniversalService.
   */
  public static final class UniversalServiceStub
      extends io.grpc.stub.AbstractAsyncStub<UniversalServiceStub> {
    private UniversalServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UniversalServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UniversalServiceStub(channel, callOptions);
    }

    /**
     */
    public void invoke(com.example.universal.Universal.CallRequest request,
        io.grpc.stub.StreamObserver<com.example.universal.Universal.CallResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInvokeMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service UniversalService.
   */
  public static final class UniversalServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<UniversalServiceBlockingStub> {
    private UniversalServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UniversalServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UniversalServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.example.universal.Universal.CallResponse invoke(com.example.universal.Universal.CallRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInvokeMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service UniversalService.
   */
  public static final class UniversalServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<UniversalServiceFutureStub> {
    private UniversalServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected UniversalServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new UniversalServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.universal.Universal.CallResponse> invoke(
        com.example.universal.Universal.CallRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInvokeMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_INVOKE = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_INVOKE:
          serviceImpl.invoke((com.example.universal.Universal.CallRequest) request,
              (io.grpc.stub.StreamObserver<com.example.universal.Universal.CallResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getInvokeMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.universal.Universal.CallRequest,
              com.example.universal.Universal.CallResponse>(
                service, METHODID_INVOKE)))
        .build();
  }

  private static abstract class UniversalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    UniversalServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.universal.Universal.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("UniversalService");
    }
  }

  private static final class UniversalServiceFileDescriptorSupplier
      extends UniversalServiceBaseDescriptorSupplier {
    UniversalServiceFileDescriptorSupplier() {}
  }

  private static final class UniversalServiceMethodDescriptorSupplier
      extends UniversalServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    UniversalServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (UniversalServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new UniversalServiceFileDescriptorSupplier())
              .addMethod(getInvokeMethod())
              .build();
        }
      }
    }
    return result;
  }
}
