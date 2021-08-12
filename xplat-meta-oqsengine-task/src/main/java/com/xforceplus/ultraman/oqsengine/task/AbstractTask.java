package com.xforceplus.ultraman.oqsengine.task;

import com.xforceplus.ultraman.oqsengine.common.id.UUIDGenerator;

/**
 * 任务的抽像实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/12 14:50
 * @since 1.8
 */
public abstract class AbstractTask implements Task {

    private String id;
    private long createTime;

    public AbstractTask() {
        id = UUIDGenerator.getInstance().next();
        createTime = System.currentTimeMillis();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public long createTime() {
        return createTime;
    }
}
