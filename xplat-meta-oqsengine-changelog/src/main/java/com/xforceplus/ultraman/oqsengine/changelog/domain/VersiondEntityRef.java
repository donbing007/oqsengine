package com.xforceplus.ultraman.oqsengine.changelog.domain;

/**
 * EntityRef
 */
public class VersiondEntityRef {

    private long version = -1;

    private long entityClassId;

    private long id;

    public VersiondEntityRef() {
    }

    public VersiondEntityRef(long entityClassId, long id){
        this.entityClassId = entityClassId;
        this.id = id;
    }

    public VersiondEntityRef(long entityClassId, long id, long version) {
        this.version = version;
        this.entityClassId = entityClassId;
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getEntityClassId() {
        return entityClassId;
    }

    public void setEntityClassId(long entityClassId) {
        this.entityClassId = entityClassId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
