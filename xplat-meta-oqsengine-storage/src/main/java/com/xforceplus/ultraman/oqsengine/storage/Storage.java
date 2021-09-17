package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.Iterator;

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
     * Batch creation.
     *
     * @param entityPackage entity package.
     * @return create a number.
     * @throws SQLException Storage error.
     */
    default int build(EntityPackage entityPackage) throws SQLException {
        int size = 0;
        Iterator<IEntity> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            size += build(iter.next(), entityPackage.getEntityClass());
        }
        return size;
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
     * Batch replace.
     *
     * @param entityPackage entity package.
     * @return replace a number.
     * @throws SQLException Storage error.
     */
    default int replace(EntityPackage entityPackage) throws SQLException {
        int size = 0;
        Iterator<IEntity> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            size += replace(iter.next(), entityPackage.getEntityClass());
        }
        return size;
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

    /**
     * Batch delete.
     *
     * @param entityPackage entity package.
     * @return delete a number.
     * @throws SQLException Storage error.
     */
    default int delete(EntityPackage entityPackage) throws SQLException {
        int size = 0;
        Iterator<IEntity> iter = entityPackage.iterator();
        while (iter.hasNext()) {
            size += delete(iter.next(), entityPackage.getEntityClass());
        }
        return size;
    }
}
