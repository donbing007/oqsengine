package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.utils.EntityRefComparator;
import com.xforceplus.ultraman.oqsengine.core.service.utils.StreamMerger;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.DataQueryIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;

import static java.util.stream.Collectors.toList;

/**
 * combined storage
 */
public class CombinedStorage implements MasterStorage, IndexStorage {

    private Logger logger = LoggerFactory.getLogger(CombinedStorage.class);

    private MasterStorage masterStorage;

    private IndexStorage indexStorage;

    private final Map<FieldType, EntityRefComparator> refMapping;

    private final Map<FieldType, String> sortDefaultValue;

    public CombinedStorage(MasterStorage masterStorage, IndexStorage indexStorage) {
        this.masterStorage = masterStorage;
        this.indexStorage = indexStorage;

        refMapping = new HashMap<>();
        refMapping.put(FieldType.BOOLEAN, new EntityRefComparator(FieldType.BOOLEAN));
        refMapping.put(FieldType.DATETIME, new EntityRefComparator(FieldType.DATETIME));
        refMapping.put(FieldType.DECIMAL, new EntityRefComparator(FieldType.DECIMAL));
        refMapping.put(FieldType.ENUM, new EntityRefComparator(FieldType.ENUM));
        refMapping.put(FieldType.LONG, new EntityRefComparator(FieldType.LONG));
        refMapping.put(FieldType.STRING, new EntityRefComparator(FieldType.STRING));
        refMapping.put(FieldType.STRINGS, new EntityRefComparator(FieldType.STRINGS));

        sortDefaultValue = new HashMap();
        sortDefaultValue.put(FieldType.BOOLEAN, Boolean.FALSE.toString());
        sortDefaultValue.put(FieldType.DATETIME, Long.toString(new Date(0).getTime()));
        sortDefaultValue.put(FieldType.LONG, "0");
        sortDefaultValue.put(FieldType.DECIMAL, "0.0");
        sortDefaultValue.put(FieldType.ENUM, "");
        sortDefaultValue.put(FieldType.STRING, "");
        sortDefaultValue.put(FieldType.UNKNOWN, "0");
    }

    private List<EntityRef> merge(Collection<EntityRef> masterRefs, Collection<EntityRef> indexRefs, Sort sort) {
        StreamMerger<EntityRef> streamMerger = new StreamMerger<>();
        FieldType type = sort.getField().type();

        EntityRefComparator entityRefComparator = refMapping.get(type);

        if (entityRefComparator == null) {
            //default
            logger.error("unknown field type !! fallback to string");
            entityRefComparator = new EntityRefComparator(FieldType.STRING);
        }

        //sort masterRefs first
        List<EntityRef> sortedMasterRefs = masterRefs.stream().sorted(sort.isAsc() ? entityRefComparator : entityRefComparator.reversed()).collect(toList());
        return streamMerger.merge(sortedMasterRefs.stream(), indexRefs.stream(), refMapping.get(type), sort.isAsc()).collect(toList());

    }

    @Deprecated
    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page, List<Long> filterIds, Long commitId) throws SQLException {
        throw new RuntimeException("");
    }

    @Deprecated
    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        throw new RuntimeException("");
    }

    @Deprecated
    @Override
    public int delete(long id) throws SQLException {
        throw new RuntimeException("");
    }

    @Override
    public void entityValueToStorage(StorageEntity storageEntity, IEntityValue entityValue) {
        indexStorage.entityValueToStorage(storageEntity, entityValue);
    }

    @Override
    public int batchSave(Collection<StorageEntity> storageEntities, boolean replacement, boolean retry) throws SQLException {
        return indexStorage.batchSave(storageEntities, replacement, retry);
    }

    @Override
    public int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException {
        return indexStorage.buildOrReplace(storageEntity, entityValue, replacement);
    }


    @Override
    public boolean clean(long entityId, long maintainId, long start, long end) throws SQLException {
        return indexStorage.clean(entityId, maintainId, start, end);
    }

    @Override
    public DataQueryIterator newIterator(IEntityClass entityClass, long start, long end, ExecutorService threadPool, int queryTimeout, int pageSize) throws SQLException {
        return masterStorage.newIterator(entityClass, start, end, threadPool, queryTimeout, pageSize);
    }

    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        return masterStorage.selectOne(id, entityClass);
    }

    @Override
    public Optional<IEntityValue> selectEntityValue(long id) throws SQLException {
        return masterStorage.selectEntityValue(id);
    }

    @Override
    public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {
        return masterStorage.selectMultiple(ids);
    }

    @Deprecated
    @Override
    public Collection<EntityRef> select(long commitid, Conditions conditions, IEntityClass entityClass, Sort sort) throws SQLException {
        return null;
    }

    public Collection<EntityRef> select(long commitId, Conditions conditions, IEntityClass entityClass, Sort sort, Page page) throws SQLException {


        Collection<EntityRef> masterRefs = Collections.emptyList();

        if (commitId > 0) {
            //trigger master search
            masterRefs = masterStorage.select(commitId, conditions, entityClass, sort);
        }

        masterRefs = fixNullSortValue(masterRefs, sort);

        /**
         * filter ids
         */
        List<Long> filterIdsFromMaster = masterRefs.stream()
            .filter(x -> x.getOp() == OperationType.DELETE.getValue() || x.getOp() == OperationType.UPDATE.getValue())
            .map(EntityRef::getId)
            .collect(toList());

        Page indexPage = new Page(page.getIndex(), page.getPageSize());
        Collection<EntityRef> refs = indexStorage.select(
            conditions, entityClass, sort, indexPage, filterIdsFromMaster, commitId);

        List<EntityRef> masterRefsWithoutDeleted = masterRefs.stream().
            filter(x -> x.getOp() != OperationType.DELETE.getValue()).collect(toList());

        List<EntityRef> retRefs = new LinkedList<>();
        //combine two refs
        if (sort != null && !sort.isOutOfOrder()) {
            retRefs.addAll(merge(masterRefsWithoutDeleted, refs, sort));
        } else {
            retRefs.addAll(masterRefsWithoutDeleted);
            retRefs.addAll(refs);
        }

        page.setTotalCount(indexPage.getTotalCount() + masterRefsWithoutDeleted.size());
        PageScope scope = page.getNextPage();
        long pageSize = page.getPageSize();

        long skips = scope == null ? 0 : scope.getStartLine();
        List<EntityRef> limitedSelect = retRefs.stream().skip(skips < 0 ? 0 : skips).limit(pageSize).collect(toList());
        return limitedSelect;
    }

    // 如果排序,但是查询结果没有值.
    private Collection<EntityRef> fixNullSortValue(Collection<EntityRef> refs, Sort sort) {
        if (sort != null && !sort.isOutOfOrder()) {
            refs.parallelStream().forEach(r -> {
                if (r.getOrderValue() == null || r.getOrderValue().isEmpty()) {
                    r.setOrderValue(sortDefaultValue.get(sort.getField().type()));
                    // 如果是意外的字段,那么设置为一个字符串0,数字和字符串都可以正常转型.
                    if (r.getOrderValue() == null) {
                        r.setOrderValue("0");
                    }
                }
            });
        }

        return refs;
    }

    @Override
    public int synchronize(long id, long child) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int synchronizeToChild(IEntity entity) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int build(IEntity entity) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(IEntity entity) throws SQLException {
        throw new UnsupportedOperationException();
    }
}
