package com.xforceplus.ultraman.oqsengine.storage.master;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntity;
import com.xforceplus.ultraman.oqsengine.core.metadata.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.Entity;
import com.xforceplus.ultraman.oqsengine.storage.executor.DataSourceShardingTask;
import com.xforceplus.ultraman.oqsengine.storage.executor.TransactionExecutor;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionResource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 主要储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 22:11
 * @since 1.8
 */
public class SQLMasterStorage implements MasterStorage {

    private static final String BUILD_SQL = "insert into %s (id, entity, version, time, data, deleted) values(?,?,?,?,?,?)";
    private static final String REPLACE_SQL = "update %s set version = version + 1, time = ?, data = ? where id = ?";
    private static final String DELETE_SQL = "update %s set version = version + 1, deleted = ?, time = ? where id = ?";
    private static final String SELECT_SQL = "select entity, version, time, status, data from %s where id = ?";

    @Resource(name = "masterDataSourceSelector")
    private Selector<DataSource> dataSourceSelector;

    @Resource(name = "masterTableSelector")
    private Selector<String> tableNameSelector;

    @Resource
    private TransactionExecutor transactionExecutor;

    @Resource
    private LongIdGenerator idGenerator;


    @Override
    public Optional<IEntity> select(long id, IEntityClass entityClass) throws SQLException {
        return (Optional<IEntity>) transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(id)) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String tableName = tableNameSelector.select(Long.toString(id));
                    String sql = String.format(SELECT_SQL, tableName);

                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, id); // id

                    ResultSet resultSet = st.executeQuery();
                    if (resultSet.next()) {
                        Entity entity = new Entity();

                        return Optional.of(entity);
                    } else {
                        return Optional.empty();
                    }
                }
            });
    }

    @Override
    public List<IEntity> selectMultiple(Map<IEntityClass, int[]> ids) throws SQLException {
        // TODO: 还未实现. by dongbin
        return null;
    }


    @Override
    public long build(IEntity entity) throws SQLException {

        return (long) transactionExecutor.execute(
            new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

                @Override
                public Object run(TransactionResource resource) throws SQLException {
                    String tableName = tableNameSelector.select(Long.toString(entity.id()));
                    String sql = String.format(BUILD_SQL, tableName);

                    long newId = idGenerator.next();
                    PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                    st.setLong(1, newId); // id
                    st.setLong(2, entity.entityClass().id()); // entity
                    st.setLong(3, 0); // version
                    st.setLong(4, System.currentTimeMillis()); // time
                    //TODO: entityvalue 转换成 josn. by dongbin 2020/02/16
                    //st.setString(5,data); // data
                    st.setBoolean(6, false); // deleted

                    int size = st.executeUpdate();

                    /**
                     * 插入影响条件恒定为1.
                     */
                    final int onlyOne = 1;
                    if (size != onlyOne) {
                        throw new SQLException(
                            String.format("Entity{%s} could not be created successfully.", entity.toString()));
                    }

                    return newId;
                }
            });
    }

    @Override
    public void replace(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {
                String tableName = tableNameSelector.select(Long.toString(entity.id()));
                String sql = String.format(REPLACE_SQL, tableName);
                PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                st.setLong(1, System.currentTimeMillis()); // time
                //TODO: entityvalue 转换成 josn. by dongbin 2020/02/16
                //st.setString(2, data) // data
                st.setLong(3, entity.id()); // id

                int size = st.executeUpdate();
                final int onlyOne = 1;
                if (size != onlyOne) {
                    throw new SQLException(String.format("Entity{%s} could not be replace successfully.", entity.toString()));
                }

                return null;
            }
        });
    }

    @Override
    public void delete(IEntity entity) throws SQLException {
        checkId(entity);

        transactionExecutor.execute(new DataSourceShardingTask(dataSourceSelector, Long.toString(entity.id())) {

            @Override
            public Object run(TransactionResource resource) throws SQLException {
                String tableName = tableNameSelector.select(Long.toString(entity.id()));
                String sql = String.format(DELETE_SQL, tableName);
                PreparedStatement st = ((Connection) resource.value()).prepareStatement(sql);
                st.setBoolean(1, true); // deleted
                st.setLong(2, System.currentTimeMillis()); // time
                st.setLong(3, entity.id()); // id

                int size = st.executeUpdate();
                final int onlyOne = 1;
                if (size != onlyOne) {
                    throw new SQLException(String.format("Entity{%s} could not be delete successfully.", entity.toString()));
                }

                return null;
            }
        });
    }

    private void checkId(IEntity entity) throws SQLException {
        if (entity.id() == null) {
            throw new SQLException("Invalid entity`s id.");
        }
    }

}
