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
     * 初始化.
     *
     * @throws SQLException 初始化失败.
     */
    default void init() throws SQLException {

    }

    /**
     * 清理.
     *
     * @throws SQLException 清理失败.
     */
    default void destroy() throws SQLException {

    }

    /**
     * Create a new Entity.
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    int build(IEntity entity, IEntityClass entityClass) throws SQLException;

    /**
     * Replace the information of the target Entity.
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    int replace(IEntity entity, IEntityClass entityClass) throws SQLException;

    /**
     * Deletes an existing entity.
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    int delete(IEntity entity, IEntityClass entityClass) throws SQLException;
}
