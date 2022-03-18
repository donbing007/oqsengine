package com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.define.OperationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

/**
 * 储存实例抽像公共.
 *
 * @author dongbin
 * @version 0.1 2021/12/23 14:33
 * @since 1.8
 */
public class BaseMasterStorageEntity {

    // 动态对象操作是否成功.
    private boolean dynamicSuccess = false;
    // 静态对象操作是否成功.
    private boolean originalSucess = false;
    private boolean original = false;
    private boolean deleted = false;
    private int oqsMajor = OqsVersion.MAJOR;
    private int entityClassVersion = 0;
    private int version = 0;
    private int op = OperationType.UNKNOWN.getValue();
    private long id = -1;
    private long tx = -1;
    private long commitid = -1;
    private long createTime;
    private long updateTime;
    private long[] entityClasses;
    private String profile;
    private String originalTableName;
    private IEntity sourceEntity;

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public int getOqsMajor() {
        return oqsMajor;
    }

    public void setOqsMajor(int oqsMajor) {
        this.oqsMajor = oqsMajor;
    }

    public int getEntityClassVersion() {
        return entityClassVersion;
    }

    public void setEntityClassVersion(int entityClassVersion) {
        this.entityClassVersion = entityClassVersion;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTx() {
        return tx;
    }

    public void setTx(long tx) {
        this.tx = tx;
    }

    public long getCommitid() {
        return commitid;
    }

    public void setCommitid(long commitid) {
        this.commitid = commitid;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long[] getEntityClasses() {
        return entityClasses;
    }

    public void setEntityClasses(long[] entityClasses) {
        this.entityClasses = entityClasses;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getOriginalTableName() {
        return originalTableName;
    }

    public void setOriginalTableName(String originalTableName) {
        this.originalTableName = originalTableName;
    }

    public IEntity getSourceEntity() {
        return sourceEntity;
    }

    public void setSourceEntity(IEntity sourceEntity) {
        this.sourceEntity = sourceEntity;
    }

    public boolean isDynamicSuccess() {
        return dynamicSuccess;
    }

    public void setDynamicSuccess(boolean dynamicSuccess) {
        this.dynamicSuccess = dynamicSuccess;
    }

    public boolean isOriginalSucess() {
        return originalSucess;
    }

    public void setOriginalSucess(boolean originalSucess) {
        this.originalSucess = originalSucess;
    }

    /**
     * 获得当前实例实际类型.
     *
     * @return 实际类型标识.
     */
    public long getSelfEntityClassId() {
        if (this.entityClasses == null) {
            return 0;
        }

        for (int i = 0; i < this.entityClasses.length; i++) {
            if (this.entityClasses[i] == 0) {
                if (i > 0) {
                    return this.entityClasses[i - 1];
                } else {
                    return 0;
                }
            }
        }

        return this.entityClasses[this.entityClasses.length - 1];
    }
}
