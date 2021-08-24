package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task;

import com.xforceplus.ultraman.oqsengine.task.AbstractTask;
import java.io.Serializable;
import java.util.Optional;

/**
 * lookup维护任务.
 *
 * @author dongbin
 * @version 0.1 2021/08/16 14:54
 * @since 1.8
 */
public class LookupMaintainingTask extends AbstractTask implements Serializable {

    private static final int DEFAULT_SIZE = 100;

    private String iterKey;
    private String pointKey;
    private int maxSize;

    public LookupMaintainingTask(String iterKey) {
        this(iterKey, null);
    }

    public LookupMaintainingTask(String iterKey, String pointKey) {
        this(iterKey, pointKey, DEFAULT_SIZE);
    }

    /**
     * 构造任务实例.
     *
     * @param iterKey  迭代的key.
     * @param pointKey 当前开始的key.
     * @param maxSize  最大处理数据量.
     */
    public LookupMaintainingTask(String iterKey, String pointKey, int maxSize) {
        this.iterKey = iterKey;
        this.pointKey = pointKey;
        this.maxSize = maxSize;
    }

    @Override
    public Class runnerType() {
        return LookupMaintainingTaskRunner.class;
    }

    public String getIterKey() {
        return iterKey;
    }

    public Optional<String> getPointKey() {
        return Optional.ofNullable(pointKey);
    }

    public int getMaxSize() {
        return maxSize;
    }
}
