package com.xforceplus.ultraman.oqsengine.synchronizer.server.impl;

import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.dto.CriticalResourceKey;
import java.util.Objects;

/**
 * id critical resource.
 */
public class IdCriticalResource implements CriticalResource<Long> {

    private long id;

    private CriticalResourceKey criticalResourceKey;

    public IdCriticalResource(long id) {
        this.id = id;
        this.criticalResourceKey = new CriticalResourceKey(CriticalResource.ResType.ID, id);
    }

    @Override
    public CriticalResourceKey getKey() {
        return criticalResourceKey;
    }

    @Override
    public ResType getType() {
        return ResType.ID;
    }

    @Override
    public Long getRes() {
        return id;
    }

    @Override
    public int compareTo(CriticalResource<Long> o) {
        return Long.compare(o.getRes(), this.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IdCriticalResource that = (IdCriticalResource) o;
        return id == that.id
            && Objects.equals(criticalResourceKey, that.criticalResourceKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, criticalResourceKey);
    }
}
