package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.Task;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * entity 管理服务实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:12
 * @since 1.8
 */
public class EntityManagementServiceImpl implements EntityManagementService {

    @Resource
    private LongIdGenerator idGenerator;

    @Resource
    private TransactionExecutor transactionExecutor;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Override
    public long build(IEntity entity) throws SQLException {

        return (long) transactionExecutor.execute(r -> {

            if (isSub(entity)) {
                // 处理父类
                long fatherId = idGenerator.next();
                long childId = idGenerator.next();

                IEntity fathcerEntity = buildFatherEntity(entity, childId);
                fathcerEntity.resetId(fatherId);

                IEntity childEntity = buildChildEntity(entity, fatherId);
                childEntity.resetId(childId);

                // master
                masterStorage.build(fathcerEntity); // father
                masterStorage.build(childEntity); // child

                // index
                indexStorage.build(fathcerEntity); // fatcher

                /**
                 * 索引中子类包含父类所有属性,保证可以使用父类属性查询子类.
                 * 这里直接使用外界传入的 entity 实例,不这重置 id 为新的子类 id.
                 */
                entity.resetId(childId);
                indexStorage.build(entity); // child

                return childId;

            } else {

                entity.resetId(idGenerator.next());

                masterStorage.build(entity);
                indexStorage.build(entity);

                return entity.id();
            }

        });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {

        transactionExecutor.execute(r -> {
            masterStorage.replace(entity);

            if (isSub(entity)) {

                /**
                 * 拆分为父与子.
                 */
                IEntity fatherEntity = buildFatherEntity(entity, entity.id());
                IEntity childEntity = buildChildEntity(entity,entity.family().parent());

                masterStorage.replace(fatherEntity);
                masterStorage.replace(childEntity);

                indexStorage.replace(fatherEntity);
                indexStorage.replace(entity);

            } else {

                masterStorage.replace(entity);
                indexStorage.replace(entity);
            }

            return null;
        });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {

        transactionExecutor.execute(r -> {

            if (isSub(entity)) {

                IEntity fatherEntity = buildFatherEntity(entity, entity.id());
                IEntity childEntity = buildChildEntity(entity,entity.family().parent());

                masterStorage.delete(fatherEntity);
                masterStorage.delete(childEntity);

                indexStorage.delete(fatherEntity);
                indexStorage.delete(entity);

            } else {
                masterStorage.delete(entity);
                indexStorage.delete(entity);
            }
            return null;
        });
    }

    private boolean isSub(IEntity entity) {
        return entity.family().parent() > 0;
    }

    private IEntity buildChildEntity(IEntity entity, long pref) {
        return build(entity, entity.entityClass(), new EntityFamily(pref, 0));
    }

    private IEntity buildFatherEntity(IEntity entity, long cref) {
        return build(entity, entity.entityClass().extendEntityClass(), new EntityFamily(0, cref));
    }

    private IEntity build(IEntity entity, IEntityClass entityClass, IEntityFamily family) {
        // 当前属于子类的属性速查表.
        Map<IEntityField, Object> fieldTable =
            entityClass.fields().stream().collect(Collectors.toMap(v -> v, v -> ""));

        IEntityValue newValues = new EntityValue(entityClass.id());
        entity.entityValue().values().stream()
            .filter(v -> fieldTable.containsKey(v.getField()))
            .forEach(v -> {
                newValues.addValue(v);
            });

        //TODO: 外键关系未分离.
        return new Entity(entity.id(), entityClass, newValues, null, family, entity.version());
    }

}
