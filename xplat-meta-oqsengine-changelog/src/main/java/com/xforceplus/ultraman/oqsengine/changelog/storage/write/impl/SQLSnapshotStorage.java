package com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.SnapshotStorage;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import io.vavr.control.Either;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Optional;

/**
 * sql implemention
 */
public class SQLSnapshotStorage implements SnapshotStorage {

    @Resource(name = "changelogDataSource")
    private DataSource changelogDatasource;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource
    private ObjectMapper mapper;

    private String tableName = "snapshot";

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Either<SQLException, Integer> saveSnapshot(ChangeSnapshot changeSnapshot) {
        try {
            int result = new SnapshotStorageCommand(tableName, mapper)
                    .saveSnapshot(changelogDatasource, idGenerator, changeSnapshot);
            return Either.right(result);
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public Optional<ChangeSnapshot> query(long objId, long version) {
        try {
            return Optional.of(new SnapshotStorageCommand(tableName, mapper)
                    .querySnapshot(changelogDatasource, objId, version));
        } catch (SQLException ex) {
            return Optional.empty();
        }
    }
}
