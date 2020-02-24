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
import java.util.Optional;

/**
 * entity 搜索服务.
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

        return buildEntities(refs);
    }

    private Collection<IEntity> buildEntities(Collection<EntityRef> refs) {
        //TODO: 还未实现.

        throw new UnsupportedOperationException();
    }
}
