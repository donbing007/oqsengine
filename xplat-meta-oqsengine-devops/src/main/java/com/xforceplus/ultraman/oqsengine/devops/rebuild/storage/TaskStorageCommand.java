package com.xforceplus.ultraman.oqsengine.devops.rebuild.storage;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DefaultDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.sql.SQL;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.pojo.page.PageScope;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 任务储存命令.
 *
 * @author xujia 2020/8/24
 * @since 1.8
 */
public class TaskStorageCommand {

    final Logger logger = LoggerFactory.getLogger(TaskStorageCommand.class);

    private String tableName;

    public TaskStorageCommand(String tableName) {
        this.tableName = tableName;
    }

    /**
     * 查找活动任务.
     */
    public Collection<DevOpsTaskInfo> selectActive(DataSource dataSource, long id) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return doSelectActive(connection, id);
        }
    }

    /**
     * 查询单独任务.
     */
    public Optional<DevOpsTaskInfo> selectByUnique(DataSource dataSource, long taskId) throws SQLException {
        String sql = String.format(SQL.SELECT_SQL_TASK_ID, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, taskId); // task_id

            return executeResult(st);
        }
    }

    /**
     * 恢复任务.
     */
    public int resumeTask(DataSource dataSource, long maintainid) throws SQLException {
        String sql = String.format(SQL.RESUME_SQL, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            //  batchUpdateTime
            st.setLong(1, System.currentTimeMillis());
            //  Status
            st.setInt(2, BatchStatus.RUNNING.getCode());
            //  message
            st.setString(3, "TASK RECOVERING");
            //  taskId
            st.setLong(4, maintainid);

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    /**
     * 列出活动任务.
     */
    public Collection<DevOpsTaskInfo> listActives(DataSource dataSource, Page page) throws SQLException {
        String selectSql = String.format(SQL.LIST_ACTIVES, tableName);
        String countSql = String.format(SQL.COUNT_ACTIVES, tableName);
        return lists(dataSource, selectSql, countSql, page);
    }

    /**
     * 列出所有任务.
     */
    public Collection<DevOpsTaskInfo> listAll(DataSource dataSource, Page page) throws SQLException {
        String selectSql = String.format(SQL.LIST_ALL, tableName);
        String countSql = String.format(SQL.COUNT_ALL, tableName);

        return lists(dataSource, selectSql, countSql, page);
    }

    /**
     * 列出任务.
     */
    public Collection<DevOpsTaskInfo> lists(DataSource dataSource, String selectSql, String countSql, Page page)
        throws SQLException {
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

    /**
     * 构造任务执行.
     */
    public int build(DataSource dataSource, DevOpsTaskInfo taskInfo) throws SQLException {
        String sql = String.format(SQL.BUILD_SQL, tableName);

        Connection connection = dataSource.getConnection();
        try {
            return doBuild(connection, sql, taskInfo);
        } catch (SQLException e) {
            throw e;
        } finally {
            connection.close();
        }
    }

    private Collection<DevOpsTaskInfo> doSelectActive(Connection connection, long id) throws SQLException {
        String sql = String.format(SQL.SELECT_SQL_ACTIVE, tableName);
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setLong(1, id); // id

            return executeListResults(st);
        }
    }

    private int doBuild(Connection connection, String sql, DevOpsTaskInfo devTask) throws SQLException {
        DefaultDevOpsTaskInfo taskInfo = (DefaultDevOpsTaskInfo) devTask;
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
            st.setLong(5, taskInfo.getBatchSize());
            // finishSize
            st.setInt(6, taskInfo.getFinishSize());
            // batchStatus
            st.setInt(7, taskInfo.getStatus());
            // batchCreateTime
            st.setLong(8, System.currentTimeMillis());
            // startid
            st.setLong(9, taskInfo.getErrorSize());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    /**
     * 更新任务.
     */
    public int update(DataSource dataSource, DefaultDevOpsTaskInfo taskInfo) throws SQLException {
        String sql = updateSql(taskInfo);

        try (Connection connection = dataSource.getConnection();
            PreparedStatement st = connection.prepareStatement(sql)) {

            int pos = 1;

            //  batchUpdateTime
            st.setLong(pos++, System.currentTimeMillis());

            //  Status
            st.setInt(pos++, taskInfo.getStatus());

            //  batchSize
            if (taskInfo.getBatchSize() > 0) {
                st.setInt(pos++, (int) taskInfo.getBatchSize());
            }

            //  finishSize increment
            if (taskInfo.incrementSize() > 0) {
                st.setInt(pos++, taskInfo.incrementSize());
            }

            //  message
            if (null != taskInfo.message()) {
                st.setString(pos++, taskInfo.message());
            }

            //  errorSize
            if (taskInfo.getErrorSize() > 0) {
                st.setLong(pos++, taskInfo.getErrorSize());
            }

            //  taskId
            st.setLong(pos, taskInfo.getMaintainid());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    private String updateSql(DefaultDevOpsTaskInfo taskInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("update ").append(tableName).append(" set updatetime = ?, status = ?");
        if (taskInfo.getBatchSize() > 0) {
            stringBuilder.append(", ").append("batchsize = ?");
        }
        if (taskInfo.incrementSize() > 0) {
            stringBuilder.append(", ").append("finishsize = finishsize + ?");
        }
        if (null != taskInfo.message()) {
            stringBuilder.append(", ").append("message = ?");
        }

        //  由于改变方式为cdc同步、所以该字段记录为错误数量.
        if (taskInfo.getErrorSize() > 0) {
            stringBuilder.append(", ").append("startid = ?");
        }

        stringBuilder.append(" ").append("where maintainid = ? and status not in (2, 3, 4)");

        return stringBuilder.toString();
    }


    /**
     * 任务错误结束.
     */
    public int error(DataSource dataSource, DefaultDevOpsTaskInfo taskInfo) throws SQLException {
        String sql = String.format(SQL.ERROR_SQL, tableName);

        try (Connection connection = dataSource.getConnection();
             PreparedStatement st = connection.prepareStatement(sql)) {
            //  batchUpdateTime
            st.setLong(1, System.currentTimeMillis());
            //  finishSize
            st.setInt(2, taskInfo.getFinishSize());
            //  status
            st.setInt(3, taskInfo.getStatus());
            //  message
            st.setString(4, taskInfo.message());
            //  startId
            st.setLong(5, taskInfo.getErrorSize());
            //  taskId
            st.setLong(6, taskInfo.getMaintainid());

            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            return st.executeUpdate();
        }
    }

    /**
     * 设置任务状态.
     */
    public int status(DataSource dataSource, long taskId, BatchStatus batchStatus, String message) throws SQLException {
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

    private Optional<DevOpsTaskInfo> executeResult(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            if (rs.next()) {
                return Optional.of(fillByResultSet(rs));
            }
            return Optional.empty();
        }
    }

    private Collection<DevOpsTaskInfo> executeListResults(PreparedStatement st) throws SQLException {
        try (ResultSet rs = st.executeQuery()) {
            if (logger.isDebugEnabled()) {
                logger.debug(st.toString());
            }

            Collection<DevOpsTaskInfo> taskInfoList = new ArrayList<>();
            while (rs.next()) {
                taskInfoList.add(fillByResultSet(rs));
            }
            return taskInfoList;
        }
    }

    private DevOpsTaskInfo fillByResultSet(ResultSet rs) throws SQLException {

        DefaultDevOpsTaskInfo taskInfo = new DefaultDevOpsTaskInfo(
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
        taskInfo.setErrorSize(rs.getLong("startid"));

        return taskInfo;
    }

}

