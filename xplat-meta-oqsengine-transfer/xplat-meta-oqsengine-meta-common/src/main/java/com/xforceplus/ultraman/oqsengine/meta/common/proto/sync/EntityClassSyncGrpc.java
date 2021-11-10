package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

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
 *
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.15.0)",
    comments = "Source: sync.proto")
public final class EntityClassSyncGrpc {

    private EntityClassSyncGrpc() {
    }

    public static final String SERVICE_NAME = "EntityClassSync";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest,
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse> getRegisterMethod;

    @io.grpc.stub.annotations.RpcMethod(
        fullMethodName = SERVICE_NAME + '/' + "register",
        requestType = com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest.class,
        responseType = com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.class,
        methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
    public static io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest,
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse> getRegisterMethod() {
        io.grpc.MethodDescriptor<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse>
            getRegisterMethod;
        if ((getRegisterMethod = EntityClassSyncGrpc.getRegisterMethod) == null) {
            synchronized (EntityClassSyncGrpc.class) {
                if ((getRegisterMethod = EntityClassSyncGrpc.getRegisterMethod) == null) {
                    EntityClassSyncGrpc.getRegisterMethod = getRegisterMethod =
                        io.grpc.MethodDescriptor
                            .<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse>newBuilder()
                            .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
                            .setFullMethodName(generateFullMethodName(
                                "EntityClassSync", "register"))
                            .setSampledToLocalTracing(true)
                            .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest
                                    .getDefaultInstance()))
                            .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse
                                    .getDefaultInstance()))
                            .setSchemaDescriptor(new EntityClassSyncMethodDescriptorSupplier("register"))
                            .build();
                }
            }
        }
        return getRegisterMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static EntityClassSyncStub newStub(io.grpc.Channel channel) {
        return new EntityClassSyncStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static EntityClassSyncBlockingStub newBlockingStub(
        io.grpc.Channel channel) {
        return new EntityClassSyncBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static EntityClassSyncFutureStub newFutureStub(
        io.grpc.Channel channel) {
        return new EntityClassSyncFutureStub(channel);
    }

    /**
     *
     */
    public static abstract class EntityClassSyncImplBase implements io.grpc.BindableService {

        /**
         *
         */
        public io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest> register(
            io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse> responseObserver) {
            return asyncUnimplementedStreamingCall(getRegisterMethod(), responseObserver);
        }

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                .addMethod(
                    getRegisterMethod(),
                    asyncBidiStreamingCall(
                        new MethodHandlers<
                            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest,
                            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse>(
                            this, METHODID_REGISTER)))
                .build();
        }
    }

    /**
     *
     */
    public static final class EntityClassSyncStub extends io.grpc.stub.AbstractStub<EntityClassSyncStub> {
        private EntityClassSyncStub(io.grpc.Channel channel) {
            super(channel);
        }

        private EntityClassSyncStub(io.grpc.Channel channel,
                                    io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected EntityClassSyncStub build(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
            return new EntityClassSyncStub(channel, callOptions);
        }

        /**
         *
         */
        public io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRequest> register(
            io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse> responseObserver) {
            return asyncBidiStreamingCall(
                getChannel().newCall(getRegisterMethod(), getCallOptions()), responseObserver);
        }
    }

    /**
     *
     */
    public static final class EntityClassSyncBlockingStub
        extends io.grpc.stub.AbstractStub<EntityClassSyncBlockingStub> {
        private EntityClassSyncBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private EntityClassSyncBlockingStub(io.grpc.Channel channel,
                                            io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected EntityClassSyncBlockingStub build(io.grpc.Channel channel,
                                                    io.grpc.CallOptions callOptions) {
            return new EntityClassSyncBlockingStub(channel, callOptions);
        }
    }

    /**
     *
     */
    public static final class EntityClassSyncFutureStub extends io.grpc.stub.AbstractStub<EntityClassSyncFutureStub> {
        private EntityClassSyncFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private EntityClassSyncFutureStub(io.grpc.Channel channel,
                                          io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected EntityClassSyncFutureStub build(io.grpc.Channel channel,
                                                  io.grpc.CallOptions callOptions) {
            return new EntityClassSyncFutureStub(channel, callOptions);
        }
    }

    private static final int METHODID_REGISTER = 0;

    private static final class MethodHandlers<Req, Resp> implements
        io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
        io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
        io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
        io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final EntityClassSyncImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(EntityClassSyncImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
            io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_REGISTER:
                    return (io.grpc.stub.StreamObserver<Req>) serviceImpl.register(
                        (io.grpc.stub.StreamObserver<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse>) responseObserver);
                default:
                    throw new AssertionError();
            }
        }
    }

    private static abstract class EntityClassSyncBaseDescriptorSupplier
        implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        EntityClassSyncBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("EntityClassSync");
        }
    }

    private static final class EntityClassSyncFileDescriptorSupplier
        extends EntityClassSyncBaseDescriptorSupplier {
        EntityClassSyncFileDescriptorSupplier() {
        }
    }

    private static final class EntityClassSyncMethodDescriptorSupplier
        extends EntityClassSyncBaseDescriptorSupplier
        implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        EntityClassSyncMethodDescriptorSupplier(String methodName) {
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
            synchronized (EntityClassSyncGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                        .setSchemaDescriptor(new EntityClassSyncFileDescriptorSupplier())
                        .addMethod(getRegisterMethod())
                        .build();
                }
            }
        }
        return result;
    }
}
