package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AnyEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    final Logger logger = LoggerFactory.getLogger(EntityManagementServiceImpl.class);

    @Resource
    private LongIdGenerator idGenerator;

    @Resource(name = "serviceTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    private Counter inserCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "build");
    private Counter replaceCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "replace");
    private Counter deleteCountTotal = Metrics.counter(MetricsDefine.WRITE_COUNT_TOTAL, "action", "delete");
    private Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);


    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "build"})
    @Override
    public IEntity build(IEntity entity) throws SQLException {

        // 克隆一份,后续的修改不影响入参.
        IEntity entityClone;
        try {
            entityClone = (IEntity) entity.clone();
        } catch (CloneNotSupportedException e) {
            failCountTotal.increment();
            throw new SQLException(e.getMessage(), e);
        }

        try {

            return (IEntity) transactionExecutor.execute(r -> {

                if (isSub(entityClone)) {
                    // 处理父类
                    long fatherId = idGenerator.next();
                    long childId = idGenerator.next();

                    IEntity fathcerEntity = buildFatherEntity(entityClone, childId);
                    fathcerEntity.resetId(fatherId);

                    IEntity childEntity = buildChildEntity(entityClone, fatherId);
                    childEntity.resetId(childId);

                    // master
                    masterStorage.build(fathcerEntity); // father
                    masterStorage.build(childEntity); // child


                    indexStorage.build(buildIndexEntity(fathcerEntity)); // fatcher

                    /**
                     * 索引中子类包含父类所有属性,保证可以使用父类属性查询子类.
                     * 这里直接使用外界传入的 entity 实例,重置 id 为新的子类 id.
                     * entity 设置为了传入 entity 可以有新的 id.
                     */
                    entity.resetId(childId);
                    entityClone.resetId(childId);
                    /**
                     * 索引中只存放可搜索字段,子类包含父类和本身的所有可搜索字段.
                     * 这里先将父的属性合并进来过滤再储存.
                     */
                    IEntity indexEntity = buildIndexEntity(entityClone);
                    // 来源于外部 entity,所以这里需要调整继承家族信息.
                    indexEntity.resetFamily(new EntityFamily(fatherId, 0));
                    indexStorage.build(indexEntity); // child


                    entity.resetFamily(childEntity.family());
                    return entity;

                } else {

                    entity.resetId(idGenerator.next());
                    entityClone.resetId(entity.id());

                    masterStorage.build(entityClone);
                    indexStorage.build(buildIndexEntity(entityClone));

                    return entity;
                }

            });
        } catch (Exception ex) {

            failCountTotal.increment();
            throw ex;

        } finally {

            inserCountTotal.increment();

        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "replace"})
    @Override
    public void replace(IEntity entity) throws SQLException {

        if (!masterStorage.select(entity.id(), entity.entityClass()).isPresent()) {
            failCountTotal.increment();
            throw new SQLException(String.format("An Entity that does not exist cannot be updated (%d).", entity.id()));
        }

        // 克隆一份,后续的修改不影响入参.
        IEntity target;
        try {
            target = (IEntity) entity.clone();
        } catch (CloneNotSupportedException e) {
            replaceCountTotal.increment();
            throw new SQLException(e.getMessage(), e);
        }

        try {
            transactionExecutor.execute(r -> {

                if (isSub(entity)) {

                    /**
                     * 拆分为父与子.
                     */
                    IEntity fatherEntity = buildFatherEntity(target, target.id());
                    fatherEntity.resetId(entity.family().parent());

                    IEntity childEntity = buildChildEntity(target, target.family().parent());

                    masterStorage.replace(fatherEntity);
                    masterStorage.replace(childEntity);

                    // 子类的索引需要父和子所有属性.
                    indexStorage.replace(buildIndexEntity(fatherEntity));

                    indexStorage.replace(buildIndexEntity(target));

                } else {

                    masterStorage.replace(target);

                    IEntity indexEntity = buildIndexEntity(target);
                    indexStorage.replace(indexEntity);

                    // 有子类
                    if (target.family().child() > 0) {
                        // 父子同步
                        masterStorage.synchronize(target.id(), target.family().child());

                        // 同步子类索引信息.
                        IEntityValue childIndexValue = new EntityValue(target.family().child());
                        childIndexValue.addValues(indexEntity.entityValue().values());
                        indexStorage.replaceAttribute(childIndexValue);
                    }
                }

                return null;
            });
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            replaceCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "delete"})
    @Override
    public void delete(IEntity entity) throws SQLException {

        try {
            transactionExecutor.execute(r -> {

                if (isSub(entity)) {

                    IEntity fatherEntity = buildFatherEntity(entity, entity.id());
                    fatherEntity.resetId(entity.family().parent());

                    IEntity childEntity = buildChildEntity(entity, entity.family().parent());

                    masterStorage.delete(fatherEntity);
                    masterStorage.delete(childEntity);

                    indexStorage.delete(fatherEntity);
                    indexStorage.delete(entity);

                } else {

                    masterStorage.delete(entity);
                    indexStorage.delete(entity);

                    // 有子类需要删除.
                    if (entity.family().child() > 0) {

                        IEntity chlidEntity = new Entity(
                            entity.family().child(),
                            AnyEntityClass.getInstance(),
                            new EntityValue(entity.family().child()),
                            entity.version()
                        );

                        masterStorage.delete(chlidEntity);
                        indexStorage.delete(chlidEntity);
                    }
                }
                return null;
            });
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            deleteCountTotal.increment();
        }
    }

    // 构造一个适合索引的 IEntity 实例.
    private IEntity buildIndexEntity(IEntity target) {
        target.entityValue().filter(v -> v.getField().config().isSearchable());
        return target;
    }

    private boolean isSub(IEntity entity) {
        return entity.entityClass().extendEntityClass() != null;
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
