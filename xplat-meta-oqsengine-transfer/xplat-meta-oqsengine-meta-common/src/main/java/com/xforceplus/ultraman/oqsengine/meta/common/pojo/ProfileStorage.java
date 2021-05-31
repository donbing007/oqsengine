package com.xforceplus.ultraman.oqsengine.meta.common.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.util.ArrayList;
import java.util.List;

/**
 * 替身 storage.
 *
 * @author xujia
 * @since 1.8
 */
public class ProfileStorage {
    private final String code;
    private final List<IEntityField> entityFieldList;
    private List<RelationStorage> relationStorageList;

    /**
     * 构造函数.
     */
    public ProfileStorage(String code, List<IEntityField> entityFieldList, List<RelationStorage> relationStorageList) {
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

    public List<IEntityField> getEntityFieldList() {
        return entityFieldList;
    }

    public List<RelationStorage> getRelationStorageList() {
        return relationStorageList;
    }

    public void addField(IEntityField entityField) {
        this.entityFieldList.add(entityField);
    }

    public void setRelationStorageList(List<RelationStorage> relationStorageList) {
        this.relationStorageList = relationStorageList;
    }
}
