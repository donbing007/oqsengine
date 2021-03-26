package com.xforceplus.ultraman.oqsengine.changelog.domain;

import java.util.Objects;

/**
 * to identify a entity is changed in this commit
 */
public class ChangedEntity {

    private long entityclassId;

    private long objId;

    private long commitId;

    public ChangedEntity(long entityclassId, long objId, long commitId) {
        this.entityclassId = entityclassId;
        this.objId = objId;
        this.commitId = commitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangedEntity that = (ChangedEntity) o;
        return entityclassId == that.entityclassId &&
                objId == that.objId &&
                commitId == that.commitId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityclassId, objId, commitId);
    }
}

