package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task;

import com.xforceplus.ultraman.oqsengine.task.AbstractTask;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * lookup维护任务.
 *
 * @author dongbin
 * @version 0.1 2021/08/16 14:54
 * @since 1.8
 */
public class LookupMaintainingTask extends AbstractTask implements Serializable {

    private static final int DEFAULT_SIZE = 10000;

    private String iterKey;
    private String pointKey;
    private int maxSize;

    public LookupMaintainingTask(String iterKey) {
        this(iterKey, null);
    }

    public LookupMaintainingTask(String iterKey, String seekKey) {
        this(iterKey, seekKey, DEFAULT_SIZE);
    }

    public LookupMaintainingTask(String iterKey, int maxSize) {
        this(iterKey, null, maxSize);
    }

    /**
     * 构造任务实例.
     *
     * @param iterKey 迭代的key.
     * @param seekKey 从此key之后开始.
     * @param maxSize 最大处理数据量.
     */
    public LookupMaintainingTask(String iterKey, String seekKey, int maxSize) {
        this.iterKey = iterKey;
        this.pointKey = seekKey;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LookupMaintainingTask that = (LookupMaintainingTask) o;
        return getMaxSize() == that.getMaxSize() && Objects.equals(getIterKey(), that.getIterKey())
            && Objects.equals(getPointKey(), that.getPointKey());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIterKey(), getPointKey(), getMaxSize());
    }
}
