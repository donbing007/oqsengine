package com.xforceplus.ultraman.oqsengine.devops.rebuild.handler;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;

import java.util.Optional;

/**
 * desc :
 * name : AnyDevOpsTaskHandler
 *
 * @author : xujia
 * date : 2021/3/30
 * @since : 1.8
 */
public class AnyDevOpsTaskHandler implements TaskHandler {
    @Override
    public String id() {
        return null;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public boolean isCancel() {
        return false;
    }

    @Override
    public void cancel() throws Exception {

    }

    @Override
    public Optional<BatchStatus> batchStatus() {
        return Optional.empty();
    }

    @Override
    public int getProgressPercentage() {
        return 0;
    }

    @Override
    public IDevOpsTaskInfo devOpsTaskInfo() {
        return null;
    }
}
