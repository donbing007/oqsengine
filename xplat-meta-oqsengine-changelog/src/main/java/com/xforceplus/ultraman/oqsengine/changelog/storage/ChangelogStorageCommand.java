package com.xforceplus.ultraman.oqsengine.changelog.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.sql.SQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 */
public class ChangelogStorageCommand {

    final Logger logger = LoggerFactory.getLogger(ChangelogStorageCommand.class);

    final ObjectMapper mapper;

    private String tableName;

    public ChangelogStorageCommand(String tableName, ObjectMapper mapper) {
        this.tableName = tableName;
        this.mapper = mapper;
    }

    /**
     * TODO snapshot
     * @param dataSource
     * @param id
     * @return
     * @throws SQLException
     */
    public List<Changelog> findChangelogById(DataSource dataSource, long id, long reqVersion) throws SQLException {

        String sql;
        if(reqVersion < 0){
            sql = String.format(SQL.FIND_SQL, tableName, id);
        } else {
            sql = String.format(SQL.FIND_SQL_VERSION, tableName, id, reqVersion);
        }

        Connection connection = dataSource.getConnection();
        List<Changelog> retList = new LinkedList<>();
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = st.executeQuery()){
                while(resultSet.next()){
                    long cid = resultSet.getLong("cid");
                    long recordId = resultSet.getLong("id");
                    String comment = resultSet.getString("comment");
                    long createTime = resultSet.getLong("create_time");
                    String json = resultSet.getString("changes");
                    long entity = resultSet.getLong("entity");
                    long version = resultSet.getLong("version");

                    Changelog changelog = new Changelog();
                    changelog.setcId(cid);
                    changelog.setId(recordId);
                    changelog.setComment(comment);
                    changelog.setCreateTime(createTime);
                    changelog.setVersion(version);
                    changelog.setEntityClass(entity);
                    List<ChangeValue> changeValues = Collections.emptyList();
                    try {
                        changeValues = mapper.readValue(json, new TypeReference<List<ChangeValue>>() {});
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    changelog.setChangeValues(changeValues);
                    retList.add(changelog);
                }
            }
        }

        return retList;
    }

    /**
     * save changelog and changeValue
     * reference changeValue will store in another table
     * @param dataSource
     * @return
     * @throws SQLException
     */
    public int saveChangelog(DataSource dataSource, List<Changelog> changelogList) throws SQLException{

        String sql = String.format(SQL.SAVE_SQL, tableName);

        Connection connection = dataSource.getConnection();
        try {
            changelogList.forEach(x -> {
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

    private int save(Connection connection, String sql, Changelog changelog) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {

            st.setLong(1, changelog.getcId());

            st.setLong(2, changelog.getId());
            // entityClassId
            st.setLong(3, changelog.getEntityClass());
            // startTime
            st.setString(4, changelog.getComment());
            // endTime
            st.setString(5, mapper.writeValueAsString(changelog.getChangeValues()));
            // batchSize
            st.setLong(6, changelog.getVersion());
            // finishSize
            st.setLong(7, changelog.getCreateTime());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return -1;
        }
    }
}

