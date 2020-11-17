package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceNoShardResourceTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.executor.hint.ExecutorHint;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.master.define.OperationType;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.master.strategy.conditions.SQLJsonConditionsBuilderFactory;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import com.xforceplus.ultraman.oqsengine.storage.utils.IEntityValueBuilder;
import com.xforceplus.ultraman.oqsengine.storage.value.StorageValue;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategy;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.StorageStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 主要储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:11
 * @since 1.8
 */
public class SQLMasterStorage implements MasterStorage {

    final Logger logger = LoggerFactory.getLogger(SQLMasterStorage.class);

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    @Resource(name = "storageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterStorageStrategy")
    private StorageStrategyFactory storageStrategyFactory;

    @Resource(name = "entityValueBuilder")
    private IEntityValueBuilder<String> entityValueBuilder;

    @Resource(name = "masterConditionsBuilderFactory")
    private SQLJsonConditionsBuilderFactory conditionsBuilderFactory;

    private String tableName;

    private long queryTimeout;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    @PostConstruct
    public void init() {

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }
    }

    @Override
    public Collection<EntityRef> select(long commitid, Conditions conditions, IEntityClass entityClass, Sort sort)
        throws SQLException {
        return (Collection<EntityRef>) transactionExecutor.execute(new DataSourceNoShardResourceTask(masterDataSource) {
            @Override
            public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                hint.setReadOnly(true);
                return QueryLimitCommitidByConditionsExecutor.build(
                    tableName,
                    resource,
                    entityClass,
                    sort,
                    commitid,
                    queryTimeout,
                    conditionsBuilderFactory,
                    storageStrategyFactory).execute(conditions);
            }
        });
    }

    @Override
    public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute(
            new DataSourceNoShardResourceTask(masterDataSource) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    Optional<StorageEntity> seOP = QueryExecutor.buildHaveDetail(tableName, resource, queryTimeout).execute(id);
                    hint.setReadOnly(true);
                    if (seOP.isPresent()) {
                        return buildEntityFromStorageEntity(seOP.get(), entityClass);
                    } else {
                        return Optional.empty();
                    }
                }

            });
    }

    @Override
    public Collection<IEntity> selectMultiple(Map<Long, IEntityClass> ids) throws SQLException {

        Collection<StorageEntity> storageEntities = (Collection<StorageEntity>) transactionExecutor.execute(
            new DataSourceNoShardResourceTask(masterDataSource) {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {

                    return MultipleQueryExecutor.build(tableName, resource, queryTimeout).execute(ids.keySet());
                }
            }
        );

        return storageEntities.parallelStream().filter(se -> ids.containsKey(se.getId())).map(se -> {
            IEntityClass entityClass = ids.get(se.getId());
            Optional<IEntity> op;
            try {
                op = buildEntityFromStorageEntity(se, entityClass);
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            // 不可能为空.
            return op.get();

        }).collect(Collectors.toList());

    }

    @Override
    public int synchronize(long sourceId, long targetId) throws SQLException {
        Optional<StorageEntity> oldOp = (Optional<StorageEntity>) transactionExecutor.execute(
            new DataSourceNoShardResourceTask(masterDataSource) {
                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    return QueryExecutor.buildNoDetail(tableName, resource, queryTimeout).execute(sourceId);
                }
            }
        );
        if (oldOp.isPresent()) {

            return (int) transactionExecutor.execute(
                new DataSourceNoShardResourceTask(masterDataSource) {
                    @Override
                    public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                        StorageEntity targetEntity = oldOp.get();
                        targetEntity.setId(targetId);

                        hint.setReadOnly(false);
                        return UpdateVersionAndTxExecutor.build(tableName, resource, queryTimeout).execute(targetEntity);
                    }
                }

            );
        } else {

            return 0;
        }

    }

    @Override
    public int build(IEntity entity) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            new DataSourceNoShardResourceTask(masterDataSource) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setEntity(entity.entityClass().id());
                    storageEntity.setPref(entity.family().parent());
                    storageEntity.setCref(entity.family().child());
                    storageEntity.setTime(entity.time());
                    storageEntity.setDeleted(false);
                    storageEntity.setAttribute(toJson(entity.entityValue()));
                    storageEntity.setMeta(buildSearchAbleSyncMeta(entity.entityClass()));

                    storageEntity.setOp(OperationType.CREATE.getValue());
                    Optional<Transaction> tOp = resource.getTransaction();
                    if (tOp.isPresent()) {
                        storageEntity.setTx(tOp.get().id());
                    } else {
                        logger.warn("Build run with no transaction, unable to get the transaction ID.");
                        storageEntity.setTx(0);
                    }
                    storageEntity.setCommitid(CommitHelper.getUncommitId());

                    hint.setReadOnly(false);
                    return BuildExecutor.build(tableName, resource, queryTimeout).execute(storageEntity);
                }
            });
    }

    @Override
    public int replace(IEntity entity) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            new DataSourceNoShardResourceTask(masterDataSource) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setEntity(entity.entityClass().id());
                    storageEntity.setVersion(entity.version());
                    storageEntity.setPref(entity.family().parent());
                    storageEntity.setCref(entity.family().child());
                    storageEntity.setTime(entity.time());
                    storageEntity.setAttribute(toJson(entity.entityValue()));
                    storageEntity.setMeta(buildSearchAbleSyncMeta(entity.entityClass()));

                    storageEntity.setOp(OperationType.UPDATE.getValue());
                    Optional<Transaction> tOp = resource.getTransaction();
                    if (tOp.isPresent()) {
                        storageEntity.setTx(tOp.get().id());
                    } else {
                        logger.warn("Replace run with no transaction, unable to get the transaction ID.");
                        storageEntity.setTx(0);
                    }
                    storageEntity.setCommitid(CommitHelper.getUncommitId());

                    hint.setReadOnly(false);
                    return ReplaceExecutor.build(tableName, resource, queryTimeout).execute(storageEntity);

                }
            });
    }

    @Override
    public int delete(IEntity entity) throws SQLException {
        checkId(entity);

        return (int) transactionExecutor.execute(
            new DataSourceNoShardResourceTask(masterDataSource) {

                @Override
                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
                    /**
                     * 删除数据时不再关心字段信息.
                     */
                    StorageEntity storageEntity = new StorageEntity();
                    storageEntity.setId(entity.id());
                    storageEntity.setVersion(entity.version());
                    storageEntity.setTime(entity.time());

                    storageEntity.setOp(OperationType.DELETE.getValue());
                    Optional<Transaction> tOp = resource.getTransaction();
                    if (tOp.isPresent()) {
                        storageEntity.setTx(tOp.get().id());
                    } else {
                        logger.warn("Delete run with no transaction, unable to get the transaction ID.");
                        storageEntity.setTx(0);
                    }
                    storageEntity.setCommitid(CommitHelper.getUncommitId());

                    hint.setReadOnly(false);
                    return DeleteExecutor.build(tableName, resource, queryTimeout).execute(storageEntity);
                }
            });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

    private IEntityValue toEntityValue(long id, IEntityClass entityClass, String json) throws SQLException {

        // 以字段逻辑名称为 key, 字段信息为 value.
        Map<String, IEntityField> fieldTable = entityClass.fields()
            .stream().collect(Collectors.toMap(f -> Long.toString(f.id()), f -> f, (f0, f1) -> f0));

        return entityValueBuilder.build(id, fieldTable, json);

    }

    // 属性名称使用的是属性 F + {id}.
    private String toJson(IEntityValue value) {

        JSONObject object = new JSONObject();
        StorageStrategy storageStrategy;
        StorageValue storageValue;
        for (IValue logicValue : value.values()) {
            storageStrategy = storageStrategyFactory.getStrategy(logicValue.getField().type());
            storageValue = storageStrategy.toStorageValue(logicValue);
            while (storageValue != null) {
                object.put(FieldDefine.ATTRIBUTE_PREFIX + storageValue.storageName(), storageValue.value());
                storageValue = storageValue.next();
            }
        }
        return object.toJSONString();

    }

    private Optional<IEntity> buildEntityFromStorageEntity(StorageEntity se, IEntityClass entityClass) throws SQLException {
        if (se == null) {
            return Optional.empty();
        }
        long dataEntityClassId = se.getEntity();
        if (entityClass.id() != dataEntityClassId) {
            throw new SQLException(
                String.format(
                    "The incorrect Entity type is expected to be %d, but the actual data type is %d."
                    , entityClass.id(), dataEntityClassId));
        }

        long id = se.getId();
        Entity entity = new Entity(
            id,
            entityClass,
            toEntityValue(se.getId(), entityClass, se.getAttribute()),
            new EntityFamily(se.getPref(), se.getCref()),
            se.getVersion()
        );
        return Optional.of(entity);
    }

    /**
     * 构造提示同步数据时的元信息.
     * 只会包含可搜索字段信息.
     * ["{ID}-{type}",.....]
     *
     * @param entityClass entity信息.
     * @return 字符串表示.
     */
    private String buildSearchAbleSyncMeta(IEntityClass entityClass) {
        return "[" + entityClass.fields().stream()
            .filter(f -> f.config().isSearchable())
            .map(f -> "\"" + String.join("-", Long.toString(f.id()), f.type().getType()) + "\"")
            .collect(Collectors.joining(",")) + "]";
    }
}
