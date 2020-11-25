package com.xforceplus.ultraman.oqsengine.devops.rebuild.handler;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.storage.SQLTaskStorage;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;

import java.sql.SQLException;
import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.BATCH_STATUS;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.ONE_HUNDRED_PERCENT;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus.CANCEL;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.DevOpsRebuildIndexExecutor.BATCH_STATUS_CACHE;


/**
 * desc :
 * name : DevOpsTaskHandler
 *
 * @author : xujia
 * date : 2020/8/25
 * @since : 1.8
 */
public class DefaultDevOpsTaskHandler implements TaskHandler {
    protected IDevOpsTaskInfo devOpsTaskInfo;
    private SQLTaskStorage sqlTaskStorage;

    public DefaultDevOpsTaskHandler(SQLTaskStorage sqlTaskStorage, IDevOpsTaskInfo taskInfo) {
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
            syncTask();
        }
        return devOpsTaskInfo.isDone();
    }

    @Override
    public boolean isCancel() {
        if (!devOpsTaskInfo.isCancel()) {
            syncTask();
        }
        return devOpsTaskInfo.isCancel();
    }

    @Override
    public void cancel() throws SQLException {
        ((DevOpsTaskInfo)devOpsTaskInfo).setStatus(CANCEL.getCode());
        sqlTaskStorage.cancel(devOpsTaskInfo.getMaintainid());
    }

    @Override
    public Optional<BatchStatus> batchStatus() {
        String key = BATCH_STATUS + id();
        BatchStatus batchStatus = BATCH_STATUS_CACHE.getIfPresent(key);
        if (null == batchStatus) {
            if (!devOpsTaskInfo.isDone() && !devOpsTaskInfo.isCancel()) {
                syncTask();
            }
            batchStatus = BatchStatus.toBatchStatus(devOpsTaskInfo.getStatus());
            if (null != batchStatus) {
                BATCH_STATUS_CACHE.put(key, batchStatus);
            }
        }
        return Optional.ofNullable(batchStatus);
    }

    @Override
    public int getProgressPercentage() {
        if (!isDone()) {
            return devOpsTaskInfo.getProgressPercentage();
        }
        return ONE_HUNDRED_PERCENT;
    }

    @Override
    public IDevOpsTaskInfo devOpsTaskInfo() {
        return devOpsTaskInfo;
    }

    private void syncTask() {
        try {
            Optional<IDevOpsTaskInfo> dev = sqlTaskStorage.selectUnique((devOpsTaskInfo).getMaintainid());
            dev.ifPresent(
                    devOps -> {
                        synchronized (DefaultDevOpsTaskHandler.class) {
                            if (devOps.getFinishSize() >
                                    (devOpsTaskInfo).getFinishSize()) {
                                ((DevOpsTaskInfo)devOpsTaskInfo).setFinishSize(devOps.getFinishSize());
                            } else if (devOps.getFinishSize() == devOps.getBatchSize()) {
                                ((DevOpsTaskInfo)devOpsTaskInfo).setStatus(devOps.getStatus());
                            } else if (devOps.getStatus() != devOpsTaskInfo.getStatus()) {
                                ((DevOpsTaskInfo) devOpsTaskInfo).setStatus(devOps.getStatus());
                                devOpsTaskInfo.resetMessage(devOps.message());
                            }
                        }
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            //  ignore
        }
    }
}
