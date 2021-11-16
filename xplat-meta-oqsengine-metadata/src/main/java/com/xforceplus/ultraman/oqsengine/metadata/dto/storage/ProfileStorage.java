package com.xforceplus.ultraman.oqsengine.metadata.dto.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import java.util.List;

/**
 * 替身 storage.
 *
 * @author xujia
 * @since 1.8
 */
public class ProfileStorage {
    private String code;
    private List<EntityField> entityFieldList;
    private List<RelationStorage> relationStorageList;

    public ProfileStorage() {
    }

    /**
     * 构造函数.
     */
    public ProfileStorage(String code, List<EntityField> entityFieldList, List<RelationStorage> relationStorageList) {
        this.code = code;
        this.entityFieldList = entityFieldList;
        this.relationStorageList = relationStorageList;
    }

    public ProfileStorage(String code) {
        this.code = code;
        this.entityFieldList = new ArrayList<>();
    }

    public String getCode() {
        return code;
    }

    public List<EntityField> getEntityFieldList() {
        return entityFieldList;
    }

    public List<RelationStorage> getRelationStorageList() {
        return relationStorageList;
    }

    public void addField(EntityField entityField) {
        this.entityFieldList.add(entityField);
    }

    public void setRelationStorageList(List<RelationStorage> relationStorageList) {
        this.relationStorageList = relationStorageList;
    }
}
