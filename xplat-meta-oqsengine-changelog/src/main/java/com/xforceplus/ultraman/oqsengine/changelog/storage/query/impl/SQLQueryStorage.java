package com.xforceplus.ultraman.oqsengine.changelog.storage.query.impl;

import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeVersion;
import com.xforceplus.ultraman.oqsengine.changelog.sql.SQL;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.ChangeVersionTable;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * sql query service
 */
public class SQLQueryStorage implements QueryStorage {

    final Logger logger = LoggerFactory.getLogger(SQLQueryStorage.class);

    @Resource(name = "changelogDataSource")
    private DataSource changelogQueryDataSource;

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private LongIdGenerator snowFlakeIdGenerator;

    private String tableName = "changeversion";

    /**
     * @param ids
     * @param isSelf
     * @return
     * @throws SQLException
     */
    @Override
    public Map<Long, Long> changeCountMapping(List<Long> ids, boolean isSelf) throws SQLException {

        Connection connection = changelogQueryDataSource.getConnection();

        Map<Long, Long> retMap = new HashMap<>(ids.size());

        //init map
        ids.forEach(x -> {
            retMap.put(x, 0L);
        });

        String idsStr = ids.stream().map(Object::toString).collect(Collectors.joining(","));
        String sql;
        if(isSelf) {
            sql = String.format(SQL.FIND_GROUPED_VERSION_SELF, tableName, idsStr);
        } else {
            sql = String.format(SQL.FIND_GROUPED_VERSION, tableName, idsStr);
        }

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = st.executeQuery()) {
                while(resultSet.next()) {
                    long id = resultSet.getLong(ChangeVersionTable.ID);
                    long count = resultSet.getLong(ChangeVersionTable.COUNT);
                    retMap.put(id, count);
                }
            }
        }

        return retMap;
    }

    @Override
    public List<ChangeVersion> queryChangelog(long id, boolean isSelf, int page, int size) throws SQLException {
        Connection connection = changelogQueryDataSource.getConnection();

        List<ChangeVersion> changeVersionList = new LinkedList<>();

        /**
         * always start at 1
         */
        if(page < 1){
            page = 1;
        }

        String sql;
        if(isSelf) {
            sql = String.format(SQL.FIND_VERSION_SELF, tableName, id, (page - 1)* size, size);
        } else {
            sql = String.format(SQL.FIND_VERSION, tableName, id, (page - 1)* size, size);
        }

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = st.executeQuery()) {
                while(resultSet.next()){
                    ChangeVersion cv = new ChangeVersion();
                    cv.setTimestamp(resultSet.getLong(ChangeVersionTable.TIMESTAMP));
                    cv.setComment(resultSet.getString(ChangeVersionTable.COMMENT));
                    cv.setUsername(resultSet.getString(ChangeVersionTable.USER));
                    cv.setVersion(resultSet.getLong(ChangeVersionTable.VERSION));
                    cv.setSource(resultSet.getLong(ChangeVersionTable.SOURCE));
                    changeVersionList.add(cv);
                }
            }
        }

        return changeVersionList;
    }

    @Override
    public int saveChangeVersion(long id, List<ChangeVersion> changeVersionList) throws SQLException{

        Connection connection = changelogQueryDataSource.getConnection();
        String sql = String.format(SQL.SAVE_VERSION_SQL, tableName);

        try{
            changeVersionList.forEach(x -> {
                try {
                    save(connection, sql, x);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        } finally {
            connection.close();
        }

        return 1;
    }

    private int save(Connection connection, String sql, ChangeVersion changeVersion) throws SQLException{
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, snowFlakeIdGenerator.next());
            st.setLong(2, changeVersion.getId());
            st.setString(3, changeVersion.getComment());
            st.setLong(4, changeVersion.getVersion());
            st.setLong(5, changeVersion.getTimestamp());
            st.setString(6, changeVersion.getUsername());
            st.setLong(7, changeVersion.getSource());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }
}
