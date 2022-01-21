package com.xforceplus.ultraman.oqsengine.devops.rebuild.handler;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;


/**
 * 默认的devops任务实现.
 *
 * @author : xujia 2020/8/25
 * @since : 1.8
 */
public class DefaultDevOpsTaskHandler implements TaskHandler {
    protected DevOpsTaskInfo devOpsTaskInfo;
    private SQLTaskStorage sqlTaskStorage;

    public DefaultDevOpsTaskHandler(SQLTaskStorage sqlTaskStorage, DevOpsTaskInfo taskInfo) {
        this.devOpsTaskInfo = taskInfo;
        this.sqlTaskStorage = sqlTaskStorage;
    }

    @Override
    public String id() {
        return String.valueOf(devOpsTaskInfo.id());
    }

    @Override
    public boolean isDone() {
        if (!devOpsTaskInfo.isDone()) {
            flush();
        }
        return devOpsTaskInfo.isDone();
    }

    @Override
    public boolean isError() {
        if (!devOpsTaskInfo.isDone()) {
            flush();
        }
        return devOpsTaskInfo.isError();
    }

    @Override
    public long getProgressPercentage() {
        if (!isDone()) {
            return devOpsTaskInfo.getProgressPercentage();
        }
        return ONE_HUNDRED_PERCENT;
    }

    @Override
    public DevOpsTaskInfo devOpsTaskInfo() {
        return devOpsTaskInfo;
    }

    private void flush() {
        try {
            sqlTaskStorage.selectUnique((devOpsTaskInfo).getMaintainid()).ifPresent(opsTaskInfo -> {
                synchronized (DefaultDevOpsTaskHandler.class) {
                    devOpsTaskInfo.setFinishSize(opsTaskInfo.getFinishSize());
                    devOpsTaskInfo.setErrorSize(opsTaskInfo.getErrorSize());
                    devOpsTaskInfo.resetStatus(opsTaskInfo.getStatus());
                    devOpsTaskInfo.resetMessage(opsTaskInfo.message());
                    devOpsTaskInfo.resetUpdateTime(opsTaskInfo.updateTime());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            //  ignore
        }
    }
}
