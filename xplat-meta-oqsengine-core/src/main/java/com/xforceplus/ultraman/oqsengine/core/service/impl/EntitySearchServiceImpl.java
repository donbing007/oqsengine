package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
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
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

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

    @Resource(name = "ioThreadPool")
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

    public int getMaxJoinEntityNumber() {
        return maxJoinEntityNumber;
    }

    public void setMaxJoinEntityNumber(int maxJoinEntityNumber) {
        this.maxJoinEntityNumber = maxJoinEntityNumber;
    }

    public int getMaxJoinDriverLineNumber() {
        return maxJoinDriverLineNumber;
    }

    public void setMaxJoinDriverLineNumber(int maxJoinDriverLineNumber) {
        this.maxJoinDriverLineNumber = maxJoinDriverLineNumber;
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
                    toMap(c -> c.family().parent(), c -> c.entityClass().extendEntityClass(), (c0, c1) -> c0)
                );

                Collection<IEntity> parentEntities = masterStorage.selectMultiple(request);
                Map<Long, IEntity> parentEntityMap =
                    parentEntities.stream().collect(toMap(p -> p.id(), p -> p, (p0, p1) -> p0));


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
        return selectByConditions(conditions, entityClass, Sort.buildOutOfSort(), page);
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"action", "condition"})
    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException {

        if (conditions == null) {
            throw new SQLException("Incorrect query condition.");
        }

        if (entityClass == null) {
            throw new SQLException("Invalid entityClass.");
        }

        Conditions useConditions = conditions;
        Sort useSort = sort;
        Page usePage = page;
        if (useSort == null) {
            useSort = Sort.buildOutOfSort();
        }
        if (usePage == null) {
            usePage = new Page();
        }

        try {
            // join
            Collection<IEntityClass> entityClassCollection = collectEntityClass(conditions, entityClass);

            final int onlyOneEntityClass = 1;
            if (entityClassCollection.size() > onlyOneEntityClass) {

                if (entityClassCollection.size() > maxJoinEntityNumber) {
                    throw new SQLException(
                        String.format("Join queries can be associated with at most %d entities.", maxJoinEntityNumber));
                }

                /**
                 * 得到了所有非OR开始及其子孙结点没有 OR 的子树.即所有子孙结点没有一个是 OR 关联的子树根结点.
                 * 每一个子树都是 AND 的组合,或者只有一个值结点.
                 *
                 *            and(0)
                 *     or(1)           and(2)
                 * c1       c2    c3         c4
                 *
                 * 目标为得到
                 *   c1   值结点恒为非 OR.
                 *   c2   值结点恒为非 OR.
                 *   and(2) 本身和子孙结点,没有任何 or 结点.
                 *
                 * 三个结点.
                 */
                Collection<ConditionNode> safeNodes = conditions.collectSubTree(c -> !c.isRed(), true);

                /**
                 * 所有的安全结点组成的 Conditions 集合.最终这些条件将会以 OR 连接起来做为最终查询.
                 * 这些条件中的关联 entity 已经被替换成了合式的条件.
                 */
                Collection<Conditions> subConditions = new ArrayList(safeNodes.size());
                IEntityClassReader entityClassReader = new IEntityClassReader(entityClass);

                for (ConditionNode safeNode : safeNodes) {

                    subConditions.add(buildSafeNodeConditions(safeNode, entityClassReader));

                }

                useConditions = Conditions.buildEmtpyConditions();
                for (Conditions cs : subConditions) {
                    if (cs.size() > 0) {
                        useConditions.addOr(cs, false);
                    }
                }

                if (useConditions.size() == 0) {
                    return Collections.emptyList();
                }

            }

            Collection<EntityRef> refs = indexStorage.select(useConditions, entityClass, useSort, usePage);

            return buildEntities(refs, entityClass);


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
        Set<IEntityClass> entityClasses = conditions.collectCondition().stream().map(c -> {
            if (c.getEntityClass().isPresent()) {
                return c.getEntityClass().get();
            } else {
                return mainEntityClass;
            }
        }).collect(Collectors.toSet());

        // 防止条件中没有出现非驱动 entity 的字段条件.
        entityClasses.add(mainEntityClass);

        return entityClasses;
    }


    private Collection<IEntity> buildEntities(Collection<EntityRef> refs, IEntityClass entityClass) throws SQLException {
        if (refs.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, IEntityClass> batchSelect =
            refs.parallelStream().filter(e -> e.getId() > 0)
                .collect(toMap(EntityRef::getId, e -> entityClass, (e0, e1) -> e0));

        // 有继承
        if (entityClass.extendEntityClass() != null) {
            batchSelect.putAll(
                refs.parallelStream().filter(e -> e.getPref() > 0)
                    .collect(toMap(EntityRef::getPref, e -> entityClass.extendEntityClass(), (e0, e1) -> e0)));
        }

        Collection<IEntity> entities = masterStorage.selectMultiple(batchSelect);

        //生成 entity 速查表
        Map<Long, IEntity> entityTable =
            entities.stream().collect(toMap(IEntity::id, e -> e, (e0, e1) -> e0));

        Collection<IEntity> resultEntities = new ArrayList<>(refs.size());
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

    /**
     * 将安全条件结点处理成可查询的 Conditions 实例.
     * ignoreEntityClass 表示不需要处理的条件.
     */
    private Conditions buildSafeNodeConditions(ConditionNode safeNode, IEntityClassReader entityClassReader)
        throws SQLException {

        Conditions processConditions = new Conditions(safeNode);

        // 只包含驱动 entity 条件的集合.
        Collection<Condition> driverConditionCollection = processConditions.collectCondition().stream()
            .filter(c -> c.getEntityClass().isPresent())
            .collect(toList());

        // 按照驱动 entity 的 entityClass 和关联字段来分组条件.
        Map<DriverEntityKey, Conditions> driverEntityConditionsGroup = splitEntityClassCondition(
            driverConditionCollection,
            entityClassReader);

        // driver 数据收集 future.
        List<Future<Map.Entry<DriverEntityKey, Collection<EntityRef>>>> futures =
            new ArrayList<>(driverEntityConditionsGroup.size());
        /**
         * 过滤掉所有 entityClass 等于 ignoreEntityClass
         * 并将剩余的构造成 DriverEntityTask 实例交由线程池执行.
         */
        driverEntityConditionsGroup.entrySet().stream()
            .map(entry -> new DriverEntityTask(entry.getKey(), entry.getValue()))
            .forEach(driverEntityTask -> futures.add(threadPool.submit(driverEntityTask)));

        Conditions conditions = Conditions.buildEmtpyConditions();
        // 是否应该提前结束.
        boolean termination = false;
        for (Future<Map.Entry<DriverEntityKey, Collection<EntityRef>>> future : futures) {

            try {
                Map.Entry<DriverEntityKey, Collection<EntityRef>> driverQueryResult = future.get();
                /**
                 * 由于所有安全结点都以 and 连接,所以当其中任意一个 driver 条件出现0匹配时安全结点都应该被修剪.
                 */
                if (driverQueryResult.getValue().isEmpty()) {
                    termination = true;
                    break;
                }

                conditions.addAnd(
                    new Condition(
                        driverQueryResult.getKey().relationshipField,
                        ConditionOperator.MULTIPLE_EQUALS,
                        driverQueryResult.getValue().stream()
                            .map(ref -> new LongValue(driverQueryResult.getKey().relationshipField, ref.getId()))
                            .toArray(LongValue[]::new)
                    ));
            } catch (Exception e) {

                termination = true;
                throw new SQLException(e.getMessage(), e);

            } finally {

                if (termination) {

                    // 中止其他操作.
                    futures.stream().forEach(f -> f.cancel(true));
                }
            }
        }


        // 之前过滤掉了非 driver 的条件,这里需要加入.
        processConditions.collectCondition().stream().filter(c -> !c.getEntityClass().isPresent()).forEach(c -> {
                conditions.addAnd(c);
            }
        );

        return conditions;
    }

    /**
     * 将不同的 entityClass 的条件进行分组.
     * 不同的 entityClass 关联不同的 Field 将认为是不同的组.
     * 不能处理非 driver 的 entity 查询条件.
     */
    private Map<DriverEntityKey, Conditions> splitEntityClassCondition(
        Collection<Condition> conditionCollection, IEntityClassReader reader) throws SQLException {


        Map<DriverEntityKey, Conditions> result = new HashMap(conditionCollection.size());

        Optional<IEntityClass> entityClassOptional = null;
        Optional<IEntityField> relationshipFieldOptional = null;
        IEntityClass entityClass;
        DriverEntityKey key;
        Conditions driverConditions;
        for (Condition c : conditionCollection) {
            entityClassOptional = c.getEntityClass();
            if (entityClassOptional.isPresent()) {
                entityClass = entityClassOptional.get();
            } else {
                throw new SQLException("An attempt was made to correlate the query, but the entityClass for the driver table was not set!");
            }

            relationshipFieldOptional = reader.getRelatedOriginalField(c.getField());
            if (relationshipFieldOptional.isPresent()) {
                key = new DriverEntityKey(entityClass, relationshipFieldOptional.get());
                driverConditions = result.get(key);
                if (driverConditions == null) {
                    driverConditions = Conditions.buildEmtpyConditions();
                    result.put(key, driverConditions);
                }

                driverConditions.addAnd(c);
            }
        }

        return result;
    }

    /**
     * 驱动 entity 的条件分组 key.
     * 以 entityClass 和其关联的主 entityClass 中字段为区分.
     */
    private class DriverEntityKey {
        private IEntityClass entityClass;
        private IEntityField relationshipField;

        public DriverEntityKey(IEntityClass entityClass, IEntityField relationshipField) {
            this.entityClass = entityClass;
            this.relationshipField = relationshipField;
        }

        public IEntityClass getEntityClass() {
            return entityClass;
        }

        public IEntityField getRelationshipField() {
            return relationshipField;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof DriverEntityKey)) {
                return false;
            }
            DriverEntityKey that = (DriverEntityKey) o;
            if (entityClass.id() != that.entityClass.id()) {
                return false;
            }
            if (relationshipField.id() != that.relationshipField.id()) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityClass.id(), relationshipField.id());
        }
    }

    /**
     * 驱动 entity 的 条件id列表 构造任务.
     */
    private class DriverEntityTask implements Callable<Map.Entry<DriverEntityKey, Collection<EntityRef>>> {

        private DriverEntityKey key;
        private Conditions conditions;

        public DriverEntityTask(DriverEntityKey key, Conditions conditions) {
            this.key = key;
            this.conditions = conditions;
        }

        @Override
        public Map.Entry<DriverEntityKey, Collection<EntityRef>> call() throws Exception {
            long count = checkLineNumber();
            if (count == 0) {
                return new AbstractMap.SimpleEntry<>(key, Collections.emptyList());
            }

            Collection<EntityRef> refs = indexStorage.select(
                conditions, key.getEntityClass(), Sort.buildOutOfSort(), Page.newSinglePage(maxJoinDriverLineNumber));
            return new AbstractMap.SimpleEntry<>(key, refs);
        }

        // 检查命中数据集大小.
        private long checkLineNumber() throws SQLException {
            Page emptyPage = Page.emptyPage();
            indexStorage.select(conditions, key.getEntityClass(), Sort.buildOutOfSort(), emptyPage);

            if (emptyPage.getTotalCount() > maxJoinDriverLineNumber) {
                throw new SQLException(String.format("Drives entity(%s) data exceeding %d.",
                    key.getEntityClass().code(), maxJoinDriverLineNumber));
            }

            return emptyPage.getTotalCount();
        }
    }
}
