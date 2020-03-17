package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * entity 搜索服务.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:53
 * @since 1.8
 */
public class EntitySearchServiceImpl implements EntitySearchService {


    final Logger logger = LoggerFactory.getLogger(EntitySearchServiceImpl.class);

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        Optional<IEntity> entityOptional = masterStorage.select(id, entityClass);

        if (entityOptional.isPresent()) {

            if (entityClass.extendEntityClass() != null) {

                /**
                 * 查询出子类,需要加载父类信息.
                 */
                IEntity child = entityOptional.get();

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "The query object is a subclass, loading the parent class data information.[id={},parent=[]]",
                        id, child.family().parent());
                }

                if (child.family().parent() == 0) {
                    throw new SQLException(
                        String.format("A fatal error, unable to find parent data (%d) for data (%d).",
                            child.family().parent(), id));
                }

                Optional<IEntity> parentOptional =
                    masterStorage.select(child.family().parent(), entityClass.extendEntityClass());

                if (parentOptional.isPresent()) {

                    merageChildAndParent(child, parentOptional.get());

                } else {

                    throw new SQLException(
                        String.format("A fatal error, unable to find parent data (%d) for data (%d)",
                            child.family().parent(), id));
                }

            }

        }

        return entityOptional;
    }

    @Override
    public Collection<IEntity> selectMultiple(Long[] ids, IEntityClass entityClass) throws SQLException {
        Map<Long, IEntityClass> request = Arrays.stream(ids).collect(
            Collectors.toMap(i -> i, i -> entityClass, (i0, i1) -> i0));

        return masterStorage.selectMultiple(request);
    }

    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Page page) throws SQLException {
        return selectByConditions(conditions, entityClass, null, page);
    }

    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException {
        Collection<EntityRef> refs = indexStorage.select(conditions, entityClass, sort, page);

        return buildEntities(refs, entityClass);
    }

    private Collection<IEntity> buildEntities(Collection<EntityRef> refs, IEntityClass entityClass) throws SQLException {
        Map<Long, IEntityClass> batchSelect =
            refs.parallelStream().filter(e -> e.getId() > 0)
                .collect(Collectors.toMap(EntityRef::getId, e -> entityClass, (e0, e1) -> e0));

        // 有继承
        if (entityClass.extendEntityClass() != null) {
            batchSelect.putAll(
                refs.parallelStream().filter(e -> e.getPref() > 0)
                    .collect(Collectors.toMap(EntityRef::getPref, e -> entityClass.extendEntityClass(), (e0, e1) -> e0)));
        }

        Collection<IEntity> entities = masterStorage.selectMultiple(batchSelect);

        //生成 entity 速查表
        Map<Long, IEntity> entityTable =
            entities.stream().collect(Collectors.toMap(IEntity::id, e -> e, (e0, e1) -> e0));

        List<IEntity> resultEntities = new ArrayList<>(refs.size());
        IEntity resultEntity = null;
        for (EntityRef ref : refs) {
            resultEntity = buildEntity(ref, entityClass, entityTable);
            if (resultEntity != null) {
                resultEntities.add(resultEntity);
            }
        }

        // 需要保证顺序
        return resultEntities;

    }

    // 根据 id 转换实际 entity.
    private IEntity buildEntity(EntityRef ref, IEntityClass entityClass, Map<Long, IEntity> entityTable)
        throws SQLException {
        if (entityClass.extendEntityClass() == null) {

            IEntity entity = entityTable.get(ref.getId());

            if (entity == null) {
                throw new SQLException(String.format("A fatal error, unable to find data (%d).", ref.getId()));
            }

            return entity;

        } else {

            IEntity child = entityTable.get(ref.getId());

            if (ref.getPref() == 0) {
                throw new SQLException(
                    String.format("A fatal error, unable to find parent data (%d) for data (%d).",
                        ref.getId(), ref.getPref()));
            }

            IEntity parent = entityTable.get(ref.getPref());

            // 子类数据和父类数据有一个不存在即判定无法构造.
            if (child == null) {
                throw new SQLException(String.format("A fatal error, unable to find data (%d).", ref.getId()));
            }

            if (parent == null) {
                throw new SQLException(
                    String.format("A fatal error, unable to find parent data (%d) for data (%d).",
                        ref.getId(), ref.getPref()));
            }

            merageChildAndParent(child, parent);

            return child;

        }
    }

    // 合并子类和父类属性,同样字段子类会覆盖父类.
    private void merageChildAndParent(IEntity child, IEntity parent) {
        Collection<IValue> childValues = new ArrayList(child.entityValue().values());
        child.entityValue().clear()
            .addValues(parent.entityValue().values())
            .addValues(childValues);
    }
}
