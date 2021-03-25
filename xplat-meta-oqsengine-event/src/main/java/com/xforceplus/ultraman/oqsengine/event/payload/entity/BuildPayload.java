package com.xforceplus.ultraman.oqsengine.event.payload.entity;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 创建的负载.
 *
 * @author dongbin
 * @version 0.1 2021/3/24 15:39
 * @since 1.8
 */
public class BuildPayload implements Serializable {

    private IEntity entity;

    public BuildPayload(IEntity entity) {
        this.entity = entity;
    }

    public IEntity getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BuildPayload)) {
            return false;
        }
        BuildPayload that = (BuildPayload) o;
        return Objects.equals(getEntity(), that.getEntity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEntity());
    }
}
