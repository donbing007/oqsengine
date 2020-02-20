package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;

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
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    void build(IEntity entity) throws SQLException;

    /**
     * Replace the information of the target Entity.
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    void replace(IEntity entity) throws SQLException;

    /**
     * Deletes an existing entity.
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    void delete(IEntity entity) throws SQLException;
}
