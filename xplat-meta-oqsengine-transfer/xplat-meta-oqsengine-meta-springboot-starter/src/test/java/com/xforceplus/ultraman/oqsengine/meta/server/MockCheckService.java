package com.xforceplus.ultraman.oqsengine.meta.server;

import com.xforceplus.ultraman.oqsengine.meta.annotation.BindGRpcService;
import com.xforceplus.ultraman.oqsengine.meta.proto.test.*;
import org.springframework.stereotype.Component;

/**
 * desc :
 * name : MockCheckService
 *
 * @author : xujia
 * date : 2021/3/23
 * @since : 1.8
 */

@Component
@BindGRpcService
public class MockCheckService extends CheckServiceGrpc.CheckServiceImplBase {

    /**
     *
     */
    public void check(ModuleUp request,
                      io.grpc.stub.StreamObserver<ModuleUpResult> responseObserver) {
        Authorization authorization = null;
        if (request.getAuthorizationCount() > 0) {
            authorization = request.getAuthorization(0);
        }
        responseObserver.onNext(mockResult(authorization));

        responseObserver.onCompleted();
    }

    /**
     *
     */
    public void checkStreaming(Authorization request,
                               io.grpc.stub.StreamObserver<ModuleUpResult> responseObserver) {
        responseObserver.onNext(mockResult(request));
    }

    private ModuleUpResult mockResult(Authorization request) {
        ModuleUpResult.Builder builder = ModuleUpResult.newBuilder();
        if (null != request) {
            builder.setAuth(request)
                    .setStatus(1).addBoUps(BoUp.newBuilder()
                                                .addApis(
                                                        Api.newBuilder()
                                                                .setCode("test")
                                                                .setMethod("testMethod")
                                                                .setUrl("testUrl")
                                                                .build()
                                                )
                                                .setCode("testBoUp")
                                                .setCreateType("testCreate")
                                                .setDomainCode("testDomain")
                                                .setId("testId")
                                                .setName("testName")
                                                .build())
                    .setMessage("test")
                    .setVersion("1.0");
            return builder.build();
        } else {
            return builder.setMessage("授权失败").setStatus(2).build();
        }
    }

}
