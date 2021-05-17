package com.xforceplus.ultraman.oqsengine.storage.master;

import com.alibaba.google.common.collect.Lists;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.master.executor.*;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueIndexValue;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueKeyGenerator;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/22 5:58 PM
 */
public class MasterUniqueStorage implements UniqueMasterStorage {

    @Resource
    UniqueKeyGenerator keyGenerator;

    private String tableName;


    @Resource(name = "uniqueStorageJDBCTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Resource(name = "masterDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;


    @Resource(name = "uniqueTableNameSelector")
    private Selector<String> uniqueTableNameSelector;

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private long queryTimeout;

    public void setQueryTimeout(long queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    @PostConstruct
    public void init() {

        if (queryTimeout <= 0) {
            setQueryTimeout(3000L);
        }
    }

    @Override
    public int build(IEntity entity, IEntityClass entityClass) throws SQLException {
        // todo:如果包含业务主键的实体，才需要走以下逻辑。
        String uniqueKey = buildEntityUniqueKey(entity, entityClass);
        if (StringUtils.isEmpty(uniqueKey)) {
            return 0;
        }
        return (int) transactionExecutor.execute(
                (tx, resource, hint) -> {
                   StorageUniqueEntity storageUniqueEntity =  StorageUniqueEntity.builder().id(entity.id()).key(uniqueKey)
                            .entityClasses(getEntityClasses(entityClass)).build();
//                    fullTransactionInformation(storageEntityBuilder, resource);
                    return BuildUniqueExecutor.build(tableName, resource, queryTimeout).execute(storageUniqueEntity);
                });
    }

    private List<String> getAncestorCode(IEntityClass entityClass) {
        List<String> codes = Lists.newArrayList(entityClass.code());
        while (entityClass.father().isPresent()) {
            codes.add(entityClass.father().get().code());
            entityClass = entityClass.father().get();
        }
        return codes;
    }

    private String buildEntityUniqueKey(IEntity entity, IEntityClass entityClass) throws SQLException {
        List<String> codes = getAncestorCode(entityClass);
        Map<String, UniqueIndexValue> values = keyGenerator.generator(entity);
        Optional<UniqueIndexValue> indexValue = values.values().stream()
            .filter(item -> codes.contains(item.getCode()))
                .findAny();
        return indexValue.isPresent() ? indexValue.get().getValue() : "";
    }

    // 填充类型信息
    private long[] getEntityClasses(IEntityClass entityClass) {
        Collection<IEntityClass> family = entityClass.family();
        long[] tileEntityClassesIds = family.stream().mapToLong(ecs -> ecs.id()).toArray();
        return tileEntityClassesIds;
    }

    @Override
    public int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
        String uniqueKey = buildEntityUniqueKey(entity, entityClass);
        if (StringUtils.isBlank(uniqueKey)) {
            return 0;
        }
        return (int) transactionExecutor.execute(
                (tx, resource, hint) -> {
                    StorageUniqueEntity.StorageUniqueEntityBuilder storageEntityBuilder = StorageUniqueEntity.builder();
                    storageEntityBuilder.id(entity.id()).key(uniqueKey);
                    fullEntityClassInformation(storageEntityBuilder, entityClass);
//                    fullTransactionInformation(storageEntityBuilder, resource);
                    return UpdateUniqueExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                });

    }

    // 填充类型信息
    private void fullEntityClassInformation(StorageUniqueEntity.StorageUniqueEntityBuilder storageEntityBuilder, IEntityClass
            entityClass) {
        Collection<IEntityClass> family = entityClass.family();
        long[] tileEntityClassesIds = family.stream().mapToLong(ecs -> ecs.id()).toArray();
        storageEntityBuilder.entityClasses(tileEntityClassesIds);
    }

    @Override
    public int delete(IEntity entity, IEntityClass entityClass) throws SQLException {
        return (int) transactionExecutor.execute(
                (tx, resource, hint) -> {
                    StorageUniqueEntity.StorageUniqueEntityBuilder storageEntityBuilder = StorageUniqueEntity.builder();
                    storageEntityBuilder.id(entity.id());
//                    fullTransactionInformation(storageEntityBuilder, resource);
                    return DeleteUniqueExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                });
    }

//    private Object buildUniqueRecord(IEntity entity, StorageCommand command) throws SQLException {
//        int ret = 0;
//        if (containUniqueConfig(entity)) {
//            String uniqueKey = buildEntityUniqueKey(entity);
//            ret = (int) transactionExecutor.execute(new AbstractDataSourceShardingTask(dataSourceSelector, uniqueKey) {
//                @Override
//                public Object run(TransactionResource resource, ExecutorHint hint) throws SQLException {
//                    StorageUniqueEntity storageUniqueEntity = StorageUniqueEntity.builder()
//                            .id(entity.id())
//                            .entity(entity.entityClass().id())
//                            .key(uniqueKey).build();
//                    return command.execute(resource, storageUniqueEntity);
//                }
//            });
//        }
//        return ret;
//    }



    private String buildEntityUniqueKeyByEntityClass(List<BusinessKey> businessKeys, IEntityClass entityClass) throws SQLException {
        Map<String, UniqueIndexValue> values = keyGenerator.generator(businessKeys, entityClass);
        Optional<UniqueIndexValue> indexValue = matchUniqueConfig(entityClass, values);
        return indexValue.isPresent() ? indexValue.get().getValue() : "";
    }


    @Override
    public boolean containUniqueConfig(List<BusinessKey> businessKeys, IEntityClass entityClass) {
        Map<String, UniqueIndexValue> keys = keyGenerator.generator(businessKeys, entityClass);
        return matchUniqueConfig(entityClass, keys).isPresent();
    }

    @Override
    public int deleteDirectly(IEntity entity) throws SQLException {
        //shardKey强制为空 ，因此 不支持分库 todo 等能够拿到子类的shardkey信息再实现。
        return (int) transactionExecutor.execute(
                (tx, resource, hint) -> {
                    StorageUniqueEntity.StorageUniqueEntityBuilder storageEntityBuilder = StorageUniqueEntity.builder();
                    storageEntityBuilder.id(entity.id());
//                    fullTransactionInformation(storageEntityBuilder, resource);
                    return DeleteUniqueExecutor.build(tableName, resource, queryTimeout).execute(storageEntityBuilder.build());
                });
    }

    private Optional<UniqueIndexValue> matchUniqueConfig(IEntityClass entityClass, Map<String, UniqueIndexValue> keys) {
        return keys.values().stream()
                .filter(item -> item.getCode()
                        .equalsIgnoreCase(entityClass.code()))
                .findAny();
    }


    @Override
    public Optional<StorageUniqueEntity> select(List<BusinessKey> businessKeys, IEntityClass entityClass) throws SQLException {
        if (!containUniqueConfig(businessKeys, entityClass)) {
            return Optional.empty();
        }
        String uniqueKey = buildEntityUniqueKeyByEntityClass(businessKeys, entityClass);
        return (Optional<StorageUniqueEntity>) transactionExecutor.execute((tx, resource, hint) -> {
            Optional<StorageUniqueEntity> seOP =
                    new QueryUniqueExecutor(tableName, resource, entityClass, queryTimeout).execute(uniqueKey);
                return seOP;
        });
    }
}
