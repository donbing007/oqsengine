package com.xforceplus.ultraman.oqsengine.storage.index;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.Storage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;

/**
 * 索引储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 19:44
 * @since 1.8
 */
public interface IndexStorage extends Storage {

    /**
     * 条件搜索 entity 的指针信息.
     * 如果没有某个条件没有指定 entityClass,那么将假定为和返回 entityClass 一致.
     *
     * @param conditions  搜索条件.
     * @param entityClass 搜索目标的 entityClass.
     * @param sort        搜索结果排序.
     * @param page        搜索结果分页信息.
     * @return 搜索结果列表.
     * @throws SQLException
     */
    Collection<EntityRef> select(
        Conditions conditions, IEntityClass entityClass, Sort sort, Page page, Set<Long> filterIds, long commitId)
        throws SQLException;

    /**
     * 替换索引中某些属性的值.
     *
     * @param attribute 需要更新的属性值.
     */
    void replaceAttribute(IEntityValue attribute) throws SQLException;


    /**
     * Deletes an existing entity.
     *
     * @param id target entity.
     * @throws SQLException Storage error.
     */
    int delete(long id) throws SQLException;

    /**
     * 转换entityValue为JsonFields和FullFields
     *
     * @param storageEntity
     * @param entityValue
     * @throws SQLException Storage error.
     */
    void entityValueToStorage(StorageEntity storageEntity, IEntityValue entityValue);

    /**
     * 批量插入或更新.
     *
     * @param storageEntities
     * @param replacement
     * @throws SQLException Storage error.
     */
    int batchSave(Collection<StorageEntity> storageEntities, boolean replacement, boolean retry) throws SQLException;

    /**
     * Deletes an existing entity.
     *
     * @param storageEntity
     * @param entityValue
     * @param replacement
     * @throws SQLException Storage error.
     */
    int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException;

    /**
     * 批量删除，遍历dataSource.
     *
     * @param entityId   搜索目标的 entityID.
     * @param maintainId 搜索目标的 taskId.
     * @param start      开始时间.
     * @param end        结束时间.
     */
    boolean clean(long entityId, long maintainId, long start, long end) throws SQLException;
}
