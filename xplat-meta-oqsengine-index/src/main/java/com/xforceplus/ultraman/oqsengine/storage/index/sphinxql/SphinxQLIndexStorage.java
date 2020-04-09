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
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.constant.SQLConstant;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.SqlKeywordDefine;
import com.xforceplus.ultraman.oqsengine.storage.query.QueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.command.StorageCommandInvoker;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
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

    @Resource(name = "sphinxQLIndexStorageCommandInvoker")
    private StorageCommandInvoker storageCommandInvoker;


    private String indexTableName;

    public void setIndexTableName(String indexTableName) {
        this.indexTableName = indexTableName;
    }

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page) throws SQLException {

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
                        String countSql = String.format(SQLConstant.SELECT_COUNT_SQL, indexTableName, whereCondition);
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

                    String sql = String.format(SQLConstant.SELECT_SQL, indexTableName, whereCondition, orderBy);
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
            new DataSourceShardingTask(searchDataSourceSelector, Long.toString(attribute.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    return storageCommandInvoker.execute(resource, OpTypeEnum.REPLACE_ATTRIBUTE, attribute);
                }
            });
    }

    @Override
    public void build(IEntity entity) throws SQLException {
        CommonUtil.checkId(entity.id());

        transactionExecutor.execute(
                new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {
                    @Override
                    public Object run(TransactionResource resource) throws SQLException {
                        return storageCommandInvoker.execute(resource, OpTypeEnum.BUILD, entity);
                    }
                });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        CommonUtil.checkId(entity.id());

        transactionExecutor.execute(
                new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {
                    @Override
                    public Object run(TransactionResource resource) throws SQLException {
                        return storageCommandInvoker.execute(resource, OpTypeEnum.BUILD, entity);
                    }
                });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity.id());

        transactionExecutor.execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {
                return storageCommandInvoker.execute(resource, OpTypeEnum.DELETE, entity);
            }
        });

    }

    @Override
    public void setStorageStrategy(StorageStrategyFactory storageStrategyFactory) {
        this.storageStrategyFactory = storageStrategyFactory;
    }

    private void checkId(long id) throws SQLException {
        if (id == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

}
