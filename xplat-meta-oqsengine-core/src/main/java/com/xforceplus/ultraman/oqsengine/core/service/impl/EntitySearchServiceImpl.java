package com.xforceplus.ultraman.oqsengine.core.service.impl;

import static java.util.stream.Collectors.toList;

import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSearchConfig;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.ServiceSelectConfig;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AbstractConditionNode;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.ConditionOperator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import com.xforceplus.ultraman.oqsengine.storage.ConditionsSelectStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.vavr.Tuple;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * 最大允许的 join 数量.
     */
    static final int DEFAULT_MAX_JOIN_ENTITY_NUMBER = 2;

    /**
     * 驱动关联表的匹配数据上限.
     */
    static final int DEFAULT_MAX_JOIN_DRIVER_LINE_NUMBER = 1000;

    /**
     * 查询时最大可见数据量.
     */
    static final int DEFAULT_MAX_VISIBLE_TOTAL_COUNT = 10000;

    private Counter oneReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "one");
    private Counter multipleReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "multiple");
    private Counter searchReadCountTotal = Metrics.counter(MetricsDefine.READ_COUNT_TOTAL, "action", "search");
    private Counter failCountTotal = Metrics.counter(MetricsDefine.FAIL_COUNT_TOTAL);

    @Resource
    private MasterStorage masterStorage;

    @Resource
    private IndexStorage indexStorage;

    @Resource(name = "combinedSelectStorage")
    private ConditionsSelectStorage combinedSelectStorage;

    @Resource(name = "taskThreadPool")
    private ExecutorService threadPool;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private MetaManager metaManager;

    @Resource
    private TransactionManager transactionManager;

    private long maxVisibleTotalCount;
    private int maxJoinEntityNumber;
    private long maxJoinDriverLineNumber;
    private boolean debugInfo = false;


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

        if (maxJoinDriverLineNumber > maxVisibleTotalCount) {
            maxJoinDriverLineNumber = maxVisibleTotalCount;
        }

        logger.info("Search service startup:[maxVisibleTotal:{}, maxJoinEntityNumber:{}, maxJoinDriverLineNumber:{}]",
            maxVisibleTotalCount, maxJoinEntityNumber, maxJoinDriverLineNumber);


    }

    public int getMaxJoinEntityNumber() {
        return maxJoinEntityNumber;
    }

    public void setMaxJoinEntityNumber(int maxJoinEntityNumber) {
        this.maxJoinEntityNumber = maxJoinEntityNumber;
    }

    public long getMaxJoinDriverLineNumber() {
        return maxJoinDriverLineNumber;
    }

    public void setMaxJoinDriverLineNumber(long maxJoinDriverLineNumber) {
        this.maxJoinDriverLineNumber = maxJoinDriverLineNumber;
    }

    public long getMaxVisibleTotalCount() {
        return maxVisibleTotalCount;
    }

    public void setMaxVisibleTotalCount(long maxVisibleTotalCount) {
        this.maxVisibleTotalCount = maxVisibleTotalCount;
    }

    public boolean isDebugInfo() {
        return debugInfo;
    }

    public void setDebugInfo(boolean showDebugInfo) {
        this.debugInfo = showDebugInfo;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "one"})
    @Override
    public OqsResult<IEntity> selectOne(long id, EntityClassRef entityClassRef) throws SQLException {

        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef);
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {

            if (logger.isDebugEnabled()) {
                logger.debug("Unable to find meta information {}.", entityClassRef);
            }

            return OqsResult.notExistMeta(entityClassRef);

        } else {
            entityClass = entityClassOp.get();
        }

        try {

            Optional<IEntity> entityOptional = masterStorage.selectOne(id, entityClass);
            if (entityOptional.isPresent()) {

                if (logger.isDebugEnabled()) {
                    logger.debug("Query {} instance result of object ({}) : [{}].",
                        id, entityClassRef, entityOptional.get());
                }

            } else {

                if (logger.isDebugEnabled()) {
                    logger.debug("Query {} instance result of object ({}) : [].", id, entityClassRef);
                }

            }

            return OqsResult.success(entityOptional.orElse(null));

        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            oneReadCountTotal.increment();
        }

    }

    @Override
    public OqsResult<IEntity> selectOneByKey(List<BusinessKey> key, EntityClassRef entityClassRef)
        throws SQLException {
        //        Optional<IEntityClass> entityClass = metaManager.load(entityClassRef.getId());
        //        if (!entityClass.isPresent()) {
        //            throw new RuntimeException(
        //                String.format("Can not find any EntityClass with id %s", entityClassRef.getId()));
        //        }
        //        Optional<StorageUniqueEntity> uniqueStorage = masterStorage.select(key, entityClass.get());
        //        if (!uniqueStorage.isPresent()) {
        //            return Optional.empty();
        //        }
        //        return selectOne(uniqueStorage.get().getId(), entityClassRef);
        throw new UnsupportedOperationException();
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "multiple"})
    @Override
    public OqsResult<Collection<IEntity>> selectMultiple(long[] ids, EntityClassRef entityClassRef)
        throws SQLException {

        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef);
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {

            if (logger.isDebugEnabled()) {
                logger.debug("Unable to find meta information {}.", entityClassRef);
            }

            return OqsResult.notExistMeta(entityClassRef);

        } else {
            entityClass = entityClassOp.get();
        }

        try {
            Collection<IEntity> entities = masterStorage.selectMultiple(ids, entityClass);

            if (logger.isDebugEnabled()) {
                entities.stream().forEach(e -> {
                    logger.debug("Query {} instance result of object ({}) : [{}].", e.id(), entityClassRef, e);
                });
            }

            return OqsResult.success(entities);

        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            multipleReadCountTotal.increment();
        }
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "condition"})
    @Override
    public OqsResult<Collection<IEntity>> selectByConditions(Conditions conditions, EntityClassRef entityClassRef,
                                                             Page page)
        throws SQLException {
        return selectByConditions(conditions, entityClassRef,
            ServiceSelectConfig.Builder.anSearchConfig().withPage(page).build());
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "condition"})
    @Override
    public OqsResult<Collection<IEntity>> selectByConditions(
        Conditions conditions, EntityClassRef entityClassRef, Sort sort, Page page) throws SQLException {
        return selectByConditions(conditions, entityClassRef,
            ServiceSelectConfig.Builder.anSearchConfig().withSort(sort).withPage(page).build());
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "condition"})
    @Override
    public OqsResult<Collection<IEntity>> selectByConditions(Conditions conditions, EntityClassRef entityClassRef,
                                                             ServiceSelectConfig config)
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

        if (this.isDebugInfo()) {
            logger.info("Conditional query: {}.[entityClass = {}, config = {}]", conditions, entityClassRef, config);
        }

        /*
         * 数据过滤不会使有和含有模糊搜索的条件.
         */
        if (config.getFilter().isPresent()) {
            Conditions filterCondtiton = config.getFilter().get();
            if (filterCondtiton.haveFuzzyCondition()) {
                throw new SQLException("Data filtering conditions cannot use fuzzy operators.");
            }
        }

        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef);
        IEntityClass entityClass;
        if (!entityClassOp.isPresent()) {

            return OqsResult.notExistMeta(entityClassRef);

        } else {
            entityClass = entityClassOp.get();
        }

        // 检查是否有非可搜索的字段,如果有将空返回.
        boolean checkResult;
        for (Condition c : conditions.collectCondition()) {
            if (c.getEntityClassRef().isPresent()) {
                Optional<IEntityClass> ecOp = metaManager.load(c.getEntityClassRef().get());
                if (!ecOp.isPresent()) {

                    return OqsResult.notExistMeta(c.getEntityClassRef().get());
                }

                checkResult = checkCanSearch(c, ecOp.get());
            } else {
                checkResult = checkCanSearch(c, entityClass);
            }
            if (!checkResult) {
                if (config.getPage().isPresent()) {
                    Page page = config.getPage().get();
                    page.setTotalCount(0);
                }
                return OqsResult.success(Collections.emptyList());
            }
        }

        /*
        一个优化,将只有一个条件并且条件是id的查询退化成selectOne.
         */
        if (isOneIdQuery(conditions)) {
            Condition onlyCondition = conditions.collectCondition().stream().findFirst().get();
            long id = onlyCondition.getFirstValue().valueToLong();
            Optional<IEntity> entityOptional = masterStorage.selectOne(id, entityClass);
            if (entityOptional.isPresent()) {
                return OqsResult.success(Arrays.asList(entityOptional.get()));
            } else {
                return OqsResult.success(Collections.emptyList());
            }
        }


        Conditions useConditions = conditions;

        Page usePage;
        if (!config.getPage().isPresent()) {
            usePage = new Page();
        } else {
            usePage = config.getPage().get();
        }
        usePage.setVisibleTotalCount(maxVisibleTotalCount);

        try {
            // join
            Map<Long, IEntityClass> entityClassCollectionMapping = collectEntityClass(conditions, entityClass);
            collectEntityClass(conditions, entityClass);


            final long onlyOneEntityClass = 1;
            long externalSize = entityClassCollectionMapping.keySet().stream().filter(x -> x > 0).count();

            long size = externalSize + 1;

            if (size > onlyOneEntityClass) {

                if (size > maxJoinEntityNumber) {
                    throw new SQLException(
                        String.format("Join queries can be associated with at most %d entities.", maxJoinEntityNumber));
                }

                /*
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
                Collection<AbstractConditionNode> safeNodes = conditions.collectSubTree(c -> !c.isRed(), true);

                /*
                 * 所有的安全结点组成的 Conditions 集合.最终这些条件将会以 OR 连接起来做为最终查询.
                 * 这些条件中的关联 entity 已经被替换成了合式的条件.
                 */
                Collection<Conditions> subConditions = new ArrayList(safeNodes.size());

                for (AbstractConditionNode safeNode : safeNodes) {
                    subConditions.add(buildSafeNodeConditions(entityClass, safeNode));
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
                    return OqsResult.success(Collections.emptyList());
                }
            }

            SelectConfig.Builder selectConfigBuilder = SelectConfig.Builder.anSelectConfig();
            selectConfigBuilder.withPage(usePage)
                .withDataAccessFitlerCondtitons(
                    config.getFilter().isPresent() ? config.getFilter().get() : Conditions.buildEmtpyConditions()
                );
            // 非排序.
            if (!config.getSort().isPresent()
                && !config.getSecondarySort().isPresent()
                && !config.getThirdSort().isPresent()) {

                selectConfigBuilder.withSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD));

            } else {
                // 排序中是否有id排序,如果有就不需要追加id.
                boolean haveIdSort = false;
                // 上层不排序,后续的直接视为不排序.
                Optional<Sort> sortOp = config.getSort();
                if (sortOp.isPresent() && !sortOp.get().isOutOfOrder()) {
                    if (config.getSort().get().getField().config().isIdentifie()) {
                        haveIdSort = true;
                    }
                    selectConfigBuilder.withSort(config.getSort().get());

                    if (config.getSecondarySort().isPresent()) {

                        if (config.getSecondarySort().get().getField().config().isIdentifie()) {
                            haveIdSort = true;
                        }

                        selectConfigBuilder.withSecondarySort(config.getSecondarySort().get());

                        if (config.getThirdSort().isPresent()) {
                            selectConfigBuilder.withThirdSort(config.getThirdSort().get());
                        } else {
                            // 追加ID排序.防止相同值造成排序不准确.
                            if (!haveIdSort) {
                                selectConfigBuilder.withThirdSort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD));
                            }
                        }
                    } else {
                        // 追加ID排序.防止相同值造成排序不准确.
                        if (!haveIdSort) {
                            selectConfigBuilder.withSecondarySort(Sort.buildAscSort(EntityField.ID_ENTITY_FIELD));
                        }
                    }
                }
            }

            Collection<EntityRef> refs = combinedSelectStorage.select(
                useConditions,
                entityClass,
                selectConfigBuilder.build()
            );

            Collection<IEntity> entities = buildEntitiesFromRefs(refs, entityClass);

            if (logger.isDebugEnabled()) {
                if (entities.size() == 0) {

                    logger.debug("Select conditions ({}) result: []", conditions);

                } else {
                    StringBuilder buff = new StringBuilder();
                    for (IEntity e : entities) {
                        if (e != null) {
                            buff.append(e.toString()).append('\n');
                        }
                    }
                    logger.debug(
                        "Select conditions ({}) result: [{}],totalCount:[{}]",
                        conditions, buff.toString(), usePage.getTotalCount());
                    // help gc
                    buff = null;
                }
            }

            return OqsResult.success(entities);
        } catch (Exception ex) {
            failCountTotal.increment();
            throw ex;
        } finally {
            searchReadCountTotal.increment();
        }
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        percentiles = {0.5, 0.9, 0.99},
        extraTags = {"initiator", "all", "action", "count"})
    @Override
    public OqsResult<Long> countByConditions(Conditions conditions, EntityClassRef entityClassRef,
                                             ServiceSelectConfig config) throws SQLException {

        Page page = Page.emptyPage();
        ServiceSelectConfig.Builder builder = ServiceSelectConfig.Builder.anSearchConfig()
            .withPage(page)
            .withSort(Sort.buildOutOfSort());
        if (config.getFilter().isPresent()) {
            builder.withFilter(config.getFilter().get());
        }

        ServiceSelectConfig countConfig = builder.build();

        OqsResult<Collection<IEntity>> result = this.selectByConditions(conditions, entityClassRef, countConfig);
        if (result.isSuccess()) {

            if (logger.isDebugEnabled()) {
                logger.debug("Using object (%s) The result of using (%s) conditional count is %d.",
                    entityClassRef, conditions, page.getTotalCount());
            }

            return OqsResult.success(page.getTotalCount());
        } else {
            return result.copy(0);
        }
    }

    @Override
    public OqsResult<Collection<IEntity>> search(ServiceSearchConfig config) throws SQLException {
        SearchConfig searchConfig = SearchConfig.Builder.anSearchConfig()
            .withCode(config.getCode())
            .withValue(config.getValue())
            .withPage(config.getPage())
            .withFuzzyType(config.getFuzzyType())
            .build();
        EntityClassRef[] entityClassRefs = config.getEntityClassRefs();
        IEntityClass[] entityClasses = new IEntityClass[entityClassRefs.length];
        for (int i = 0; i < entityClasses.length; i++) {
            Optional<IEntityClass> ecOp = metaManager.load(entityClassRefs[i]);
            if (!ecOp.isPresent()) {
                return OqsResult.notExistMeta(entityClassRefs[i]);
            } else {
                entityClasses[i] = ecOp.get();
            }
        }

        return OqsResult.success(
            buildEntitiesFromRefs(indexStorage.search(searchConfig, entityClasses), null)
        );
    }

    /**
     * 以下情况会空返回. 1. 字段不存在. 2. 字段非可搜索. 注意: 如果字段标示为identifie类型,那么会返回true.
     */
    private boolean checkCanSearch(Condition c, IEntityClass entityClass) {
        if (c.getField().config().isIdentifie()) {
            return true;
        }

        Optional<IEntityField> fieldOp = entityClass.field(c.getField().id());
        if (fieldOp.isPresent() && fieldOp.get().config().isSearchable()) {
            return true;
        }

        return false;
    }

    /**
     * 收集条件中的 entityClass.
     */
    private Map<Long, IEntityClass> collectEntityClass(Conditions conditions, IEntityClass mainEntityClass) {
        Map<Long, IEntityClass> entityClasses = conditions.collectCondition().stream().map(c -> {
            if (c.getEntityClassRef().isPresent() && c.getRelationId() > 0) {
                if (c.getEntityClassRef().get().getId() == mainEntityClass.id()) {
                    //self reference
                    return Tuple.of(c.getRelationId(), mainEntityClass);
                } else {
                    Optional<IEntityClass> ecOp = metaManager.load(c.getEntityClassRef().get());
                    if (ecOp.isPresent()) {
                        return Tuple.of(c.getRelationId(), ecOp.get());
                    } else {
                        EntityClassRef ref = c.getEntityClassRef().get();
                        throw new IllegalStateException(
                            String.format("Non-existent meta information (%s-%s).", ref.getCode(), ref.getProfile()));
                    }
                }
            } else {
                return Tuple.of(0L, mainEntityClass);
            }
        }).collect(Collectors.toMap(x -> x._1(), x -> x._2(), (a, b) -> a));

        // 防止条件中没有出现非驱动 entity 的字段条件.
        //entityClasses.add(mainEntityClass);

        return entityClasses;
    }

    // 加载实体. entityClass 允许为null.
    private Collection<IEntity> buildEntitiesFromRefs(Collection<EntityRef> refs, IEntityClass entityClass)
        throws SQLException {

        if (refs.isEmpty()) {
            return Collections.emptyList();
        }

        long[] ids = refs.stream().mapToLong(ref -> ref.getId()).toArray();
        // 结果查询表.
        Map<Long, IEntity> entityTable;
        if (entityClass == null) {
            Collection<IEntity> entities = masterStorage.selectMultiple(ids);
            entityTable = entities.stream().collect(Collectors.toMap(ref -> ref.id(), ref -> ref, (r0, r1) -> r0));
        } else {
            Collection<IEntity> entities = masterStorage.selectMultiple(ids, entityClass);
            entityTable = entities.stream().collect(Collectors.toMap(ref -> ref.id(), ref -> ref, (r0, r1) -> r0));
        }

        List<IEntity> results = new ArrayList<>(ids.length);
        Arrays.stream(ids).forEach(id -> {
            IEntity entity = entityTable.get(id);
            if (entity != null) {
                results.add(entity);
            }
        });

        return results;
    }

    /**
     * 将安全条件结点处理成可查询的 Conditions 实例. ignoreEntityClass 表示不需要处理的条件.
     */
    private Conditions buildSafeNodeConditions(IEntityClass mainEntityClass, AbstractConditionNode safeNode)
        throws SQLException {

        Conditions processConditions = new Conditions(safeNode);

        Collection<Condition> safeCondititons = processConditions.collectCondition();
        // 只包含驱动 entity 条件的集合.
        /*
         * condition中的entityClassRef存在只是表示字段的本身来源 e.g.  A(f1,f2) <-- A'(f3)
         * 那么 f1的condition上就会存在EntityClassRef[A] f3的condition暂时没有
         * 所以判断是否是另一个驱动表查询的条件需要同时考虑到是否有relationId存在
         * relationId表示EntityClassRef和当前查询EntityClassRef的路径(关系) 即如何从 A -findEntityBy(relationId)-> B
         * 所以存在relationId时 即告知当前condition来源于另一个对象，并且当前对象A通过 relationId所指 relation可以找到该对象。
         */
        Collection<Condition> driverConditionCollection = safeCondititons.stream()
            .filter(c -> c.getEntityClassRef().isPresent() && c.getRelationId() > 0)
            .collect(toList());

        // 按照驱动 entity 的 entityClass 和关联字段来分组条件.
        Map<DriverEntityKey, Conditions> driverEntityConditionsGroup =
            splitEntityClassCondition(mainEntityClass, driverConditionCollection);

        // driver 数据收集 future.
        List<Future<Map.Entry<DriverEntityKey, Collection<EntityRef>>>> futures =
            new ArrayList<>(driverEntityConditionsGroup.size());
        /*
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
                /*
                 * 由于所有安全结点都以 and 连接,所以当其中任意一个 driver 条件出现0匹配时安全结点都应该被修剪.
                 */
                if (driverQueryResult.getValue().isEmpty()) {
                    termination = true;
                    break;
                }

                //related field should not be identifier
                driverQueryResult.getKey().mainEntityClassField.config().identifie(false);

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
            processConditions.collectCondition().stream()
                .filter(c -> !c.getEntityClassRef().isPresent() || c.getRelationId() == 0)
                .forEach(c -> conditions.addAnd(c));
        }

        return conditions;
    }

    /**
     * 将不同的 entityClass 的条件进行分组. 不同的 entityClass 关联不同的 Field 将认为是不同的组. 不能处理非 driver 的 entity 查询条件.
     */
    private Map<DriverEntityKey, Conditions> splitEntityClassCondition(IEntityClass mainEntityClass,
                                                                       Collection<Condition> conditionCollection)
        throws SQLException {


        Map<DriverEntityKey, Conditions> result = new HashMap(MapUtils.calculateInitSize(conditionCollection.size()));

        /*
         * 关系字段.最终驱动表的查询结果将使用此字段来进行in过滤.
         */
        IEntityField relationField = null;
        IEntityClass driverEntityClass;
        DriverEntityKey key;
        Conditions driverConditions;
        for (Condition c : conditionCollection) {
            if (c.getEntityClassRef().isPresent() && c.getRelationId() > 0) {
                Optional<IEntityClass> ecOp = metaManager.load(c.getEntityClassRef().get());
                if (ecOp.isPresent()) {
                    driverEntityClass = ecOp.get();
                } else {
                    EntityClassRef ref = c.getEntityClassRef().get();
                    throw new SQLException(
                        String.format("Non-existent meta information (%s-%s).", ref.getCode(), ref.getProfile()));
                }
            } else {
                throw new SQLException(
                    "An attempt was made to correlate the query, but the entityClass for the driver table was not set!");
            }

            Optional<Relationship> relationOp = mainEntityClass.relationship().stream()
                .filter(r -> r.getId() == c.getRelationId()).findFirst();
            if (!relationOp.isPresent()) {
                throw new SQLException(
                    String.format("Unable to load the specified relationship.[id=%d]", c.getRelationId()));
            }
            Relationship relation = relationOp.get();
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
     * 驱动 entity 的条件分组 key. 以 entityClass 和其关联的主 entityClass 中字段为区分.
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

        public DriverEntityTask(DriverEntityKey key, Conditions conditions) {
            this.key = key;
            this.conditions = conditions;
        }

        @Override
        public Map.Entry<DriverEntityKey, Collection<EntityRef>> call() throws Exception {
            Page driverPage = Page.newSinglePage(maxJoinDriverLineNumber);
            driverPage.setVisibleTotalCount(maxJoinDriverLineNumber);
            Collection<EntityRef> refs = combinedSelectStorage.select(
                conditions,
                key.getEntityClass(),
                SelectConfig.Builder.anSelectConfig()
                    .withSort(Sort.buildOutOfSort())
                    .withPage(driverPage).build()
            );

            /*
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
     * 判断是否为单条件标识查询.
     */
    private boolean isOneIdQuery(Conditions conditions) {
        // 只有一个条件.
        final int onlyOne = 1;

        /*
         * related
         */
        final int notRelated = 0;

        boolean result = false;
        if (conditions.size() == onlyOne) {
            for (Condition condition : conditions.collectCondition()) {
                result = condition.getField().config().isIdentifie();
                result = result && condition.getRelationId() <= notRelated
                    && (condition.getOperator() == ConditionOperator.EQUALS);
                break;
            }
        }

        return result;
    }
}
