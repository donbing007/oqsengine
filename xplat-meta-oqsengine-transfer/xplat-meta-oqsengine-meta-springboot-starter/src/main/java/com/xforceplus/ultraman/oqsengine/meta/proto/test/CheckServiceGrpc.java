package com.xforceplus.ultraman.oqsengine.meta.proto.test;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: ForBo.proto")
public final class CheckServiceGrpc {

  private CheckServiceGrpc() {}

  public static final String SERVICE_NAME = "CheckService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp,
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> getCheckMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "check",
      requestType = com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.class,
      responseType = com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp,
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> getCheckMethod() {
    io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp, com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> getCheckMethod;
    if ((getCheckMethod = CheckServiceGrpc.getCheckMethod) == null) {
      synchronized (CheckServiceGrpc.class) {
        if ((getCheckMethod = CheckServiceGrpc.getCheckMethod) == null) {
          CheckServiceGrpc.getCheckMethod = getCheckMethod = 
              io.grpc.MethodDescriptor.<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp, com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(
                  "CheckService", "check"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult.getDefaultInstance()))
                  .setSchemaDescriptor(new CheckServiceMethodDescriptorSupplier("check"))
                  .build();
          }
        }
     }
     return getCheckMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization,
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> getCheckStreamingMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "checkStreaming",
      requestType = com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.class,
      responseType = com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization,
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> getCheckStreamingMethod() {
    io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization, com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> getCheckStreamingMethod;
    if ((getCheckStreamingMethod = CheckServiceGrpc.getCheckStreamingMethod) == null) {
      synchronized (CheckServiceGrpc.class) {
        if ((getCheckStreamingMethod = CheckServiceGrpc.getCheckStreamingMethod) == null) {
          CheckServiceGrpc.getCheckStreamingMethod = getCheckStreamingMethod = 
              io.grpc.MethodDescriptor.<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization, com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(
                  "CheckService", "checkStreaming"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult.getDefaultInstance()))
                  .setSchemaDescriptor(new CheckServiceMethodDescriptorSupplier("checkStreaming"))
                  .build();
          }
        }
     }
     return getCheckStreamingMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static CheckServiceStub newStub(io.grpc.Channel channel) {
    return new CheckServiceStub(channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static CheckServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    return new CheckServiceBlockingStub(channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static CheckServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    return new CheckServiceFutureStub(channel);
  }

  /**
   */
  public static abstract class CheckServiceImplBase implements io.grpc.BindableService {

    /**
     */
    public void check(com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp request,
        io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> responseObserver) {
      asyncUnimplementedUnaryCall(getCheckMethod(), responseObserver);
    }

    /**
     */
    public void checkStreaming(com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization request,
        io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> responseObserver) {
      asyncUnimplementedUnaryCall(getCheckStreamingMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCheckMethod(),
            asyncUnaryCall(
              new MethodHandlers<
                com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp,
                com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult>(
                  this, METHODID_CHECK)))
          .addMethod(
            getCheckStreamingMethod(),
            asyncServerStreamingCall(
              new MethodHandlers<
                com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization,
                com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult>(
                  this, METHODID_CHECK_STREAMING)))
          .build();
    }
  }

  /**
   */
  public static final class CheckServiceStub extends io.grpc.stub.AbstractStub<CheckServiceStub> {
    private CheckServiceStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CheckServiceStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CheckServiceStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CheckServiceStub(channel, callOptions);
    }

    /**
     */
    public void check(com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp request,
        io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> responseObserver) {
      asyncUnaryCall(
          getChannel().newCall(getCheckMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void checkStreaming(com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization request,
        io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> responseObserver) {
      asyncServerStreamingCall(
          getChannel().newCall(getCheckStreamingMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class CheckServiceBlockingStub extends io.grpc.stub.AbstractStub<CheckServiceBlockingStub> {
    private CheckServiceBlockingStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CheckServiceBlockingStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CheckServiceBlockingStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CheckServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult check(com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp request) {
      return blockingUnaryCall(
          getChannel(), getCheckMethod(), getCallOptions(), request);
    }

    /**
     */
    public java.util.Iterator<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> checkStreaming(
        com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization request) {
      return blockingServerStreamingCall(
          getChannel(), getCheckStreamingMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class CheckServiceFutureStub extends io.grpc.stub.AbstractStub<CheckServiceFutureStub> {
    private CheckServiceFutureStub(io.grpc.Channel channel) {
      super(channel);
    }

    private CheckServiceFutureStub(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected CheckServiceFutureStub build(io.grpc.Channel channel,
        io.grpc.CallOptions callOptions) {
      return new CheckServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult> check(
        com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp request) {
      return futureUnaryCall(
          getChannel().newCall(getCheckMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CHECK = 0;
  private static final int METHODID_CHECK_STREAMING = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final CheckServiceImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(CheckServiceImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CHECK:
          serviceImpl.check((com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp) request,
              (io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult>) responseObserver);
          break;
        case METHODID_CHECK_STREAMING:
          serviceImpl.checkStreaming((com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization) request,
              (io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpResult>) responseObserver);
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

  private static abstract class CheckServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    CheckServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("CheckService");
    }
  }

  private static final class CheckServiceFileDescriptorSupplier
      extends CheckServiceBaseDescriptorSupplier {
    CheckServiceFileDescriptorSupplier() {}
  }

  private static final class CheckServiceMethodDescriptorSupplier
      extends CheckServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    CheckServiceMethodDescriptorSupplier(String methodName) {
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
      synchronized (CheckServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new CheckServiceFileDescriptorSupplier())
              .addMethod(getCheckMethod())
              .addMethod(getCheckStreamingMethod())
              .build();
        }
      }
    }
    return result;
  }
}
