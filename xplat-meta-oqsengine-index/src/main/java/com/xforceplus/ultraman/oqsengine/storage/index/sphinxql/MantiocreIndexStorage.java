package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.ManticoreStorageEntitiesSaveExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.OriginEntitiesDeleteExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.QueryConditionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.ManticoreStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import io.micrometer.core.instrument.Metrics;
import io.vavr.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

/**
 * 基于manticore的索引storage实现.
 *
 * @author dongbin
 * @version 0.1 2021/3/2 14:00
 * @since 1.8
 */
public class MantiocreIndexStorage implements IndexStorage {

    final Logger logger = LoggerFactory.getLogger(MantiocreIndexStorage.class);

    final ObjectMapper jsonMapper = new ObjectMapper();

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> writerDataSourceSelector;

    @Resource(name = "indexWriteIndexNameSelector")
    private Selector<String> indexWriteIndexNameSelector;

    @Resource(name = "sphinxQLSearchTransactionExecutor")
    private TransactionExecutor searchTransactionExecutor;

    @Resource(name = "sphinxQLWriteTransactionExecutor")
    private TransactionExecutor writeTransactionExecutor;

    @Resource(name = "indexConditionsBuilderFactory")
    private SphinxQLConditionsBuilderFactory sphinxQLConditionsBuilderFactory;

    @Resource(name = "indexStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "callWriteThreadPool")
    private ExecutorService threadPool;

    private String searchIndexName;

    private long maxSearchTimeoutMs = 0;

    public String getSearchIndexName() {
        return searchIndexName;
    }

    public void setSearchIndexName(String searchIndexName) {
        this.searchIndexName = searchIndexName;
    }

    public long getMaxSearchTimeoutMs() {
        return maxSearchTimeoutMs;
    }

    public void setMaxSearchTimeoutMs(long maxSearchTimeoutMs) {
        this.maxSearchTimeoutMs = maxSearchTimeoutMs;
    }

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config) throws SQLException {
        long startMs = System.currentTimeMillis();

        try {
            return (Collection<EntityRef>) searchTransactionExecutor.execute((resource, hint) -> {
                Set<Long> useFilterIds = null;

                if (resource.getTransaction().isPresent()) {
                    Transaction tx = (Transaction) resource.getTransaction().get();
                    Set<Long> updateIds = tx.getAccumulator().getUpdateIds();
                    if (updateIds.size() > 0) {
                        useFilterIds = new HashSet();
                        useFilterIds.addAll(updateIds);
                        useFilterIds.addAll(config.getExcludedIds());
                    }
                } else {
                    useFilterIds = config.getExcludedIds();
                }

                return QueryConditionExecutor.build(
                    getSearchIndexName(),
                    resource,
                    sphinxQLConditionsBuilderFactory,
                    storageStrategyFactory,
                    getMaxSearchTimeoutMs()).execute(
                    Tuple.of(entityClass, conditions, config.getPage(), config.getSort(), useFilterIds, config.getCommitId()));
            });
        } finally {
            Metrics.timer(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, "initiator", "index", "action", "condition")
                .record(System.currentTimeMillis() - startMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean clean(IEntityClass entityClass, long maintainId, long start, long end) throws SQLException {
        return false;
    }

    @Override
    public void saveOrDeleteOriginalEntities(Collection<OriginalEntity> originalEntities) throws SQLException {
        if (originalEntities.isEmpty()) {
            return;
        }
        Collection<OriginalEntitySection> sections = split(originalEntities);

        final long retryDurationMs = 3000;
        /**
         * 交由线程池并行执行.
         */
        CountDownLatch latch = new CountDownLatch(sections.size());
        for (OriginalEntitySection section : sections) {
            threadPool.submit(new HandlerTask(section, latch, retryDurationMs));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    /**
     * 实际的执行任务.
     */
    private class HandlerTask implements Runnable {

        private OriginalEntitySection section;
        private CountDownLatch latch;
        private long retryDurationMs;

        public HandlerTask(OriginalEntitySection section, CountDownLatch latch, long retryDurationMs) {
            this.section = section;
            this.latch = latch;
            this.retryDurationMs = retryDurationMs;
        }

        /**
         * 无限次数的重试.
         */
        @Override
        public void run() {
            boolean exit = false;
            try {
                while (!exit) {
                    try {
                        doSave();
                        exit = true;
                    } catch (SQLException ex) {
                        logger.error("Batch write error ({}), wait {} milliseconds to try again.", ex.getMessage(), retryDurationMs);
                        exit = false;

                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(retryDurationMs));

                    }
                }
            } finally {
                latch.countDown();
            }
        }

        private int doSave() throws SQLException {
            Map<OperationType, Collection<OriginalEntity>> sectionMap = section.getEntities();
            int totalSize = 0;
            for (OperationType op : sectionMap.keySet()) {
                switch (op) {
                    case CREATE:
                    case UPDATE: {
                        totalSize += doSaveOpSection(op, section.getIndexName(), section.getShardKey(), sectionMap.get(op));
                        break;
                    }
                    case DELETE: {
                        totalSize += doDeleteOpSection(section.getIndexName(), section.getShardKey(), sectionMap.get(op));
                        break;
                    }
                    default: {
                        logger.warn("Incorrect operation type.");
                    }
                }
            }

            return totalSize;
        }

        // 保存更新和创建的分区.
        private int doSaveOpSection(OperationType op, String indexName, String shardKey, Collection<OriginalEntity> originalEntities)
            throws SQLException {

            Collection<ManticoreStorageEntity> manticoreStorageEntities =
                originalEntities.parallelStream().map(oe -> toStorageEntityFromOriginal(oe)).collect(Collectors.toList());

            return (int) writeTransactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    if (OperationType.CREATE == op) {
                        return ManticoreStorageEntitiesSaveExecutor.buildCreate(indexName, resource)
                            .execute(manticoreStorageEntities);
                    } else if (OperationType.UPDATE == op) {
                        return ManticoreStorageEntitiesSaveExecutor.buildReplace(indexName, resource)
                            .execute(manticoreStorageEntities);
                    } else {
                        throw new SQLException("An operation that cannot be handled.");
                    }
                }

                @Override
                public String key() {
                    return shardKey;
                }
            });
        }

        // 操作删除的分区.
        private int doDeleteOpSection(String indexName, String shardKey, Collection<OriginalEntity> originalEntities)
            throws SQLException {
            return (int) writeTransactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    return OriginEntitiesDeleteExecutor.builder(indexName, resource).execute(originalEntities);
                }

                @Override
                public String key() {
                    return shardKey;
                }
            });
        }
    }

    private ManticoreStorageEntity toStorageEntityFromOriginal(OriginalEntity originalEntity) {
        ManticoreStorageEntity.Builder builder = ManticoreStorageEntity.Builder.aManticoreStorageEntity()
            .withId(originalEntity.getId())
            .withCommitId(originalEntity.getCommitid())
            .withTx(originalEntity.getTx())
            .withCreateTime(originalEntity.getCreateTime())
            .withUpdateTime(originalEntity.getUpdateTime())
            .withMaintainId(0)
            .withOqsmajor(originalEntity.getOqsMajor())
            .withAttributeF(toAttributesF(originalEntity))
            .withEntityClassF(toEntityClassF(originalEntity))
            .withAttribute(toAttribute(originalEntity));
        return builder.build();
    }

    private String toAttribute(OriginalEntity originalEntity) {
        Map<String, Object> attributeMap =
            originalEntity.listAttributes().stream().collect(Collectors.toMap(a -> {
                String storageName = a.getKey();
                return AnyStorageValue.getInstance(storageName).shortStorageName();
            }, a -> a.getValue()));

        try {
            return jsonMapper.writeValueAsString(attributeMap);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    // 全文属性
    private String toAttributesF(OriginalEntity originalEntity) {
        return originalEntity.listAttributes().stream().map(a -> {
            String storageName = a.getKey();
            Object value = a.getValue();

            StorageValue anyStorageValue = AnyStorageValue.getInstance(storageName);
            storageName = anyStorageValue.shortStorageName();

            return wrapperAttribute(storageName, value, anyStorageValue.type());
        }).collect(Collectors.joining(" "));
    }

    // 全文元信息
    private String toEntityClassF(OriginalEntity originalEntity) {
        return originalEntity.getEntityClass().family()
            .stream().map(e -> Long.toString(e.id())).collect(Collectors.joining(" "));
    }

    /**
     * StorageType.STRING
     * aZl8N0{空格}test{空格}y58M7S
     * StorageType.Long
     * aZl8N0123y58M7S
     */
    private String wrapperAttribute(String shortStorageName, Object value, StorageType storageType) {
        Map.Entry<String, String> shortWrapper = SphinxQLHelper.parseShortStorageName(shortStorageName);
        StringBuilder buff = new StringBuilder();
        buff.append(shortWrapper.getKey());
        if (StorageType.STRING == storageType) {
            buff.append(" ").append(value.toString()).append(" ");
        } else {
            buff.append(value.toString());
        }
        buff.append(shortWrapper.getValue());

        return buff.toString();
    }

    // 根据最终数据的储存目标切割.
    private Collection<OriginalEntitySection> split(Collection<OriginalEntity> originalEntities) {
        DataSource dataSource;
        String indexName;
        String shardKey;
        OriginalEntitySection originalEntitySection;

        int dsSize = writerDataSourceSelector.selects().size();
        int tSize = indexWriteIndexNameSelector.selects().size();
        // 防止 rehash
        Map<DataSource, Map<String, OriginalEntitySection>> dsSectionMap =
            new HashMap(dsSize + (int) (dsSize * (1D - 0.75D)));

        Map<String, OriginalEntitySection> nameSectionMap;

        for (OriginalEntity oe : originalEntities) {
            shardKey = Long.toString(oe.getId());
            dataSource = writerDataSourceSelector.select(shardKey);
            indexName = indexWriteIndexNameSelector.select(shardKey);

            nameSectionMap = dsSectionMap.get(dataSource);
            if (nameSectionMap == null) {
                // 防止 rehash
                nameSectionMap = new HashMap(tSize + (int) (tSize - tSize * 0.75D));
                dsSectionMap.put(dataSource, nameSectionMap);
            }

            originalEntitySection = nameSectionMap.get(indexName);
            if (originalEntitySection == null) {
                originalEntitySection = new OriginalEntitySection(shardKey, indexName, oe);
                nameSectionMap.put(indexName, originalEntitySection);
            } else {
                originalEntitySection.add(oe);
            }
        }

        List<OriginalEntitySection> oesList = new LinkedList();
        for (Map<String, OriginalEntitySection> nameSection : dsSectionMap.values()) {
            for (OriginalEntitySection oes : nameSection.values()) {
                oesList.add(oes);
            }
        }

        return oesList;
    }

    /**
     * 每一个原始实体最终需要存放的数据源部份.
     */
    private static class OriginalEntitySection {
        private String shardKey;
        private String indexName;
        private Map<OperationType, Collection<OriginalEntity>> entities;

        public OriginalEntitySection(String shardKey, String indexName, OriginalEntity entity) {
            this.shardKey = shardKey;
            this.indexName = indexName;
            this.add(entity);
        }

        public OriginalEntitySection(String shardKey, String indexName, Collection<OriginalEntity> entities) {
            this.shardKey = shardKey;
            this.indexName = indexName;
            for (OriginalEntity oe : entities) {
                this.add(oe);
            }
        }

        public String getIndexName() {
            return indexName;
        }

        public Map<OperationType, Collection<OriginalEntity>> getEntities() {
            return entities;
        }

        public void add(OriginalEntity originalEntity) {
            if (entities == null) {
                entities = new HashMap();
            }
            OperationType op = getOperationType(originalEntity);
            Collection<OriginalEntity> originalEntities = entities.get(op);
            if (originalEntities == null) {
                originalEntities = new LinkedList<>();
                entities.put(op, originalEntities);
            }
            originalEntities.add(originalEntity);
        }

        public String getShardKey() {
            return shardKey;
        }

        private OperationType getOperationType(OriginalEntity oe) {
            OperationType op = OperationType.getInstance(oe.getOp());
            if (OperationType.UNKNOWN == op) {
                throw new IllegalArgumentException(
                    String.format("Unrecognized operation type.[id=%d, type=%d]", oe.getId(), oe.getOp()));
            } else {
                return op;
            }
        }
    }

}
