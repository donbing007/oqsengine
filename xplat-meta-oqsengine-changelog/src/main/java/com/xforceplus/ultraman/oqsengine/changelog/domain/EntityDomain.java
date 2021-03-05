package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * an entityDomain contains both the entityvalue and a referenceValue
 */
public class EntityDomain {

    /**
     * main value
     */
    private IEntity entity;

    /**
     * TODO
     */
    private Map<OqsRelation, List<Long>> referenceMap = new HashMap<>();

    public IEntity getEntity() {
        return entity;
    }

    public void setEntity(IEntity entity) {
        this.entity = entity;
    }

    public Map<OqsRelation, List<Long>> getReferenceMap() {
        return referenceMap;
    }

    public void setReferenceMap(Map<OqsRelation, List<Long>> referenceMap) {
        this.referenceMap = referenceMap;
    }
}
