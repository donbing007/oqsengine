package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;
import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

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
public class SphinxQLIndexStorage implements IndexStorage {

    final Logger logger = LoggerFactory.getLogger(SphinxQLIndexStorage.class);

    @Resource
    SphinxQLIndexAction sphinxQLIndexAction;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> writerDataSourceSelector;

    @Resource(name = "indexSearchDataSourceSelector")
    private Selector<DataSource> searchDataSourceSelector;

    @Resource(name = "storageSphinxQLTransactionExecutor")
    private TransactionExecutor transactionExecutor;

    @Override
    public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
            throws SQLException {

        return (Collection<EntityRef>) transactionExecutor.execute(
                new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entityClass.id())) {
                    @Override
                    public Object run(TransactionResource resource) throws SQLException {
                        return sphinxQLIndexAction.select(((Connection) resource.value()), conditions, entityClass, sort, page);
                    }
                });
    }

    @Override
    public void replaceAttribute(IEntityValue attribute) throws SQLException {
        checkId(attribute.id());

        transactionExecutor.execute(
                new DataSourceShardingTask(searchDataSourceSelector, Long.toString(attribute.id())) {

                    @Override
                    public Object run(TransactionResource resource) throws SQLException {
                        sphinxQLIndexAction.replaceAttribute(((Connection) resource.value()), attribute);

                        return null;
                    }
                });
    }

    @Override
    public void build(IEntity entity) throws SQLException {
        transactionExecutor.execute(
                new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entity.id()), OpTypeEnum.BUILD) {

                    @Override
                    public Object run(TransactionResource resource) throws SQLException {
                        return sphinxQLIndexAction.build(((Connection) resource.value()), entity);
                    }
                });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        transactionExecutor.execute(
                new DataSourceShardingTask(searchDataSourceSelector, Long.toString(entity.id()), OpTypeEnum.REPLACE) {

                    @Override
                    public Object run(TransactionResource resource) throws SQLException {
                        return sphinxQLIndexAction.replace(((Connection) resource.value()), entity);
                    }
                });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        transactionExecutor.execute(new DataSourceShardingTask(writerDataSourceSelector, Long.toString(entity.id()), OpTypeEnum.DELETE) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {
                return sphinxQLIndexAction.delete(((Connection) resource.value()), entity);
            }
        });

    }

    private void checkId(long id) throws SQLException {
        if (id == 0) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

}
