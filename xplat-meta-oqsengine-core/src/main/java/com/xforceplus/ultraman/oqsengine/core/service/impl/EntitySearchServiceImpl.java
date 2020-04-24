package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * entity 搜索服务.
 *
 * @author dongbin
 * @version 0.1 2020/2/18 14:53
 * @since 1.8
 */
public class EntitySearchServiceImpl implements EntitySearchService {


    final Logger logger = LoggerFactory.getLogger(EntitySearchServiceImpl.class);

    /**
     * 最大允许的 join 数量,
     */
    final int DEFAULT_MAX_JOIN_ENTITY_NUMBER = 2;

    /**
     * 驱动关联表的匹配数据上限.
     */
    final int DEFAULT_MAX_JOIN_DRIVER_LINE_NUMBER = 1000;

    private Counter oneReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "one");
    private Counter multipleReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "multiple");
    private Counter searchReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "search");
    private Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Resource
    private ExecutorService threadPool;

    private int maxJoinEntityNumber;
    private int maxJoinDriverLineNumber;

    @PostConstruct
    public void init() {
        if (maxJoinEntityNumber <= 0) {
            maxJoinEntityNumber = DEFAULT_MAX_JOIN_ENTITY_NUMBER;
        }

        if (maxJoinDriverLineNumber <= 0) {
            maxJoinDriverLineNumber = DEFAULT_MAX_JOIN_DRIVER_LINE_NUMBER;
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "one"})
    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {

        try {
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
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            oneReadCountTotal.increment();
        }

    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "multiple"})
    @Override
    public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {

        Map<Long, IEntityClass> request = new HashMap<>(ids.length);
        for (long id : ids) {
            request.put(id, entityClass);
        }

        try {
            Collection<IEntity> entities = masterStorage.selectMultiple(request);

            // 如果有继承关系.
            if (entityClass.extendEntityClass() != null) {
                request = entities.stream().collect(
                    Collectors.toMap(c -> c.family().parent(), c -> c.entityClass().extendEntityClass(), (c0, c1) -> c0)
                );

                Collection<IEntity> parentEntities = masterStorage.selectMultiple(request);
                Map<Long, IEntity> parentEntityMap =
                    parentEntities.stream().collect(Collectors.toMap(p -> p.id(), p -> p, (p0, p1) -> p0));


                IEntity parent;
                for (IEntity child : entities) {
                    parent = parentEntityMap.get(child.family().parent());
                    if (parent == null) {
                        throw new SQLException(
                            String.format("A fatal error, unable to find parent data (%d) for data (%d).",
                                child.family().parent(), child.id()));
                    }

                    this.merageChildAndParent(child, parent);
                }
            }

            return entities;
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            multipleReadCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "condition"})
    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Page page) throws SQLException {
        return selectByConditions(conditions, entityClass, null, page);
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "condition"})
    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException {
        try {
            Collection<IEntityClass> entityClassCollection = collectEntityClass(conditions, entityClass);

            final int onlyOneEntityClass = 1;
            if (entityClassCollection.size() > onlyOneEntityClass) {

                if (entityClassCollection.size() > maxJoinDriverLineNumber) {
                    throw new SQLException(
                        String.format("Join queries can be associated with at most %d entities.", maxJoinEntityNumber));
                }

                /**
                 * 得到了所有非红色开始的子树.即所有子孙结点没有一个是 OR 关联的子树根结点.
                 * 每一个子树都是 AND 的组合,或者只有一个值结点.
                 *
                 *            and
                 *     or            and
                 * c1     c2    c3         c4
                 * 目标为得到
                 *   c1
                 *   c2
                 *    and
                 * c3      c4
                 * 三个结点.
                 */
                Collection<ConditionNode> greenConditionNode = conditions.collectSubTree(c -> !c.isRed(), true);

                for (ConditionNode greenNode : greenConditionNode) {

                }

            } else {

                Collection<EntityRef> refs = indexStorage.select(conditions, entityClass, sort, page);

                return buildEntities(refs, entityClass);
            }
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            searchReadCountTotal.increment();
        }
    }

    /**
     * 收集条件中的 entityClass
     */
    private Collection<IEntityClass> collectEntityClass(Conditions conditions, IEntityClass mainEntityClass) {
        return conditions.collectCondition().stream().map(c -> {
            if (c.getEntityClass().isPresent()) {
                return c.getEntityClass().get();
            } else {
                return mainEntityClass;
            }
        }).collect(Collectors.toSet());
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
        child.entityValue().addValues(parent.entityValue().values());
    }

    private Map<IEntityClass, Collection<Condition>> splitEntityClassCondition(
        ConditionNode conditionNode, IEntityClass defaultEntityClass) {

        Conditions conditions = new Conditions(conditionNode);

        return conditions.collectCondition().stream().collect(Collectors.groupingBy(condition -> {
                Optional<IEntityClass> entityClassOptional = condition.getEntityClass();
                if (entityClassOptional.isPresent()) {
                    return entityClassOptional.get();
                } else {
                    return defaultEntityClass;
                }
            })
        );
    }

    /**
     * 驱动 entity 的id 构造任务.
     */
    private class DriverEntityTask implements Callable<Collection<EntityRef>> {

        private Conditions conditions;

        public DriverEntityTask(ConditionNode conditionNode) {
            conditions = new Conditions(conditionNode);
        }

        @Override
        public Collection<EntityRef> call() throws Exception {

        }
    }

}
