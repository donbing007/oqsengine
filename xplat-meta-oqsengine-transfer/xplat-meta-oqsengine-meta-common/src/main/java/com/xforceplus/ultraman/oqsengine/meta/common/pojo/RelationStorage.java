package com.xforceplus.ultraman.oqsengine.meta.common.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;

/**
 * desc :
 * name : RelationStorage
 *
 * @author : xujia
 * date : 2021/2/26
 * @since : 1.8
 */
public class RelationStorage {

    private long id;
    private String code;
    private long rightEntityClassId;
    private long leftEntityClassId;
    private String leftEntityClassCode;
    private int relationType;
    private boolean identity;
    private EntityField entityField;
    private boolean belongToOwner;
    private boolean strong;

    public RelationStorage() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getRightEntityClassId() {
        return rightEntityClassId;
    }

    public void setRightEntityClassId(long rightEntityClassId) {
        this.rightEntityClassId = rightEntityClassId;
    }

    public long getLeftEntityClassId() {
        return leftEntityClassId;
    }

    public void setLeftEntityClassId(long leftEntityClassId) {
        this.leftEntityClassId = leftEntityClassId;
    }

    public String getLeftEntityClassCode() {
        return leftEntityClassCode;
    }

    public void setLeftEntityClassCode(String leftEntityClassCode) {
        this.leftEntityClassCode = leftEntityClassCode;
    }

    public boolean isIdentity() {
        return identity;
    }

    public void setIdentity(boolean identity) {
        this.identity = identity;
    }

    public EntityField getEntityField() {
        return entityField;
    }

    public void setEntityField(EntityField entityField) {
        this.entityField = entityField;
    }

    public boolean isBelongToOwner() {
        return belongToOwner;
    }

    public void setBelongToOwner(boolean belongToOwner) {
        this.belongToOwner = belongToOwner;
    }

    public void setRelationType(int relationType) {
        this.relationType = relationType;
    }

    public boolean isStrong() {
        return strong;
    }

    public int getRelationType() {
        return relationType;
    }

    public void setStrong(boolean strong) {
        this.strong = strong;
    }
}
