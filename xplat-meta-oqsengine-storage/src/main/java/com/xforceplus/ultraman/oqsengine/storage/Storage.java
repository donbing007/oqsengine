package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

/**
 * Store generic definitions.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 20:01
 * @since 1.8
 */
public interface Storage {

    /**
     * 创建一个新的实体.
     * 创建成功后保证目标实体从"脏"恢复成"干净".
     *
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    default boolean build(IEntity entity, IEntityClass entityClass) throws SQLException {
        return false;
    }

    /**
     * 批量创建实体.
     * 只有脏实体才会被创建.
     *
     * @param entityPackage entity package.
     * @throws SQLException Storage error.
     */
    default void build(EntityPackage entityPackage) throws SQLException {
        Iterator<Map.Entry<IEntity, IEntityClass>> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            Map.Entry<IEntity, IEntityClass> entry = iter.next();
            build(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Replace the information of the target Entity.
     *
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    default boolean replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        return false;
    }

    /**
     * Batch replace.
     *
     * @param entityPackage entity package.
     * @throws SQLException Storage error.
     */
    default void replace(EntityPackage entityPackage) throws SQLException {
        Iterator<Map.Entry<IEntity, IEntityClass>> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            Map.Entry<IEntity, IEntityClass> entry = iter.next();
            replace(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Deletes an existing entity.
     *
     * @param entity target entity.
     * @throws SQLException Storage error.
     */
    default boolean delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        return false;
    }

    /**
     * Batch delete.
     *
     * @param entityPackage entity package.
     * @throws SQLException Storage error.
     */
    default void delete(EntityPackage entityPackage) throws SQLException {
        Iterator<Map.Entry<IEntity, IEntityClass>> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            Map.Entry<IEntity, IEntityClass> entry = iter.next();
            delete(entry.getKey(), entry.getValue());
        }
    }
}
