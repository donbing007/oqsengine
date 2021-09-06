package com.xforceplus.ultraman.oqsengine.changelog.domain;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;

import java.util.Collection;
import java.util.Map;

/**
 * an entity relation from changelogs only need relation
 */
public class EntityRelation {

    /**
     * current entity id
     */
    private long id;

    private long entityClassId;

    private Map<Relationship, Collection<ValueLife>> relatedIds;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public Map<Relationship, Collection<ValueLife>> getRelatedIds() {
        return relatedIds;
    }

    public void setRelatedIds(Map<Relationship, Collection<ValueLife>> relatedIds) {
        this.relatedIds = relatedIds;
    }
}
