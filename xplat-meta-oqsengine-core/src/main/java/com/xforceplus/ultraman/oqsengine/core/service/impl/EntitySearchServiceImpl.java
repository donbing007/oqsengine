package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * entity 搜索服务.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:53
 * @since 1.8
 */
public class EntitySearchServiceImpl implements EntitySearchService {

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        return masterStorage.select(id, entityClass);
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

        // 需要保证顺序
        return refs.stream().map(r -> buildEntity(r, entityClass, entityTable)).collect(Collectors.toList());

    }

    // 根据 id 转换实际 entity.
    private IEntity buildEntity(EntityRef ref, IEntityClass entityClass, Map<Long, IEntity> entityTable) {
        if (entityClass.extendEntityClass() == null) {

            return entityTable.get(ref.getId());

        } else {

            IEntity child = entityTable.get(ref.getId());
            IEntity parent = entityTable.get(ref.getPref());

            // 合并父类属性
            child.entityValue().addValues(parent.entityValue().values());

            return child;

        }
    }
}
