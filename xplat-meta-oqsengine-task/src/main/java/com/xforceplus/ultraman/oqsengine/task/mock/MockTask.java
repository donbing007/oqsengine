package com.xforceplus.ultraman.oqsengine.task.mock;

import com.xforceplus.ultraman.oqsengine.task.Task;
import java.io.Serializable;

/**
 * mockTask测试使用.
 * @version 0.1 2021/12/20 16:50
 * @Auther weikai
 * @since 1.8
 */
public class MockTask implements Task, Serializable {
    private static final long serialVersionUID = 1L;
    private long location;

    @Override
    public String id() {
        return null;
    }

    @Override
    public long location() {
        return location;
    }

    @Override
    public void setLocation(long l) {
        this.location = l;
    }

    @Override
    public long createTime() {
        return 0;
    }

    @Override
    public Class runnerType() {
        return null;
    }
}
