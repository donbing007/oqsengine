package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.EntityPackage;
import java.sql.SQLException;
import java.util.Iterator;
import javafx.util.Pair;

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
     * @return create results.
     * @throws SQLException Storage error.
     */
    default int[] build(EntityPackage entityPackage) throws SQLException {
        int[] results = new int[entityPackage.size()];
        Iterator<Pair<IEntity, IEntityClass>> iter = entityPackage.iterator();
        int index = 0;
        while (iter.hasNext()) {
            Pair<IEntity, IEntityClass> entry = iter.next();
            results[index++] = build(entry.getKey(), entry.getValue());
        }
        return results;
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
     * @return replace results.
     * @throws SQLException Storage error.
     */
    default int[] replace(EntityPackage entityPackage) throws SQLException {
        int[] results = new int[entityPackage.size()];
        Iterator<Pair<IEntity, IEntityClass>> iter = entityPackage.iterator();
        int index = 0;
        while (iter.hasNext()) {
            Pair<IEntity, IEntityClass> entry = iter.next();
            results[index++] = replace(entry.getKey(), entry.getValue());
        }
        return results;
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
     * @return delete results.
     * @throws SQLException Storage error.
     */
    default int[] delete(EntityPackage entityPackage) throws SQLException {
        int[] results = new int[entityPackage.size()];
        Iterator<Pair<IEntity, IEntityClass>> iter = entityPackage.iterator();
        int index = 0;
        while (iter.hasNext()) {
            Pair<IEntity, IEntityClass> entry = iter.next();
            results[index++] = delete(entry.getKey(), entry.getValue());
        }
        return results;
    }
}
