package com.xforceplus.ultraman.oqsengine.synchronizer.server.state;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.thread.MockThread;
import io.vavr.control.Either;
import java.util.List;

/**
 * TODO
 * cirtical resource state.
 */
public interface CriticalResourceState {

    public Either<CriticalResource, Boolean> tryAcquire(List<CriticalResource> criticalResourceList,
                                                        MockThread current);
}
