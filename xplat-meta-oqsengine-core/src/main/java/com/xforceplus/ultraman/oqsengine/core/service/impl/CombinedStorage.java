package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.utils.StreamMerger;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * combined storage
 */
public class CombinedStorage implements MasterStorage, IndexStorage {

    private MasterStorage masterStorage;

    private IndexStorage indexStorage;

    private Map<FieldType, EntityRefComparator> refMapping = new HashMap<>();

    private Logger logger = LoggerFactory.getLogger(CombinedStorage.class);

    public CombinedStorage(MasterStorage masterStorage, IndexStorage indexStorage) {
        this.masterStorage = masterStorage;
        this.indexStorage = indexStorage;

        refMapping.put(FieldType.BOOLEAN, new EntityRefComparator(FieldType.BOOLEAN));
        refMapping.put(FieldType.DATETIME, new EntityRefComparator(FieldType.DATETIME));
        refMapping.put(FieldType.DECIMAL, new EntityRefComparator(FieldType.DECIMAL));
        refMapping.put(FieldType.ENUM, new EntityRefComparator(FieldType.ENUM));
        refMapping.put(FieldType.LONG, new EntityRefComparator(FieldType.LONG));
        refMapping.put(FieldType.STRING, new EntityRefComparator(FieldType.STRING));
        refMapping.put(FieldType.STRINGS, new EntityRefComparator(FieldType.STRINGS));
    }

    private List<EntityRef> merge(Collection<EntityRef> masterRefs, Collection<EntityRef> indexRefs, Sort sort) {
        StreamMerger<EntityRef> streamMerger = new StreamMerger<>();
        FieldType type = sort.getField().type();

        EntityRefComparator entityRefComparator = refMapping.get(type);

        if(entityRefComparator == null) {
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
    public int buildOrReplace(StorageEntity storageEntity, IEntityValue entityValue, boolean replacement) throws SQLException {
        return indexStorage.buildOrReplace(storageEntity, entityValue, replacement);
    }

    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        return masterStorage.selectOne(id, entityClass);
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

        /**
         * filter ids
         */
        List<Long> filterIdsFromMaster = masterRefs.stream()
                .filter(x -> x.getOp() == OperationType.DELETE.getValue() || x.getOp() == OperationType.UPDATE.getValue())
                .map(EntityRef::getId)
                .collect(toList());

        Collection<EntityRef> refs = indexStorage.select(conditions, entityClass, sort, page, filterIdsFromMaster, commitId);

        //TODO sort transform

        List<EntityRef> masterRefsWithoutDeleted = masterRefs.stream().filter(x -> x.getOp() != OperationType.DELETE.getValue()).collect(toList());

        List<EntityRef> retRefs = new LinkedList<>();
        //combine two refs
        if (sort != null && !sort.isOutOfOrder()) {
            retRefs.addAll(merge(masterRefsWithoutDeleted, refs, sort));
        } else {
            retRefs.addAll(masterRefsWithoutDeleted);
            retRefs.addAll(refs);
        }

        long start = page.getIndex();
        long pageSize = page.getPageSize();

        //update totalCount
        long totalCount = page.getTotalCount();
        page.setTotalCount(totalCount + masterRefsWithoutDeleted.size());

        long skips = (start - 1) * pageSize;
        List<EntityRef> limitedSelect = retRefs.stream().skip(skips < 0 ? 0 : skips).limit(pageSize).collect(toList());
        return limitedSelect;
    }

    @Override
    public int synchronize(long id, long child) throws SQLException {
        return masterStorage.synchronize(id, child);
    }

    @Override
    public int build(IEntity entity) throws SQLException {
        return masterStorage.build(entity);
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        return masterStorage.replace(entity);
    }

    @Override
    public int delete(IEntity entity) throws SQLException {
        return masterStorage.delete(entity);
    }
}
