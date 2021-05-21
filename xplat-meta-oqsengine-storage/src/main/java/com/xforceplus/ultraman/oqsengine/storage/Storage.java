package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.sql.SQLException;

/**
 * Store generic definitions.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 20:01
 * @since 1.8
 */
public interface Storage {

    /**
     * Create a new Entity.
     *
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    default int build(IEntity entity, IEntityClass entityClass) throws SQLException {
        return 0;
    }

    /**
     * Replace the information of the target Entity.
     *
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    default int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        return 0;
    }

    /**
     * Deletes an existing entity.
     *
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    default int delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        return 0;
    }
}
