package com.xforceplus.ultraman.oqsengine.synchronizer.server;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.sdk.LockRequest;
import com.xforceplus.ultraman.oqsengine.sdk.LockResponse;
import java.util.concurrent.CompletionStage;

/**
 * a lock state server interface.
 */
public interface LockStateService {

    Source<LockResponse, NotUsed> setupCommunication(Source<LockRequest, NotUsed> in, String node);

    CompletionStage<LockResponse> tryAcquire(LockRequest in, String node);

    CompletionStage<LockResponse> tryRelease(LockRequest in, String node);

    CompletionStage<LockResponse> addWaiter(LockRequest in, String node);
}
