package com.xforceplus.ultraman.oqsengine.devops.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.devops.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.task.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.task.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.task.sql.SQL;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.OffsetSnapShot;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.devops.constant.ConstantDefine.NULL_UPDATE;


/**
 * desc :
 * name : TaskStorageCommand
 *
 * @author : xujia
 * date : 2020/8/24
 * @since : 1.8
 */
public class TaskStorageCommand {

    final Logger logger = LoggerFactory.getLogger(TaskStorageCommand.class);

    private String tableName;

    public TaskStorageCommand(String tableName) {
        this.tableName = tableName;
    }

    public Collection<IDevOpsTaskInfo> selectActive(DataSource dataSource, long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return selectActive(connection, id);
        }
    }

    public Optional<IDevOpsTaskInfo> selectByUnique(DataSource dataSource, long taskId) throws SQLException {
        String sql = String.format(SQL.SELECT_SQL_TASK_ID, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, taskId); // task_id

            return executeResult(st);
        }
    }

    public int resumeTask(DataSource dataSource, long taskId) throws SQLException {
        //  "update %s set updatetime = ?, status = ?, message = ? " +
        //            "where maintainid = ? and status not in (0, 1, 2)";
        String sql = String.format(SQL.RESUME_SQL, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            //  batchUpdateTime
            st.setLong(1, System.currentTimeMillis());
            //  Status
            st.setInt(2, BatchStatus.RUNNING.getCode());
            //  message
            st.setString(3, "task recovering");
            //  taskId
            st.setLong(4, taskId);

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    public Collection<IDevOpsTaskInfo> listActives(DataSource dataSource, Page page) throws SQLException {
        String selectSql = String.format(SQL.LIST_ACTIVES, tableName);
        String countSql = String.format(SQL.COUNT_ACTIVES, tableName);
        return lists(dataSource, selectSql, countSql, page);
    }

    public Collection<IDevOpsTaskInfo> listAll(DataSource dataSource, Page page) throws SQLException {
        String selectSql = String.format(SQL.LIST_ALL, tableName);
        String countSql = String.format(SQL.COUNT_ALL, tableName);

        return lists(dataSource, selectSql, countSql, page);
    }

    public Collection<IDevOpsTaskInfo> lists(DataSource dataSource, String selectSql, String countSql, Page page) throws SQLException {
        // 空页,空结果返回.
        if (page.isEmptyPage()) {
            return Collections.emptyList();
        }

        page.setTotalCount(Long.MAX_VALUE);
        PageScope scope = page.getNextPage();

        // 超出页数
        if (scope == null) {
            return Collections.emptyList();
        }

        if (!page.isSinglePage()) {
            page.setTotalCount(counts(dataSource, countSql));
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(selectSql)) {
            st.setLong(1, scope.getStartLine());                        //  startIndex
            st.setLong(2, page.getPageSize());                          //  pageSize

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return executeListResults(st);
        }
    }

    public int build(DataSource dataSource, IDevOpsTaskInfo taskInfo) throws SQLException {
        String sql = String.format(SQL.BUILD_SQL, tableName);

        Connection connection = dataSource.getConnection();
        try {
            return build(connection, sql, taskInfo);
        } catch (SQLException e) {
            throw e;
        } finally {
            connection.close();
        }
    }

    private Collection<IDevOpsTaskInfo> selectActive(Connection connection, long id) throws SQLException {
        String sql = String.format(SQL.SELECT_SQL_ACTIVE, tableName);
        try(PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, id); // id

            return executeListResults(st);
        }
    }

    private int build(Connection connection, String sql, IDevOpsTaskInfo devTask) throws SQLException {
        DevOpsTaskInfo taskInfo = (DevOpsTaskInfo) devTask;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            // taskId
            st.setLong(1, taskInfo.getMaintainid());
            // entityClassId
            st.setLong(2, taskInfo.getEntity());
            // startTime
            st.setLong(3, taskInfo.getStarts());
            // endTime
            st.setLong(4, taskInfo.getEnds());
            // batchSize
            st.setInt(5, taskInfo.getBatchSize());
            // finishSize
            st.setInt(6, taskInfo.getFinishSize());
            // batchStatus
            st.setInt(7, taskInfo.getStatus());
            // batchCreateTime
            st.setLong(8, System.currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    public int update(DataSource dataSource, DevOpsTaskInfo taskInfo, BatchStatus status) throws SQLException {
        //  "update %s set updatetime = ?, finishsize = ?, status = ?, message = ?, checkpoint = ? " +
        //            "where maintainid = ? and status not in (2, 3, 4)";
        String sql = String.format(SQL.UPDATE_SQL, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            //  batchUpdateTime
            st.setLong(1, System.currentTimeMillis());
            //  finishSize
            st.setInt(2, taskInfo.getFinishSize());
            //  Status
            st.setInt(3, status.getCode());
            //  message
            st.setString(4, taskInfo.message());
            //  checkpoint
            st.setString(5, JSON.toJSONString(taskInfo.getOffsetSnapShot()));
            //  taskId
            st.setLong(6, taskInfo.getMaintainid());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    public int error(DataSource dataSource, DevOpsTaskInfo taskInfo) throws SQLException {
        //  "update %s set updatetime = ?, finishsize = ?, status = ?, message = ?, checkpoint = ? " +
        //            "where maintainid = ? and status != 2";
        String sql = String.format(SQL.ERROR_SQL, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            //  batchUpdateTime
            st.setLong(1, System.currentTimeMillis());
            //  finishSize
            st.setInt(2, taskInfo.getFinishSize());
            //  Status
            st.setInt(3, taskInfo.getStatus());
            //  message
            st.setString(4, taskInfo.message());
            //  checkpoint
            st.setString(5, JSON.toJSONString(taskInfo.getOffsetSnapShot()));
            //  taskId
            st.setLong(6, taskInfo.getMaintainid());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    public int status(DataSource dataSource, long taskId, BatchStatus batchStatus, String message) throws SQLException {
        //  "update %s set updatetime = ?, status = ?, message = ? where maintainid = ? and status not in (2, 3, 4)";
        String sql = String.format(SQL.STATUS_SQL, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            //  batchUpdateTime
            st.setLong(1, System.currentTimeMillis());
            //  Status
            st.setInt(2, batchStatus.getCode());
            //  message
            st.setString(3, message);
            //  taskId
            st.setLong(4, taskId);

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    private long counts(DataSource dataSource, String sql) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
                throw new SQLException("count result empty.");
            }
        }
    }

    private Optional<IDevOpsTaskInfo> executeResult(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            if (rs.next()) {
                return Optional.of(fillByResultSet(rs, true));
            }
            return Optional.empty();
        }
    }

    private Collection<IDevOpsTaskInfo> executeListResults(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            Collection<IDevOpsTaskInfo> taskInfoList = new ArrayList<>();
            while (rs.next()) {
                taskInfoList.add(fillByResultSet(rs, false));
            }
            return taskInfoList;
        }
    }

    private IDevOpsTaskInfo fillByResultSet(ResultSet rs, boolean withOffset) throws SQLException {

        DevOpsTaskInfo taskInfo = new DevOpsTaskInfo(
                rs.getLong("maintainid"),
                rs.getLong("entity"),
                rs.getLong("starts"),
                rs.getLong("ends"),
                rs.getInt("batchsize"),
                rs.getInt("finishsize"),
                rs.getInt("status"),
                rs.getLong("createtime"),
                rs.getLong("updatetime"));

        taskInfo.resetMessage(rs.getString("message"));


        if (withOffset) {
            String offset = rs.getString("checkpoint");
            if (null != offset) {
                taskInfo.setOffsetSnapShot(JSONObject.parseObject(offset, OffsetSnapShot.class));
            }
        }

        return taskInfo;
    }

}

