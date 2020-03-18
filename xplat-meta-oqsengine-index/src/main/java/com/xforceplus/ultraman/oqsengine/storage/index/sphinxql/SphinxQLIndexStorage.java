package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
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

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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

    private static final String WRITER_SQL = "%s into %s (%s, %s, %s, %s, %s, %s) values(?,?,?,?,?,?)";
    private static final String DELETE_SQL = "delete from %s where id = ?";
    private static final String SELECT_SQL = "select id, pref, cref from %s where entity = ? and %s order by %s limit ?,?";
    private static final String SELECT_COUNT_SQL = "select count(*) as count from %s where entity = ? and %s";
    private static final String SELECT_FROM_ID_SQL = "select id, pref, cref, entity, jsonfields from %s where id = ?";

    private String buildSql;
    private String replaceSql;

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

    @PostConstruct
    public void init() {
        buildSql =
            String.format(WRITER_SQL,
                "insert", indexTableName,
                FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
        replaceSql =
            String.format(WRITER_SQL,
                "replace", indexTableName,
                FieldDefine.ID, FieldDefine.ENTITY, FieldDefine.PREF, FieldDefine.CREF,
                FieldDefine.JSON_FIELDS, FieldDefine.FULL_FIELDS);
    }

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException {

        return (Collection<EntityRef>) transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entityClass.id())) {
                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String whereCondition = queryOptimizer.optimizeConditions(conditions).build(conditions);
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

                    String orderBy = buildOrderBy(sort);

                    String sql = String.format(SELECT_SQL, indexTableName, whereCondition, orderBy);
                    st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, entityClass.id());
                    st.setLong(2, scope.getStartLine());
                    st.setLong(3, scope.getEndLine());

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
        StringBuilder buff = new StringBuilder();
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
                    buff.append(" ASC");
                } else {
                    buff.append(" DESC");
                }
            }


        } else {
            buff.append("id ASC");
        }
        return buff.toString();
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        checkId(attribute.id());

        transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(attribute.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    long dataId = attribute.id();
                    Optional<StorageEntity> oldStorageEntityOptional = doSelectStorageEntity(dataId);
                    if (oldStorageEntityOptional.isPresent()) {

                        StorageEntity storageEntity = oldStorageEntityOptional.get();

                        /**
                         * 把新的属性插入旧属性集中替换已有,或新增.
                         */
                        JSONObject completeJson = storageEntity.getJsonFields();
                        JSONObject modifiedJson = serializeToJson(attribute, true);
                        for (String key : modifiedJson.keySet()) {
                            completeJson.put(key, modifiedJson.get(key));
                        }

                        //处理 fulltext
                        Set<String> completeFull = convertJsonToFull(completeJson);
                        storageEntity.setJsonFields(completeJson);
                        storageEntity.setFullFields(completeFull);

                        doBuildReplaceStorageEntity(storageEntity, true);

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
        if (!doBuildOrReplace(entity, false)) {
            throw new SQLException(String.format("Entity{%s} could not be created successfully.", entity.toString()));
        }
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

                String sql = String.format(DELETE_SQL, indexTableName);
                PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                st.setLong(1, entity.id()); // id

                if (logger.isDebugEnabled()) {
                    logger.debug(st.toString());
                }

                // 在事务状态,返回值恒等于0.
                st.executeUpdate();

                try {
                    return null;
                } finally {
                    if (st != null) {
                        st.close();
                    }
                }
            }
        });

    }


    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    private Set<String> setGlobalFlag(Set<String> fullfields) {
        if (fullfields == null) {
            return fullfields;
        }
        /**
         * 增加一个系统字段,当在查询所有数据的时候利用全文搜索引擎可以使用.
         */
        return new HashSet<String>(fullfields) {{
            add(SphinxQLHelper.ALL_DATA_FULL_TEXT);
        }};
    }


    private boolean doBuildOrReplace(IEntity entity, boolean replacement) throws SQLException {
        checkId(entity.id());

        return doBuildReplaceStorageEntity(
            new StorageEntity(
                entity.id(),
                entity.entityClass().id(),
                entity.family().parent(),
                entity.family().child(),
                serializeToJson(entity.entityValue(), true),
                serializeSetFull(entity.entityValue())
            ),
            replacement
        );
    }

    /**
     * fieldId + fieldvalue(unicode) + space + fieldId + fieldvalue(unicode)....n
     */
    private Set<String> serializeSetFull(IEntityValue entityValue) {
        Set<String> fullSet = new HashSet<>();
        entityValue.values().stream().forEach(v -> {

            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);
            while (storageValue != null) {
                fullSet.add(SphinxQLHelper.encodeFullText(storageValue));
                storageValue = storageValue.next();
            }
        });

        return fullSet;
    }

    /**
     * {
     * "{fieldId}" : fieldValue
     * }
     */
    private JSONObject serializeToJson(IEntityValue values, boolean encodeString) {
        Map<String, Object> data = new HashMap<>(values.values().size());
        values.values().stream().forEach(v -> {
            StorageValue storageValue = storageStrategyFactory.getStrategy(v.getField().type()).toStorageValue(v);

            while (storageValue != null) {
                if (storageValue.type() == StorageType.STRING) {
                    data.put(storageValue.storageName(),
                        encodeString ? SphinxQLHelper.encodeString((String) storageValue.value()) : storageValue.value());
                } else {
                    data.put(storageValue.storageName(), storageValue.value());
                }
                storageValue = storageValue.next();
            }
        });

        return new JSONObject(data);
    }

    // 格式化全文属性为字符串.
    private String toFullString(Set<String> fullFields) {
        return fullFields.stream().collect(Collectors.joining(" "));
    }

    // 格式化 JSON 属性为字符串.
    private String toJsonString(JSONObject jsonObject) {
        return JSON.toJSONString(jsonObject);
    }

    // 转换 json 字段为全文搜索字段.
    private Set<String> convertJsonToFull(JSONObject jsonObject) {
        Set<String> fullfileds = new HashSet<>();
        Object value;
        StorageValue storageValue = null;
        for (String key : jsonObject.keySet()) {
            value = jsonObject.get(key);

            if (Integer.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, ((Integer) value).longValue(), false);


            } else if (Long.class.isInstance(value)) {

                storageValue = new LongStorageValue(key, (Long) value, false);

            } else {

                storageValue = new StringStorageValue(key, (String) value, false);
            }

            fullfileds.add(SphinxQLHelper.encodeFullText(storageValue));
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

                    PreparedStatement st = null;
                    ResultSet rs = null;
                    try {
                        String sql = String.format(SELECT_FROM_ID_SQL, indexTableName);
                        st = ((Connection) resource.value()).prepareStatement(sql);
                        st.setLong(1, id);

                        rs = st.executeQuery();
                        StorageEntity storageEntity = null;
                        if (rs.next()) {
                            storageEntity = new StorageEntity(
                                id,
                                rs.getLong(FieldDefine.ENTITY),
                                rs.getLong(FieldDefine.PREF),
                                rs.getLong(FieldDefine.CREF),
                                JSON.parseObject(rs.getString(FieldDefine.JSON_FIELDS)),
                                null
                            );
                        }


                        return Optional.ofNullable(storageEntity);
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

    // 更新原始数据.
    private boolean doBuildReplaceStorageEntity(StorageEntity storageEntity, boolean replacement) throws SQLException {

        final String sql = String.format(replacement ? replaceSql : buildSql, indexTableName);

        return (boolean) transactionExecutor.execute(
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(storageEntity.getId())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);

                    // id, entity, pref, cref, jsonfileds, fullfileds
                    st.setLong(1, storageEntity.getId()); // id
                    st.setLong(2, storageEntity.getEntity()); // entity
                    st.setLong(3, storageEntity.getPref()); // pref
                    st.setLong(4, storageEntity.getCref()); // cref
                    // attribute
                    st.setString(5, toJsonString(storageEntity.getJsonFields()));
                    // full
                    st.setString(6, toFullString(storageEntity.getFullFields()));

                    if (logger.isDebugEnabled()) {
                        logger.debug(st.toString());
                    }

                    int size = st.executeUpdate();

                    try {
                        // 成功只应该有一条语句影响
                        final int onlyOne = 1;
                        return size == onlyOne;

                    } finally {
                        st.close();
                    }
                }
            });
    }

    // 原始储存格式.
    private class StorageEntity {
        private long id;
        private long entity;
        private long pref;
        private long cref;
        private JSONObject jsonFields;
        private Set<String> fullFields;

        public StorageEntity(long id, long entity, long pref, long cref, JSONObject jsonFields, Set<String> fullFields) {
            this.id = id;
            this.entity = entity;
            this.pref = pref;
            this.cref = cref;
            this.jsonFields = jsonFields;
            this.fullFields = setGlobalFlag(fullFields);
        }

        public long getId() {
            return id;
        }

        public long getEntity() {
            return entity;
        }

        public long getPref() {
            return pref;
        }

        public long getCref() {
            return cref;
        }

        public JSONObject getJsonFields() {
            return jsonFields;
        }

        public Set<String> getFullFields() {
            return fullFields;
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setEntity(long entity) {
            this.entity = entity;
        }

        public void setPref(long pref) {
            this.pref = pref;
        }

        public void setCref(long cref) {
            this.cref = cref;
        }

        public void setJsonFields(JSONObject jsonFields) {
            this.jsonFields = jsonFields;
        }

        public void setFullFields(Set<String> fullFields) {
            this.fullFields = setGlobalFlag(fullFields);
        }
    }
}
