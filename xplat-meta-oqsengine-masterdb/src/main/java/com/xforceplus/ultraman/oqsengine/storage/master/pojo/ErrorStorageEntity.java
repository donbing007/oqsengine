package com.xforceplus.ultraman.oqsengine.storage.master.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;

/**
 * Created by justin.xu on 05/2021.
 *
 * @since 1.8
 */
public class ErrorStorageEntity {
    private long maintainId;
    private long id;
    private long entity;
    private String errors;
    private long executeTime;
    private long fixedTime;
    private int status;

    private ErrorStorageEntity() {
    }

    public long getMaintainId() {
        return maintainId;
    }

    public long getId() {
        return id;
    }

    public long getEntity() {
        return entity;
    }

    public String getErrors() {
        return errors;
    }

    public long getExecuteTime() {
        return executeTime;
    }

    public long getFixedTime() {
        return fixedTime;
    }

    public int getStatus() {
        return status;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setEntity(long entity) {
        this.entity = entity;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    public void setExecuteTime(long executeTime) {
        this.executeTime = executeTime;
    }

    public void setFixedTime(long fixedTime) {
        this.fixedTime = fixedTime;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setMaintainId(long maintainId) {
        this.maintainId = maintainId;
    }

    @Override
    public String toString() {
        return "ErrorStorageEntity{"
            + "maintainId=" + maintainId
            + ", id=" + id
            + ", entity=" + entity
            + ", errors='" + errors + '\''
            + ", executeTime=" + executeTime
            + ", fixedTime=" + fixedTime
            + ", status=" + status
            + '}';
    }

    /**
     * builder.
     */
    public static class Builder {
        private long maintainId;
        private long id;
        private long entity;
        private long executeTime;
        private long fixedTime;
        private String errors;
        private int status;

        private Builder() {
        }

        public static Builder anErrorStorageEntity() {
            return new Builder();
        }

        public Builder withMaintainId(long maintainId) {
            this.maintainId = maintainId;
            return this;
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withEntity(long entity) {
            this.entity = entity;
            return this;
        }

        public Builder withExecuteTime(long executeTime) {
            this.executeTime = executeTime;
            return this;
        }

        public Builder withFixedTime(long fixedTime) {
            this.fixedTime = fixedTime;
            return this;
        }

        /**
         * set error, 只记录错误的code列表.
         */
        public Builder withErrors(String errors) {
            this.errors = errors;
            return this;
        }

        public Builder withFixedStatus(int fixedStatus) {
            this.status = fixedStatus;
            return this;
        }

        /**
         * build.
         */
        public ErrorStorageEntity build() {
            ErrorStorageEntity errorStorageEntity = new ErrorStorageEntity();
            errorStorageEntity.maintainId = this.maintainId;
            errorStorageEntity.id = this.id;
            errorStorageEntity.entity = this.entity;
            errorStorageEntity.errors = this.errors;
            if (0 == this.executeTime) {
                errorStorageEntity.executeTime = System.currentTimeMillis();
            } else {
                errorStorageEntity.executeTime = this.executeTime;
            }

            errorStorageEntity.status = this.status;

            if (0 == this.fixedTime) {
                if (status == FixedStatus.FIXED.getStatus()) {
                    errorStorageEntity.fixedTime = System.currentTimeMillis();
                }
            } else {
                errorStorageEntity.fixedTime = this.fixedTime;
            }

            return errorStorageEntity;
        }

    }
}
