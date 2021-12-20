package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.task;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.task.AbstractTask;

/**
 * lookup维护任务.
 *
 * @author dongbin
 * @version 0.1 2021/08/16 14:54
 * @since 1.8
 */
public class LookupMaintainingTask extends AbstractTask {

    private static final int DEFAULT_SIZE = 10000;

    private EntityClassRef targetClassRef;
    private EntityClassRef lookupClassRef;
    private long lookupFieldId;
    private long targetEntityId;
    private long targetFieldId;
    private long lastStartLookupEntityId;
    private int maxSize;


    public long getTargetEntityId() {
        return targetEntityId;
    }

    public long getTargetFieldId() {
        return targetFieldId;
    }

    public long getLookupFieldId() {
        return lookupFieldId;
    }

    public EntityClassRef getTargetClassRef() {
        return targetClassRef;
    }

    public EntityClassRef getLookupClassRef() {
        return lookupClassRef;
    }

    public long getLastStartLookupEntityId() {
        return lastStartLookupEntityId;
    }

    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LookupMaintainingTask{");
        sb.append("targetClassRef=").append(targetClassRef);
        sb.append(", lookupClassRef=").append(lookupClassRef);
        sb.append(", lookupFieldId=").append(lookupFieldId);
        sb.append(", targetEntityId=").append(targetEntityId);
        sb.append(", targetFieldId=").append(targetFieldId);
        sb.append(", lastStartLookupEntityId=").append(lastStartLookupEntityId);
        sb.append(", maxSize=").append(maxSize);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public Class runnerType() {
        return LookupMaintainingTaskRunner.class;
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private EntityClassRef targetClassRef;
        private EntityClassRef lookupClassRef;
        private long lookupFieldId;
        private long targetEntityId;
        private long targetFieldId;
        private long lastStartLookupEntityId;
        private int maxSize = DEFAULT_SIZE;

        private Builder() {}

        public static Builder anLookupMaintainingTask() {
            return new Builder();
        }

        public Builder withTargetClassRef(EntityClassRef targetClassRef) {
            this.targetClassRef = targetClassRef;
            return this;
        }

        public Builder withLookupClassRef(EntityClassRef lookupClassRef) {
            this.lookupClassRef = lookupClassRef;
            return this;
        }

        public Builder withLookupFieldId(long lookupFieldId) {
            this.lookupFieldId = lookupFieldId;
            return this;
        }

        public Builder withTargetEntityId(long targetEntityId) {
            this.targetEntityId = targetEntityId;
            return this;
        }

        public Builder withTargetFieldId(long targetFieldId) {
            this.targetFieldId = targetFieldId;
            return this;
        }

        public Builder withMaxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder withLastStartLookupEntityId(long lastStartLookupEntityId) {
            this.lastStartLookupEntityId = lastStartLookupEntityId;
            return this;
        }

        /**
         * 构造实例.
         */
        public LookupMaintainingTask build() {
            LookupMaintainingTask lookupMaintainingTask = new LookupMaintainingTask();
            lookupMaintainingTask.maxSize = this.maxSize;
            lookupMaintainingTask.targetClassRef = this.targetClassRef;
            lookupMaintainingTask.lookupClassRef = this.lookupClassRef;
            lookupMaintainingTask.targetEntityId = this.targetEntityId;
            lookupMaintainingTask.lookupFieldId = this.lookupFieldId;
            lookupMaintainingTask.targetFieldId = this.targetFieldId;
            lookupMaintainingTask.lastStartLookupEntityId = this.lastStartLookupEntityId;
            return lookupMaintainingTask;
        }
    }
}
