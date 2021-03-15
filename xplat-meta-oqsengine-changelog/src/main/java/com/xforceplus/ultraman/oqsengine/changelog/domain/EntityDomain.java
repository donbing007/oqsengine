package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.util.List;
import java.util.Map;

/**
 * an entityDomain contains both the entityvalue and a referenceValue
 */
public class EntityDomain {

    private long id;

    private long version;

    /**
     * changelog retrieved
     */
    private long count;

    /**
     * main value
     */
    private IEntity entity;

    /**
     * current relation map with all related or be related
     * oqsRelation is not self is related --- a reversed set
     * oqsRelation is self --- a id set
     */
    private Map<OqsRelation, List<Long>> referenceMap;

    public EntityDomain(long count, long version, IEntity entity, Map<OqsRelation, List<Long>> referenceMap) {
        this.count = count;
        this.entity = entity;
        this.version = version;
        this.referenceMap = referenceMap;
        this.id = entity.id();
    }

    public long getId() {
        return id;
    }

    public long getCount() {
        return count;
    }

    public IEntity getEntity() {
        return entity;
    }

    public Map<OqsRelation, List<Long>> getReferenceMap() {
        return referenceMap;
    }

    public long getVersion() {
        return version;
    }
}
