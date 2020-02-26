package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;

import javax.annotation.Resource;
import java.sql.SQLException;
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

    @Resource(name = "serviceTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;


    @Override
    public long build(IEntity entity) throws SQLException {

        // 克隆一份,后续的修改不影响入参.
        IEntity target;
        try {
             target = (IEntity) entity.clone();
        } catch (CloneNotSupportedException e) {
            throw new SQLException(e.getMessage(),e);
        }

        return (long) transactionExecutor.execute(r -> {

            if (isSub(target)) {
                // 处理父类
                long fatherId = idGenerator.next();
                long childId = idGenerator.next();

                IEntity fathcerEntity = buildFatherEntity(target, childId);
                fathcerEntity.resetId(fatherId);

                IEntity childEntity = buildChildEntity(target, fatherId);
                childEntity.resetId(childId);

                // master
                masterStorage.build(fathcerEntity); // father
                masterStorage.build(childEntity); // child


                indexStorage.build(buildIndexEntity(fathcerEntity)); // fatcher

                /**
                 * 索引中子类包含父类所有属性,保证可以使用父类属性查询子类.
                 * 这里直接使用外界传入的 entity 实例,重置 id 为新的子类 id.
                 */
                entity.resetId(childId);
                /**
                 * 索引中只存放可搜索字段,子类包含父类和本身的所有可搜索字段.
                 * 这里先将父的属性合并进来过滤再储存.
                 */
                indexStorage.build(buildIndexEntity(target)); // child


                return childId;

            } else {

                entity.resetId(idGenerator.next());

                masterStorage.build(target);
                indexStorage.build(buildIndexEntity(target));

                return entity.id();
            }

        });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {

        // 克隆一份,后续的修改不影响入参.
        IEntity target;
        try {
            target = (IEntity) entity.clone();
        } catch (CloneNotSupportedException e) {
            throw new SQLException(e.getMessage(),e);
        }

        transactionExecutor.execute(r -> {

            if (isSub(entity)) {

                /**
                 * 拆分为父与子.
                 */
                IEntity fatherEntity = buildFatherEntity(target, target.id());
                IEntity childEntity = buildChildEntity(target,target.family().parent());

                masterStorage.replace(fatherEntity);
                masterStorage.replace(childEntity);

                indexStorage.replace(buildIndexEntity(fatherEntity));
                // 子类的索引需要父和子所有属性.

                indexStorage.replace(buildIndexEntity(target));

            } else {

                masterStorage.replace(target);

                indexStorage.replace(buildIndexEntity(target));
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

    // 构造一个适合索引的 IEntity 实例.
    private IEntity buildIndexEntity(IEntity target) {
        target.entityValue().filter(v -> v.getField().config().isSearchable());
        return target;
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

        return new Entity(entity.id(), entityClass, newValues, family, entity.version());
    }

}
