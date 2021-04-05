package com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeValue;
import com.xforceplus.ultraman.oqsengine.changelog.sql.SQL;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SnapshotStorageCommand {

    final Logger logger = LoggerFactory.getLogger(SnapshotStorageCommand.class);

    final ObjectMapper mapper;

    private String tableName;

    public SnapshotStorageCommand(String tableName, ObjectMapper mapper) {
        this.tableName = tableName;
        this.mapper = mapper;
    }

    public ChangeSnapshot querySnapshot(DataSource dataSource, long objId, long version) throws SQLException {

        String sql;
        if( version < 0){
            sql = String.format(SQL.FIND_SNAPSHOT_SQL, tableName, objId);
        } else {
            sql = String.format(SQL.FIND_SNAPSHOT_SQL_VERSION, tableName, objId, version);
        }
        Connection connection = dataSource.getConnection();
        try {
            return findSnapshot(connection, sql);
        } finally {
            connection.close();
        }
    }

    private ChangeSnapshot findSnapshot(Connection connection, String sql) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = st.executeQuery()) {
                if (resultSet.next()) {
                    long sid = resultSet.getLong("sid");
                    long recordId = resultSet.getLong("id");
                    long createTime = resultSet.getLong("create_time");
                    String json = resultSet.getString("changes");
                    String referenceMapJson = resultSet.getString("reference");
                    long entity = resultSet.getLong("entity");
                    long recordVersion = resultSet.getLong("version");

                    ChangeSnapshot changeSnapshot = new ChangeSnapshot();
                    changeSnapshot.setsId(sid);
                    changeSnapshot.setId(recordId);
                    changeSnapshot.setEntityClass(entity);
                    changeSnapshot.setCreateTime(createTime);
                    changeSnapshot.setVersion(recordVersion);

                    List<ChangeValue> changeValues = Collections.emptyList();
                    Map<Long, List<Long>> referenceMap = Collections.emptyMap();
                    try {
                        changeValues = mapper.readValue(json, new TypeReference<List<ChangeValue>>() {
                        });
                        referenceMap = mapper.readValue(referenceMapJson, new TypeReference<Map<Long, List<Long>>>() {
                        });
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    changeSnapshot.setChangeValues(changeValues);
                    changeSnapshot.setReferenceMap(referenceMap);
                    return changeSnapshot;
                }

                return null;
            }
        }
    }

    public int saveSnapshot(DataSource dataSource, LongIdGenerator longIdGenerator, ChangeSnapshot changeSnapshot) throws SQLException {
        String sql = String.format(SQL.SAVE_SNAPSHOT, tableName);
        Connection connection = dataSource.getConnection();
        try {
            save(connection, sql, longIdGenerator, changeSnapshot);
        } finally {
            connection.close();
        }

        return 1;
    }

    private int save(Connection connection, String sql, LongIdGenerator longIdGenerator, ChangeSnapshot changeSnapshot) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, longIdGenerator.next());

            st.setLong(2, changeSnapshot.getId());
            // entityClassId
            st.setLong(3, changeSnapshot.getEntityClass());
            // startTime
            st.setString(4, mapper.writeValueAsString(changeSnapshot.getChangeValues()));

            st.setString(5, mapper.writeValueAsString(changeSnapshot.getReferenceMap()));
            // batchSize
            st.setLong(6, changeSnapshot.getVersion());
            // finishSize
            st.setLong(7, changeSnapshot.getCreateTime());
        } catch (JsonProcessingException e) {
            logger.error("{}", e);
            return -1;
        }

        return 1;
    }
}
