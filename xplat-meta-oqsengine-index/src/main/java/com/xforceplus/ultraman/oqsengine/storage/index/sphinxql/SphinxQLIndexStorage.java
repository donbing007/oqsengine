package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import com.xforceplus.ultraman.oqsengine.storage.StorageType;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.*;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;
import com.xforceplus.ultraman.oqsengine.storage.query.QueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.value.LongStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.StringStorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactoryAble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 基于 SphinxQL 的索引储存实现.
 * 注意: 这里交所有的 单引号 双引号和斜杠都进行了替换.
 * 此实现并不会进行属性的返回,只会进行查询.
 * <p>
 * 同时使用了一个 json 的字段格式和全文搜索格式储存属性.
 * id, entity, pref, cref, jsonfields, fullfields.
 * 基中 jsonfields 储存的如果是字符串,那会对其中的字符串进行转义.
 *
 * @author dongbin
 * @version 0.1 2020/2/17 17:16
 * @since 1.8
 */
public class SphinxQLIndexStorage implements IndexStorage, StorageStrategyFactoryAble {

    final Logger logger = LoggerFactory.getLogger(SphinxQLIndexStorage.class);

    /**
     * %s 顺序为 where 条件, 排序.
     */
    private static final String SELECT_SQL = "select id, pref, cref from %s where entity = ? %s %s limit ?,?";
    private static final String SELECT_COUNT_SQL = "select count(*) as count from %s where entity = ? %s";

    @Resource(name = "indexQueryOptimizer")
    private QueryOptimizer<String> queryOptimizer;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> writerDataSourceSelector;

    @Resource(name = "indexSearchDataSourceSelector")
    private Selector<DataSource> searchDataSourceSelector;

    @Resource(name = "storageSphinxQLTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "indexStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    private String indexTableName;

    public void setIndexTableName(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException {

        return (Collection<EntityRef>) transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entityClass.id())) {
                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String whereCondition = queryOptimizer.optimizeConditions(conditions).build(conditions);
                    if (!whereCondition.isEmpty()) {
                        whereCondition = SqlKeywordDefine.AND + " " + whereCondition;
                    }

                    PreparedStatement st = null;
                    ResultSet rs = null;
                    if (!page.isSinglePage()) {
                        String countSql = String.format(SELECT_COUNT_SQL, indexTableName, whereCondition);
                        long count = 0;

                        st = ((Connection) resource.value()).prepareStatement(countSql);
                        st.setLong(1, entityClass.id());

                        if (logger.isDebugEnabled()) {
                            logger.debug(st.toString());
                        }

                        rs = st.executeQuery();

                        while (rs.next()) {
                            count = rs.getLong("count");
                            break;
                        }

                        rs.close();
                        st.close();
                        page.setTotalCount(count);
                    }

                    PageScope scope = page.getNextPage();
                    // 超出页数
                    if (scope == null) {
                        return Collections.emptyList();
                    }

                    String orderBy = buildOrderBy(sort);

                    String sql = String.format(SELECT_SQL, indexTableName, whereCondition, orderBy);
                    st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, entityClass.id());
                    st.setLong(2, scope.getStartLine());
                    st.setLong(3, page.getPageSize());

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    rs = st.executeQuery();

                    List<EntityRef> refs = new ArrayList((int) page.getPageSize());
                    while (rs.next()) {
                        refs.add(new EntityRef(
                            rs.getLong(FieldDefine.ID),
                            rs.getLong(FieldDefine.PREF),
                            rs.getLong(FieldDefine.CREF)
                        ));
                    }

                    try {
                        return refs;
                    } finally {
                        if (rs != null) {
                            rs.close();
                        }

                        if (st != null) {
                            st.close();
                        }
                    }
                }
            });
    }

    // 构造排序.
    private String buildOrderBy(Sort sort) {
        StringBuilder buff = new StringBuilder(SqlKeywordDefine.ORDER).append(" ");
        if (sort != null) {
            StorageStrategy storageStrategy = storageStrategyFactory.getStrategy(sort.getField().type());
            Collection<String> storageNames = storageStrategy.toStorageNames(sort.getField());

            for (String storageName : storageNames) {
                if (storageStrategy.storageType() == StorageType.LONG) {
                    buff.append("bigint(")
                        .append(FieldDefine.JSON_FIELDS)
                        .append(".")
                        .append(storageName)
                        .append(")");
                } else {
                    buff.append(FieldDefine.JSON_FIELDS)
                        .append(".")
                        .append(storageName);
                }

                if (sort.isAsc()) {
                    buff.append(" ").append(SqlKeywordDefine.ORDER_TYPE_ASC);
                } else {
                    buff.append(" ").append(SqlKeywordDefine.ORDER_TYPE_DESC);
                }
            }


        } else {
            buff.append("id ").append(SqlKeywordDefine.ORDER_TYPE_ASC);
        }
        return buff.toString();
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        checkId(attribute.id());

        transactionExecutor.execute(
            new DataSourceShardingTask(writerDataSourceSelector, Long.toString(attribute.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    long dataId = attribute.id();
                    Optional<StorageEntity> oldStorageEntityOptional = doSelectStorageEntity(dataId);
                    if (oldStorageEntityOptional.isPresent()) {

                        StorageEntity storageEntity = oldStorageEntityOptional.get();

                        /**
                         * 把新的属性插入旧属性集中替换已有,或新增.
                         */
                        Map<String, Object> completeAttribues = storageEntity.getJsonFields();
                        Map<String, Object> modifiedAttributes = serializeToMap(attribute, true);
                        completeAttribues.putAll(modifiedAttributes);

                        //处理 fulltext
                        storageEntity.setJsonFields(completeAttribues);
                        storageEntity.setFullFields(convertJsonToFull(completeAttribues));

                        new ReplaceStorageCommand(indexTableName).execute(resource, storageEntity);

                    } else {

                        throw new SQLException(
                            String.format("Attempt to update a property on a data that does not exist.[%d]", dataId)
                        );

                    }

                    return null;
                }
            });
    }

    @Override
    public void build(IEntity entity) throws SQLException {
        doBuildOrReplace(entity, false);
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        // 在事务状态,返回值恒为false
        doBuildOrReplace(entity, true);
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity.id());

        transactionExecutor.execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {
            @Override
            public Object run(TransactionResource resource) throws SQLException {
                StorageEntity storageEntity = new StorageEntity(
                        entity.id(),
                        entity.entityClass().id(),
                        entity.family().parent(),
                        entity.family().child(),
                        serializeToMap(entity.entityValue(), true),
                        serializeSetFull(entity.entityValue())
                );
                return new DeleteStorageCommand(indexTableName).execute(resource, storageEntity);
            }
        });

    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    private void doBuildOrReplace(IEntity entity, boolean replacement) throws SQLException {
        checkId(entity.id());

        doBuildReplaceStorageEntity(
            new StorageEntity(
                entity.id(),
                entity.entityClass().id(),
                entity.family().parent(),
                entity.family().child(),
                serializeToMap(entity.entityValue(), true),
                serializeSetFull(entity.entityValue())
            ),
            replacement
        );
    }

    /**
     * <f>fieldId + fieldvalue(unicode)</f> + <f>fieldId + fieldvalue(unicode)</f>....n
     */
    private Set<String> serializeSetFull(IEntityValue entityValue) {
        Set<String> fullSet = new HashSet<>();

        entityValue.values().stream().forEach(v -> {

            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);

            while (storageValue != null) {
                // 这里已经处理成了<F3S>F3S test</F3S>的储存样式.
                fullSet.add(serializeStorageValueFull(storageValue));

                storageValue = storageValue.next();
            }
        });

        return fullSet;
    }

    // 处理成<F123L>F123L 789</F123L> 形式字符串.
    private String serializeStorageValueFull(StorageValue value) {
        StringBuilder buff = new StringBuilder();
        buff.append("<").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
        buff.append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.storageName()).append(' ');
        if (value.type() == StorageType.STRING) {
            buff.append(SphinxQLHelper.encodeSpecialCharset(value.value().toString()));
        } else {
            buff.append(value.value().toString());
        }
        buff.append("</").append(SphinxQLHelper.ATTRIBUTE_FULL_FIELD_PREFIX).append(value.groupStorageName()).append(">");
        return buff.toString();
    }

    /**
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    private Map<String, Object> serializeToMap(IEntityValue values, boolean encodeString) {
        Map<String, Object> data = new HashMap<>(values.values().size());
        values.values().stream().forEach(v -> {
            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);

            while (storageValue != null) {
                if (storageValue.type() == StorageType.STRING) {
                    data.put(storageValue.storageName(),
                        encodeString ? SphinxQLHelper.encodeSpecialCharset((String) storageValue.value()) : storageValue.value());
                } else {
                    data.put(storageValue.storageName(), storageValue.value());
                }
                storageValue = storageValue.next();
            }
        });

        return data;
    }

    // 转换 json 字段为全文搜索字段.
    private Set<String> convertJsonToFull(Map<String, Object> attributes) {
        Set<String> fullfileds = new HashSet<>();
        Object value;
        StorageValue storageValue = null;
        for (String key : attributes.keySet()) {
            value = attributes.get(key);

            if (Long.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, (Long) value, false);

            } else {

                storageValue = new StringStorageValue(key, (String) value, false);
            }

            fullfileds.add(serializeStorageValueFull(storageValue));
        }

        return fullfileds;
    }


    private void checkId(long id) throws SQLException {
        if (id == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    // 查询原始数据.
    private Optional<StorageEntity> doSelectStorageEntity(long id) throws SQLException {
        return (Optional<StorageEntity>) transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(id)) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    StorageEntity input = new StorageEntity();
                    input.setId(id);
                    StorageEntity storageEntity = new SelectByIdStorageCommand(indexTableName).execute(resource, input);

                    return Optional.ofNullable(storageEntity);
                }
            });
    }

    // 更新原始数据.
    private void doBuildReplaceStorageEntity(StorageEntity storageEntity, boolean replacement) throws SQLException {
        transactionExecutor.execute(
            new DataSourceShardingTask(writerDataSourceSelector, Long.toString(storageEntity.getId())) {
                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    if(replacement) {
                        return new ReplaceStorageCommand(indexTableName).execute(resource, storageEntity);
                    } else {
                        return new BuildStorageCommand(indexTableName).execute(resource, storageEntity);
                    }
                }
            });
    }

}
