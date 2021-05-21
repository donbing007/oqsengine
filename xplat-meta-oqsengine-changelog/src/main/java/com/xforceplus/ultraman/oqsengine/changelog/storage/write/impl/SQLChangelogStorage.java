package com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * changelog storage
 */
public class SQLChangelogStorage implements ChangelogStorage {

    @Resource(name = "changelogDataSource")
    private DataSource changelogDatasource;

    @Resource(name = "snowflakeIdGenerator")
    private LongIdGenerator idGenerator;

    @Resource
    private ObjectMapper mapper;

    private String table = "changelog";

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    Logger logger = LoggerFactory.getLogger(SQLChangelogStorage.class);

    @Override
    public Either<SQLException, Integer> saveBatch(List<Changelog> changeLogs) {
        try {
            int result = new ChangelogStorageCommand(table, mapper)
                    .saveChangelog(changelogDatasource, idGenerator, changeLogs);
            return Either.right(result);
        } catch (SQLException e) {
            logger.error("{}", e);
            return Either.left(e);
        }
    }

    @Override
    public List<Changelog> findById(long id, long startVersion, long endVersion) {
        try {
            List<Changelog> result = new ChangelogStorageCommand(table, mapper)
                    .findChangelogById(changelogDatasource, id, startVersion, endVersion);
            return result;
        } catch (SQLException e) {
            logger.error("{}", e);
            return Collections.emptyList();
        }
    }
}
