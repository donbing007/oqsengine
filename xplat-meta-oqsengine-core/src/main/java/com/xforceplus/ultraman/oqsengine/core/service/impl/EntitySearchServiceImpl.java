package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.SearchConfig;
import com.xforceplus.ultraman.oqsengine.core.service.utils.EntityClassHelper;
import com.xforceplus.ultraman.oqsengine.core.service.utils.EntityRefComparator;
import com.xforceplus.ultraman.oqsengine.core.service.utils.StreamMerger;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsRelation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
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

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    /**
     * 查询时最大可见数据量.
     */
    final int DEFAULT_MAX_VISIBLE_TOTAL_COUNT = 10000;

    private Counter oneReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "one");
    private Counter multipleReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "multiple");
    private Counter searchReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "search");
    private Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Resource(name = "callReadThreadPool")
    private ExecutorService threadPool;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private MetaManager metaManager;

    private long maxVisibleTotalCount;
    private int maxJoinEntityNumber;
    private int maxJoinDriverLineNumber;
    private boolean showResult = false;
    private CombinedStorage combinedStorage;


    @PostConstruct
    public void init() {
        if (maxJoinEntityNumber <= 0) {
            maxJoinEntityNumber = DEFAULT_MAX_JOIN_ENTITY_NUMBER;
        }

        if (maxJoinDriverLineNumber <= 0) {
            maxJoinDriverLineNumber = DEFAULT_MAX_JOIN_DRIVER_LINE_NUMBER;
        }

        if (maxVisibleTotalCount <= 0) {
            maxVisibleTotalCount = DEFAULT_MAX_VISIBLE_TOTAL_COUNT;
        }

        combinedStorage = new CombinedStorage(masterStorage, indexStorage);

        logger.info("Search service startup:[maxVisibleTotal:{}, maxJoinEntityNumber:{}, maxJoinDriverLineNumber:{}]",
            maxVisibleTotalCount, maxJoinEntityNumber, maxJoinDriverLineNumber);


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

    public long getMaxVisibleTotalCount() {
        return maxVisibleTotalCount;
    }

    public void setMaxVisibleTotalCount(long maxVisibleTotalCount) {
        this.maxVisibleTotalCount = maxVisibleTotalCount;
    }

    public boolean isShowResult() {
        return showResult;
    }

    public void setShowResult(boolean showResult) {
        this.showResult = showResult;
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "one"})
    @Override
    public Optional<IEntity> selectOne(long id, EntityClassRef entityClassRef) throws SQLException {

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entityClassRef);
        try {

            Optional<IEntity> entityOptional = masterStorage.selectOne(id, entityClass);
            if (entityOptional.isPresent()) {

                if (isShowResult()) {
                    logger.info("Select one result: [{}].", entityOptional.get());
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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "multiple"})
    @Override
    public Collection<IEntity> selectMultiple(long[] ids, EntityClassRef entityClassRef) throws SQLException {

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entityClassRef);

        try {
            Collection<IEntity> entities = masterStorage.selectMultiple(ids, entityClass);

            if (isShowResult()) {
                entities.stream().forEach(e -> {
                    logger.info("Select multiple result: [{}].", e.toString());
                });
            }

            return entities;
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            multipleReadCountTotal.increment();
        }
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "condition"})
    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, EntityClassRef entityClassRef, Page page)
        throws SQLException {
        return selectByConditions(conditions, entityClassRef, Sort.buildOutOfSort(), page);
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "condition"})
    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, EntityClassRef entityClassRef, Sort sort, Page page)
        throws SQLException {
        return selectByConditions(conditions, entityClassRef,
            SearchConfig.Builder.aSearchConfig().withSort(sort).withPage(page).build());
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "all", "action", "condition"})
    @Override
    public Collection<IEntity> selectByConditions(Conditions conditions, EntityClassRef entityClassRef, SearchConfig config)
        throws SQLException {
        if (conditions == null) {
            throw new SQLException("Incorrect query condition.");
        }

        if (entityClassRef == null) {
            throw new SQLException("Invalid entityClass.");
        }

        if (config == null) {
            throw new SQLException("Invalid search config.");
        }

        /**
         * 数据过滤不会使有和含有模糊搜索的条件.
         */
        if (config.getFilter().isPresent()) {
            Conditions filterCondtiton = config.getFilter().get();
            if (filterCondtiton.haveFuzzyCondition()) {
                throw new SQLException("Data filtering conditions cannot use fuzzy operators.");
            }
        }

        IEntityClass entityClass = EntityClassHelper.checkEntityClass(metaManager, entityClassRef);

        // 检查是否有非可搜索的字段,如果有将空返回.
        boolean checkResult;
        for (Condition c : conditions.collectCondition()) {
            if (c.getEntityClassRef().isPresent()) {
                checkResult = checkCanSearch(c,
                    EntityClassHelper.checkEntityClass(metaManager, c.getEntityClassRef().get()));
            } else {
                checkResult = checkCanSearch(c, entityClass);
            }
            if (!checkResult) {
                if (config.getPage().isPresent()) {
                    Page page = config.getPage().get();
                    page.setTotalCount(0);
                }
                return Collections.emptyList();
            }
        }

        if (isOneIdQuery(conditions)) {
            Condition onlyCondition = conditions.collectCondition().stream().findFirst().get();
            long id = onlyCondition.getFirstValue().valueToLong();
            Optional<IEntity> entityOptional = masterStorage.selectOne(id, entityClass);
            if (entityOptional.isPresent()) {
                return Arrays.asList(entityOptional.get());
            } else {
                return Collections.emptyList();
            }
        }


        Conditions useConditions = conditions;
        Sort useSort = null;
        if (config.getSort().isPresent()) {
            Sort sort = config.getSort().get();
            if (sort.getField() != null  && !sort.getField().config().isSearchable()) {
                useSort = Sort.buildAscSort(EntityField.ID_ENTITY_FIELD);
            }
        } else {
            useSort = Sort.buildAscSort(EntityField.ID_ENTITY_FIELD);
        }

        Page usePage;
        if (!config.getPage().isPresent()) {
            usePage = new Page();
        } else {
            usePage = config.getPage().get();
        }
        usePage.setVisibleTotalCount(maxVisibleTotalCount);

        Optional<Long> minUnSyncCommitIdOp = commitIdStatusService.getMin();
        long minUnSyncCommitId;
        if (!minUnSyncCommitIdOp.isPresent()) {
            minUnSyncCommitId = 0;
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to fetch the commit number, use the default commit number 0.");
            }
        } else {
            minUnSyncCommitId = minUnSyncCommitIdOp.get();
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "The minimum commit number {} that is currently uncommitted was successfully obtained.",
                    minUnSyncCommitId);
            }
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

                for (ConditionNode safeNode : safeNodes) {
                    subConditions.add(buildSafeNodeConditions(safeNode, minUnSyncCommitId));
                }

                useConditions = Conditions.buildEmtpyConditions();
                for (Conditions cs : subConditions) {
                    if (cs.size() > 0) {
                        useConditions.addOr(cs, false);
                    }
                }

                if (useConditions.isEmtpy()) {
                    if (config.getPage().isPresent()) {
                        config.getPage().get().setTotalCount(0);
                    }
                    return Collections.emptyList();
                }
            }

            Collection<EntityRef> refs = combinedStorage.select(
                minUnSyncCommitId, useConditions, entityClass, useSort, usePage);

            Collection<IEntity> entities = buildEntitiesFromRefs(refs, entityClass);

            if (isShowResult()) {
                if (entities.size() == 0) {

                    logger.info("Select conditions result: []");

                } else {
                    entities.stream().forEach(e -> {
                        if (e == null) {
                            logger.info("Select conditions result: [NULL]");
                        } else {
                            logger.info("Select conditions result: [{}],totalCount:[{}]", e.toString(), usePage.getTotalCount());
                        }
                    });
                }
            }

            return entities;
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            searchReadCountTotal.increment();
        }
    }

    /**
     * 以下情况会空返回.
     * 1. 字段不存在.
     * 2. 字段非可搜索.
     * 注意: 如果字段标示为identifie类型,那么会返回true.
     */
    private boolean checkCanSearch(Condition c, IEntityClass entityClass) {
        if (c.getField().config().isIdentifie()) {
            return true;
        }

        Optional<IEntityField> fOp = entityClass.field(c.getField().id());
        if (fOp.isPresent() && fOp.get().config().isSearchable()) {
            return true;
        }

        return false;
    }

    /**
     * 收集条件中的 entityClass
     */
    private Collection<IEntityClass> collectEntityClass(Conditions conditions, IEntityClass mainEntityClass) {
        Set<IEntityClass> entityClasses = conditions.collectCondition().stream().map(c -> {
            if (c.getEntityClassRef().isPresent()) {
                return EntityClassHelper.checkEntityClass(metaManager, c.getEntityClassRef().get());
            } else {
                return mainEntityClass;
            }
        }).collect(toSet());

        // 防止条件中没有出现非驱动 entity 的字段条件.
        entityClasses.add(mainEntityClass);

        return entityClasses;
    }

    /**
     * 将根据数据查询的产生oqsmajor版本号来决定如何处理.
     */
    private Collection<IEntity> buildEntitiesFromRefs(Collection<EntityRef> refs, IEntityClass entityClass)
        throws SQLException {

        if (refs.isEmpty()) {
            return Collections.emptyList();
        }

        long[] ids = refs.stream().mapToLong(ref -> ref.getId()).toArray();
        Collection<IEntity> entities = masterStorage.selectMultiple(ids, entityClass);
        return entities;
    }

    /**
     * 将安全条件结点处理成可查询的 Conditions 实例.
     * ignoreEntityClass 表示不需要处理的条件.
     */
    private Conditions buildSafeNodeConditions(ConditionNode safeNode, long commitId)
        throws SQLException {

        Conditions processConditions = new Conditions(safeNode);

        // 只包含驱动 entity 条件的集合.
        Collection<Condition> driverConditionCollection = processConditions.collectCondition().stream()
            .filter(c -> c.getEntityClassRef().isPresent())
            .collect(toList());

        // 按照驱动 entity 的 entityClass 和关联字段来分组条件.
        Map<DriverEntityKey, Conditions> driverEntityConditionsGroup = splitEntityClassCondition(driverConditionCollection);

        // driver 数据收集 future.
        List<Future<Map.Entry<DriverEntityKey, Collection<EntityRef>>>> futures =
            new ArrayList<>(driverEntityConditionsGroup.size());
        /**
         * 过滤掉所有 entityClass 等于 ignoreEntityClass
         * 并将剩余的构造成 DriverEntityTask 实例交由线程池执行.
         */
        driverEntityConditionsGroup.entrySet().stream()
            .map(entry -> new DriverEntityTask(entry.getKey(), entry.getValue(), commitId))
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
                        driverQueryResult.getKey().mainEntityClassField,
                        ConditionOperator.MULTIPLE_EQUALS,
                        driverQueryResult.getValue().stream()
                            .map(ref -> new LongValue(driverQueryResult.getKey().mainEntityClassField, ref.getId()))
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

        if (termination) {
            // 驱动没有任何命中,整个安全树都不会有结果.所以这里空返回.
            if (conditions.isEmtpy()) {
                return conditions;
            } else {
                return Conditions.buildEmtpyConditions();
            }

        } else {
            // 之前过滤掉了非 driver 的条件,这里需要加入.
            processConditions.collectCondition().stream().filter(c -> !c.getEntityClassRef().isPresent()).forEach(c -> {
                    conditions.addAnd(c);
                }
            );
        }

        return conditions;
    }

    /**
     * 将不同的 entityClass 的条件进行分组.
     * 不同的 entityClass 关联不同的 Field 将认为是不同的组.
     * 不能处理非 driver 的 entity 查询条件.
     */
    private Map<DriverEntityKey, Conditions> splitEntityClassCondition(Collection<Condition> conditionCollection)
        throws SQLException {


        Map<DriverEntityKey, Conditions> result = new HashMap(MapUtils.calculateInitSize(conditionCollection.size()));

        /**
         * 关系字段.最终驱动表的查询结果将使用此字段来进行in过滤.
         */
        IEntityField relationField = null;
        IEntityClass driverEntityClass;
        DriverEntityKey key;
        Conditions driverConditions;
        for (Condition c : conditionCollection) {
            if (c.getEntityClassRef().isPresent()) {
                driverEntityClass = EntityClassHelper.checkEntityClass(metaManager, c.getEntityClassRef().get());
            } else {
                throw new SQLException("An attempt was made to correlate the query, but the entityClass for the driver table was not set!");
            }

            Optional<OqsRelation> relationOp = driverEntityClass.oqsRelations().stream()
                .filter(r -> r.getId() == c.getRelationId()).findFirst();
            if (!relationOp.isPresent()) {
                throw new SQLException(String.format("Unable to load the specified relationship.[id=%d]", c.getRelationId()));
            }
            OqsRelation relation = relationOp.get();
            relationField = relation.getEntityField();
            if (relationField != null) {
                key = new DriverEntityKey(driverEntityClass, relationField);
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
        private IEntityField mainEntityClassField;

        public DriverEntityKey(IEntityClass entityClass, IEntityField mainEntityClassField) {
            this.entityClass = entityClass;
            this.mainEntityClassField = mainEntityClassField;
        }

        public IEntityClass getEntityClass() {
            return entityClass;
        }

        public IEntityField getMainEntityClassField() {
            return mainEntityClassField;
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
            if (mainEntityClassField.id() != that.mainEntityClassField.id()) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityClass.id(), mainEntityClassField.id());
        }
    }

    /**
     * 驱动 entity 的 条件id列表 构造任务.
     */
    private class DriverEntityTask implements Callable<Map.Entry<DriverEntityKey, Collection<EntityRef>>> {

        private DriverEntityKey key;
        private Conditions conditions;
        private long commitId;

        public DriverEntityTask(DriverEntityKey key, Conditions conditions, long commitId) {
            this.key = key;
            this.conditions = conditions;
            this.commitId = commitId;
        }

        @Override
        public Map.Entry<DriverEntityKey, Collection<EntityRef>> call() throws Exception {
            Page driverPage = Page.newSinglePage(maxJoinDriverLineNumber);
            driverPage.setVisibleTotalCount(maxJoinDriverLineNumber);
            Collection<EntityRef> refs = combinedStorage.select(
                commitId, conditions, key.getEntityClass(), Sort.buildOutOfSort(), driverPage);

            /**
             * 确保驱动表的查询数据总量不超过 maxJoinDriverLineNumber.
             * 由于上限定义最多会返回 maxJoinDriverLineNumber 数量,如果实际匹配数量超过阀值会造成结果不精确.
             * 这里如果出现不精确以错误响应.
             */
            if (driverPage.getTotalCount() > maxJoinDriverLineNumber) {
                throw new SQLException(String.format("Drives entity(%s) data exceeding %d.",
                    key.getEntityClass().code(), maxJoinDriverLineNumber));
            }
            return new AbstractMap.SimpleEntry<>(key, refs);
        }
    }

    /**
     * 组合搜索.
     */
    static final class CombinedStorage {

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

        public Collection<EntityRef> select(
            long commitId, Conditions conditions, IEntityClass entityClass, Sort sort, Page page) throws SQLException {


            Collection<EntityRef> masterRefs = Collections.emptyList();

            if (commitId > 0) {
                //trigger master search
                masterRefs = masterStorage.select(conditions, entityClass,
                    SelectConfig.Builder.aSelectConfig().withSort(sort).withCommitId(commitId).build());

                for (EntityRef ref : masterRefs) {
                    if (ref.getOp() == OperationType.UNKNOWN.getValue()) {
                        throw new SQLException(String.format("Expected operation type unknown.[id=%d]", ref.getId()));
                    }
                }
            }

            masterRefs = fixNullSortValue(masterRefs, sort);

            /**
             * filter ids
             */
            Set<Long> filterIdsFromMaster = masterRefs.stream()
                .filter(x -> x.getOp() == OperationType.DELETE.getValue() || x.getOp() == OperationType.UPDATE.getValue())
                .map(EntityRef::getId)
                .collect(toSet());


            /**
             * 这里在查询索引时新创建一个page的原因是在查询索引时会调用page.getNextPage()造成当前页增加.相当于如下.
             * Page page = new Page(1,20);
             * page.getNextPage();
             * page.getNextPage();
             *
             * 第二次的调用会造成错误的读取后一页.
             * 为了让getNextPage()方法一个Page实例只能调用一次.
             */
            Page indexPage;
            try {
                indexPage = page.clone();
            } catch (CloneNotSupportedException e) {
                throw new SQLException(e.getMessage(), e);
            }

            Collection<EntityRef> indexRefs = indexStorage.select(
                conditions, entityClass,
                SelectConfig.Builder.aSelectConfig()
                    .withSort(sort)
                    .withPage(indexPage)
                    .withExcludedIds(filterIdsFromMaster)
                    .withCommitId(commitId).build());
            indexRefs = fixNullSortValue(indexRefs, sort);

            Collection<EntityRef> masterRefsWithoutDeleted = masterRefs.stream().
                filter(x -> x.getOp() != OperationType.DELETE.getValue()).collect(toList());

            Collection<EntityRef> retRefs;
            //combine two refs
            if (sort != null && !sort.isOutOfOrder()) {
                retRefs = merge(masterRefsWithoutDeleted, indexRefs, sort);
            } else {
                retRefs = new ArrayList<>(masterRefsWithoutDeleted.size() + indexRefs.size());
                retRefs.addAll(masterRefsWithoutDeleted);
                retRefs.addAll(indexRefs);
            }

            page.setTotalCount(indexPage.getTotalCount() + masterRefsWithoutDeleted.size());
            if (page.isEmptyPage()) {
                return Collections.emptyList();
            }

            if (!page.hasNextPage()) {
                return Collections.emptyList();
            }

            PageScope scope = page.getNextPage();
            long pageSize = page.getPageSize();

            long skips = scope == null ? 0 : scope.getStartLine();
            Collection<EntityRef> limitedSelect = retRefs.stream().skip(skips < 0 ? 0 : skips).limit(pageSize).collect(toList());
            return limitedSelect;
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
            List<EntityRef> sortedMasterRefs =
                masterRefs.stream()
                    .sorted(sort.isAsc() ? entityRefComparator : entityRefComparator.reversed())
                    .collect(toList());
            return streamMerger.merge(
                sortedMasterRefs.stream(),
                indexRefs.stream(),
                refMapping.get(type),
                sort.isAsc()).collect(toList());

        }

        // 如果排序,但是查询结果没有值.
        private Collection<EntityRef> fixNullSortValue(Collection<EntityRef> refs, Sort sort) {
            if (sort != null && !sort.isOutOfOrder()) {
                refs.parallelStream().forEach(r -> {
                    if (r.getOrderValue() == null || r.getOrderValue().isEmpty()) {
                        if (sort.getField().config().isIdentifie()) {
                            r.setOrderValue(Long.toString(r.getId()));
                        } else {
                            r.setOrderValue(sortDefaultValue.get(sort.getField().type()));
                            // 如果是意外的字段,那么设置为一个字符串0,数字和字符串都可以正常转型.
                            if (r.getOrderValue() == null) {
                                r.setOrderValue("0");
                            }
                        }
                    }
                });
            }

            return refs;
        }
    }

    /**
     * 判断是否为单条件标识查询.
     */
    private boolean isOneIdQuery(Conditions conditions) {
        // 只有一个条件.
        final int onlyOne = 1;

        boolean result = false;
        if (conditions.size() == onlyOne) {
            for (Condition condition : conditions.collectCondition()) {
                result = condition.getField().config().isIdentifie();
                break;
            }
        }

        return result;
    }

}
