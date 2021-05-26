package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.xforceplus.ultraman.oqsengine.common.map.MapUtils;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AnyEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.executor.ResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.CleanExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.OriginEntitiesDeleteIndexExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.QueryConditionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.SaveIndexExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor.SearchExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo.SphinxQLStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.conditions.SphinxQLConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.AnyStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.ShortStorageName;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.tokenizer.Tokenizer;
import com.xforceplus.ultraman.oqsengine.tokenizer.TokenizerFactory;
import io.micrometer.core.annotation.Timed;
import io.vavr.Tuple;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于manticore的索引storage实现.
 *
 * @author dongbin
 * @version 0.1 2021/3/2 14:00
 * @since 1.8
 */
public class SphinxQLManticoreIndexStorage implements IndexStorage {

    final Logger logger = LoggerFactory.getLogger(SphinxQLManticoreIndexStorage.class);

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
        .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
        .enable(JsonReadFeature.ALLOW_MISSING_VALUES)
        .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
        .enable(JsonReadFeature.ALLOW_LEADING_ZEROS_FOR_NUMBERS)
        .enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS)
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)
        .enable(JsonReadFeature.ALLOW_YAML_COMMENTS).build();

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

    @Resource(name = "tokenizerFactory")
    private TokenizerFactory tokenizerFactory;

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

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "index", "action",
        "condition"})
    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
        throws SQLException {
        return (Collection<EntityRef>) searchTransactionExecutor.execute((tx, resource, hint) -> {
            Set<Long> useFilterIds = null;

            if (resource.getTransaction().isPresent()) {
                Set<Long> updateIds = tx.getAccumulator().getUpdateIds();
                useFilterIds = new HashSet();
                useFilterIds.addAll(updateIds);
                useFilterIds.addAll(config.getExcludedIds());
            } else {
                useFilterIds = config.getExcludedIds();
            }

            SelectConfig useConfig = SelectConfig.Builder.anSelectConfig()
                .withCommitId(config.getCommitId())
                .withSort(config.getSort())
                .withPage(config.getPage())
                .withExcludedIds(useFilterIds)
                .withDataAccessFitlerCondtitons(config.getDataAccessFilterCondtitions())
                .withFacet(config.getFacet()).build();

            return QueryConditionExecutor.build(
                getSearchIndexName(),
                resource,
                sphinxQLConditionsBuilderFactory,
                storageStrategyFactory,
                getMaxSearchTimeoutMs()).execute(
                Tuple.of(entityClass, conditions, useConfig));
        });
    }

    @Override
    public Collection<EntityRef> search(SearchConfig config, IEntityClass ...entityClasses)
        throws SQLException {
        return (Collection<EntityRef>) searchTransactionExecutor.execute((tx, resource, hint) -> {
            return SearchExecutor.build(getSearchIndexName(), resource, sphinxQLConditionsBuilderFactory, getMaxSearchTimeoutMs())
                .execute(Tuple.of(config, entityClasses));
        });
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "index", "action", "clean"})
    @Override
    public long clean(IEntityClass entityClass, long maintainId, long start, long end) throws SQLException {
        CleanExecutor executor = CleanExecutor.Builder.anCleanExecutor()
            .withEntityClass(entityClass)
            .withStart(start)
            .withEnd(end)
            .withIndexNames(indexWriteIndexNameSelector.selects())
            .withDs(writerDataSourceSelector.selects())
            .build();

        return executor.execute(maintainId);
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "index", "action", "save"})
    @Override
    public void saveOrDeleteOriginalEntities(Collection<OriginalEntity> originalEntities) throws SQLException {
        if (originalEntities.isEmpty()) {
            return;
        }

        verifyOriginalEntites(originalEntities);

        Collection<OriginalEntitySection> sections = split(originalEntities);

        /*
         * 失败重试间隔毫秒.
         */
        final long retryDurationMs = 3000;

        List<Future<Boolean>> futures = new ArrayList<>(sections.size());
        for (OriginalEntitySection section : sections) {
            futures.add(threadPool.submit(new HandlerTask(section, retryDurationMs, true)));
        }

        for (Future<Boolean> f : futures) {
            try {
                if (!f.get()) {
                    throw new SQLException("Failed to save for unknown reason.");
                }
            } catch (InterruptedException e) {
                throw new SQLException(e.getCause());
            } catch (ExecutionException e) {
                throw new SQLException(e.getCause());
            }
        }
    }

    private void verifyOriginalEntites(Collection<OriginalEntity> originalEntities) throws SQLException {
        for (OriginalEntity oe : originalEntities) {
            if (oe.getEntityClass() == null || oe.getEntityClass() == AnyEntityClass.getInstance()) {
                throw new SQLException("Invalid entityclass!");
            }
        }
    }

    /**
     * 实际的执行任务.
     */
    private class HandlerTask implements Callable<Boolean> {

        private OriginalEntitySection section;
        private long retryDurationMs;
        private boolean stopWhenFail;

        public HandlerTask(OriginalEntitySection section, long retryDurationMs, boolean stopWhenFail) {
            this.section = section;
            this.retryDurationMs = retryDurationMs;
            this.stopWhenFail = stopWhenFail;
        }

        @Override
        public Boolean call() throws Exception {
            boolean exit = false;
            boolean error = false;
            Exception exception = null;
            while (!exit) {
                try {
                    doSave();
                    exit = true;
                    error = false;
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    logger.error("Batch write error, wait {} milliseconds to try again.", retryDurationMs);
                    if (stopWhenFail) {
                        exit = true;
                        exception = ex;
                    } else {
                        exit = false;
                    }
                    error = true;

                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(retryDurationMs));
                }
            }


            if (exception != null) {
                throw exception;
            } else {
                return !error ? true : false;
            }
        }

        private int doSave() throws SQLException {
            Map<OperationType, Collection<OriginalEntity>> sectionMap = section.getEntities();
            int totalSize = 0;
            for (OperationType op : sectionMap.keySet()) {
                switch (op) {
                    case CREATE:
                    case UPDATE: {
                        totalSize +=
                            doSaveOpSection(op, section.getIndexName(), section.getShardKey(), sectionMap.get(op));
                        break;
                    }
                    case DELETE: {
                        totalSize +=
                            doDeleteOpSection(section.getIndexName(), section.getShardKey(), sectionMap.get(op));
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
        private int doSaveOpSection(OperationType op, String indexName, String shardKey,
                                    Collection<OriginalEntity> originalEntities)
            throws SQLException {

            Collection<SphinxQLStorageEntity> manticoreStorageEntities = new ArrayList<>(originalEntities.size());
            for (OriginalEntity oe : originalEntities) {
                manticoreStorageEntities.add(toStorageEntityFromOriginal(oe));
            }

            return (int) writeTransactionExecutor.execute(new ResourceTask() {
                @Override
                public Object run(Transaction tx, TransactionResource resource, ExecutorHint hint) throws SQLException {
                    if (OperationType.CREATE == op) {
                        return SaveIndexExecutor.buildCreate(indexName, resource)
                            .execute(manticoreStorageEntities);
                    } else if (OperationType.UPDATE == op) {
                        return SaveIndexExecutor.buildReplace(indexName, resource)
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
                public Object run(Transaction tx, TransactionResource resource, ExecutorHint hint) throws SQLException {
                    return OriginEntitiesDeleteIndexExecutor.builder(indexName, resource).execute(originalEntities);
                }

                @Override
                public String key() {
                    return shardKey;
                }
            });
        }
    }

    private SphinxQLStorageEntity toStorageEntityFromOriginal(OriginalEntity originalEntity) throws SQLException {
        SphinxQLStorageEntity.Builder builder = SphinxQLStorageEntity.Builder.anManticoreStorageEntity()
            .withId(originalEntity.getId())
            .withCommitId(originalEntity.getCommitid())
            .withTx(originalEntity.getTx())
            .withCreateTime(originalEntity.getCreateTime())
            .withUpdateTime(originalEntity.getUpdateTime())
            .withMaintainId(originalEntity.getMaintainid())
            .withOqsmajor(originalEntity.getOqsMajor())
            .withEntityClassF(toEntityClassF(originalEntity))
            .withAttribute(toAttribute(originalEntity))
            .withAttributeF(toAttributeF(originalEntity));

        return builder.build();
    }

    // 转换成JSON类型属性.
    private String toAttribute(OriginalEntity originalEntity) {
        int attrSize = originalEntity.getAttributes().length / 2;


        Map<String, Object> attributeMap = new HashMap(MapUtils.calculateInitSize(attrSize, 0.75F));
        for (Map.Entry<String, Object> attr : originalEntity.listAttributes()) {

            IEntityClass entityClass = originalEntity.getEntityClass();
            StorageValue anyStorageValue = AnyStorageValue.getInstance(attr.getKey());
            Optional<IEntityField> fieldOp = entityClass.field(Long.parseLong(anyStorageValue.logicName()));

            if (!needField(fieldOp, true)) {
                continue;
            }


            IEntityField field = fieldOp.get();
            StorageValue storageValue =
                storageStrategyFactory.getStrategy(field.type())
                    .convertIndexStorageValue(AnyStorageValue.compatibleStorageName(attr.getKey()), attr.getValue());

            while (storageValue != null) {

                ShortStorageName shortStorageName = storageValue.shortStorageName();
                /*
                 * 字符串需要处理特殊字符.
                 */
                if (StorageType.STRING == storageValue.type()) {

                    attributeMap.put(shortStorageName.toString(),
                        SphinxQLHelper.encodeJsonCharset(storageValue.value().toString()));

                } else {

                    attributeMap.put(shortStorageName.toString(), storageValue.value());

                }

                storageValue = storageValue.next();
            }

        }

        try {
            return objectMapper.writeValueAsString(attributeMap);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    // 填允全文属性.
    // TODO: 还没有处理写入跨entity搜索字段. by dongbin
    private String toAttributeF(OriginalEntity source) throws SQLException {

        StringBuilder buff = new StringBuilder();
        for (Map.Entry<String, Object> attr : source.listAttributes()) {
            IEntityClass entityClass = source.getEntityClass();
            StorageValue anyStorageValue = AnyStorageValue.getInstance(attr.getKey());
            Optional<IEntityField> fieldOp = entityClass.field(Long.parseLong(anyStorageValue.logicName()));
            if (!needField(fieldOp, false)) {
                continue;
            }
            IEntityField field = fieldOp.get();
            StorageValue storageValue =
                storageStrategyFactory.getStrategy(field.type())
                    .convertIndexStorageValue(AnyStorageValue.compatibleStorageName(attr.getKey()), attr.getValue());

            if (StorageType.UNKNOWN == anyStorageValue.type()) {
                if (fieldOp.isPresent()) {
                    throw new SQLException(
                        String.format("Unknown physical storage type.[id=%s, name=%s]",
                            anyStorageValue.logicName(),
                            fieldOp.get().name()));
                } else {
                    throw new SQLException(
                        String.format("Unknown physical storage type.[id=%s]",
                            anyStorageValue.logicName()));
                }
            }

            buff.append(wrapperAttribute(field, storageValue)).append(' ');
        }

        return buff.toString();
    }

    // 全文元信息
    private String toEntityClassF(OriginalEntity originalEntity) {
        return originalEntity.getEntityClass().family()
            .stream().map(e -> Long.toString(e.id())).collect(Collectors.joining(" "));
    }

    /**
     * 格式为 {短名称前辍}{内容}{短名称后辍}{储存类型 S|L}.
     *
     * <p>例如:
     * 短名称为 aZl8N0y58M7S.
     *
     * <p>StorageType.STRING.
     *
     * <p>aZl8N0testy58M7S
     *
     * <p>StorageType.Long.
     * aZl8N0123y58M7S
     *
     * <P>搜索字段属性全名用相似的格式,只是做如下改变.
     * {字段code}{内容}{字段code}
     */
    private String wrapperAttribute(IEntityField field, StorageValue storageValue) {

        StringBuilder buff = new StringBuilder();
        StorageValue current = storageValue;
        while (current != null) {

            ShortStorageName shortStorageName = current.shortStorageName();
            /*
             * 字符串需要处理特殊字符.
             * fuzzyType只有字符串才会处理.
             */
            if (StorageType.STRING == current.type()) {
                String strValue = SphinxQLHelper.filterSymbols(current.value().toString());

                if (FieldConfig.FuzzyType.SEGMENTATION == field.config().getFuzzyType()
                    || FieldConfig.FuzzyType.WILDCARD == field.config().getFuzzyType()) {
                    /*
                     * 硬编码字符串长度超过30的将只分词前30个字符.
                     */
                    String limitLenStrValue = strValue.length() > 30 ? strValue.substring(0, 31) : strValue;

                    Tokenizer tokenizer = tokenizerFactory.getTokenizer(field);
                    Iterator<String> words = tokenizer.tokenize(limitLenStrValue);
                    /*
                     * 处理当前字段分词结果.
                     */
                    String word;
                    while (words.hasNext()) {
                        if (buff.length() > 0) {
                            buff.append(' ');
                        }
                        word = words.next();
                        // 防止和原始字符相同的分词结果.
                        if (!word.equals(strValue)) {
                            buff.append(shortStorageName.getPrefix())
                                .append(word)
                                .append(shortStorageName.getSuffix());


                        }
                    }
                    if (buff.length() > 0) {
                        buff.append(' ');
                    }
                }

                /*
                 * 如果是多值,这里会忽略掉字段定位序号.
                 * 1y2p0ijsilver32e8e5S0 1y2p0ijlavender32e8e5S1
                 * 最终在储存时将会去除尾部的定位序号,变成如下.
                 * 1y2p0ijsilver32e8e5S 1y2p0ijlavender32e8e5S
                 */
                if (buff.length() > 0) {
                    buff.append(' ');
                }

                buff.append(shortStorageName.getPrefix())
                    .append(strValue)
                    .append(shortStorageName.getNoLocationSuffix());
            } else {

                if (buff.length() > 0) {
                    buff.append(' ');
                }

                buff.append(shortStorageName.getPrefix())
                    .append(current.value())
                    .append(shortStorageName.getSuffix());
            }

            current = current.next();
        }

        return buff.toString();
    }

    // 根据最终数据的储存目标切割.
    private Collection<OriginalEntitySection> split(Collection<OriginalEntity> originalEntities) {
        DataSource dataSource;
        String indexName;
        String shardKey;
        OriginalEntitySection originalEntitySection;

        int dsSize = writerDataSourceSelector.selects().size();
        int indexWriteNameSize = indexWriteIndexNameSelector.selects().size();

        Map<DataSource, Map<String, OriginalEntitySection>> dsSectionMap =
            new HashMap(MapUtils.calculateInitSize(dsSize, 0.75F));

        Map<String, OriginalEntitySection> nameSectionMap;

        for (OriginalEntity oe : originalEntities) {
            shardKey = Long.toString(oe.getId());
            dataSource = writerDataSourceSelector.select(shardKey);
            indexName = indexWriteIndexNameSelector.select(shardKey);

            nameSectionMap = dsSectionMap.get(dataSource);
            if (nameSectionMap == null) {

                nameSectionMap = new HashMap(MapUtils.calculateInitSize(indexWriteNameSize, 0.75F));
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

        public final void add(OriginalEntity originalEntity) {
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

    /**
     * 需要的判断依据如下.
     * 1. 可搜索字段.
     * 2. 字段存在.
     */
    private boolean needField(Optional<IEntityField> fieldOp, boolean attr) {
        boolean result;
        if (!fieldOp.isPresent()) {
            return false;
        } else {
            IEntityField field = fieldOp.get();
            result = field.config().isSearchable();

            if (logger.isDebugEnabled()) {
                if (!result) {
                    logger.debug("Field {} is filtered out because it is not searchable or does not exist.",
                        fieldOp.get().name());
                }
            }

            // 通用检查表示需要.
            if (result) {
                if (attr) {
                    /*
                     * 检查是否需要出现在属性中.检查如下.
                     * 1. 是否可排序.
                     */
                    result = storageStrategyFactory.getStrategy(field.type()).isSortable();

                } else {
                    // 检查是否需要出现在全文属性中.
                    result = true;
                }
            }
        }

        return result;
    }

}
