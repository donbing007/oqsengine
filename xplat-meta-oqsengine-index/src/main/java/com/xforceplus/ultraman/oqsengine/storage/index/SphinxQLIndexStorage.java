package com.xforceplus.ultraman.oqsengine.storage.index;

import com.xforceplus.ultraman.oqsengine.core.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * 基于 SphinxQL 的索引储存实现.
 *
 *
 * @author dongbin
 * @version 0.1 2020/2/17 17:16
 * @since 1.8
 */
public class SphinxQLIndexStorage implements IndexStorage {

    private static final String BUILD_SQL = "insert into oqsindex (id, entity, pref, cref, numerfields, stringfields) values(?,?,?,?,?,?)";
    private static final String REPLACE_SQL = "replace into oqsindex (id, entity, pref, cref, numerfields, stringfields) values(?,?,?,?,?,?)";
    private static final String DELETE_SQL = "delete from oqsindex where id = ?";
    private static final String SELECT_SQL = "select entity, version, time, status, data from %s where id = ?";

    @Resource(name = "indexDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource
    private TransactionExecutor transactionExecutor;

    @Override
    public List<EntityRef> select(Conditions conditions, Page page) {
        //TODO: 未实现条件搜索.
        return null;
    }

    @Override
    public long build(IEntity entity) throws SQLException {
        doBuildOrReplace(entity, false);
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        doBuildOrReplace(entity, true);
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {

                PreparedStatement st = ((Connection) resource.value()).prepareStatement(DELETE_SQL);
                st.setLong(1, entity.id()); // id

                int size = st.executeUpdate();
                final int onlyOne = 1;
                if (size != onlyOne) {
                    throw new SQLException(String.format("Entity{%s} could not be delete successfully.", entity.toString()));
                }

                return null;
            }
        });

    }

    private void doBuildOrReplace(IEntity entity, boolean replacement) throws SQLException {
        checkId(entity);
        final String sql = replacement ? REPLACE_SQL : BUILD_SQL;

        transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, entity.id()); // id
                    st.setLong(2, entity.entityClass().id()); // entity
                    //st.setLong(3, ); // pref
                    //st.setLong(4, ); // cref
                    //st.setString(5, ); // numberfields
                    //st.setString(6, ); // stringfields
                    int size = st.executeUpdate();

                    // 成功只应该有一条语句影响
                    final int onlyOne = 1;
                    if (size == onlyOne) {
                        return entity.id();
                    } else {
                        throw new SQLException(
                            String.format(
                                "Entity{%s} could not be %s successfully.",
                                entity.toString(),
                                replacement ? "replace" : "build"
                            ));
                    }
                }
            });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == null) {
            throw new SQLException("Invalid entity`s id.");
        }
    }
}
