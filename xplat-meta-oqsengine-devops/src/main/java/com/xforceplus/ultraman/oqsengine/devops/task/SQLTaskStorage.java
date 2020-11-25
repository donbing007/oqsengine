package com.xforceplus.ultraman.oqsengine.devops.task;

import com.xforceplus.ultraman.oqsengine.devops.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.task.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.task.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import io.vavr.control.Either;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.devops.constant.ConstantDefine.EMPTY_COLLECTION_SIZE;
import static com.xforceplus.ultraman.oqsengine.devops.enums.ERROR.DUPLICATE_KEY_ERROR;


/**
 * desc :
 * name : BatchSqlMaster
 *
 * @author : xujia
 * date : 2020/8/24
 * @since : 1.8
 */
public class SQLTaskStorage implements TaskStorage {

    @Resource(name = "devOpsDataSource")
    private DataSource devOpsDataSource;

    private String table = "devopstasks";

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    @Override
    public Either<SQLException, Integer> build(IDevOpsTaskInfo taskInfo) {
        try {
            Collection<IDevOpsTaskInfo> collection = selectActive(((DevOpsTaskInfo)taskInfo).getEntity());
            if (EMPTY_COLLECTION_SIZE == collection.size()) {
                int result = new TaskStorageCommand(table).build(devOpsDataSource, taskInfo);
                return Either.right(result);
            }
            return Either.left(new SQLException("reIndex has already been begun, ignore",
                                        DUPLICATE_KEY_ERROR.name(), DUPLICATE_KEY_ERROR.ordinal()));
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    public int update(IDevOpsTaskInfo taskInfo, BatchStatus status) throws SQLException {
        return new TaskStorageCommand(table).update(devOpsDataSource, ((DevOpsTaskInfo)taskInfo), status);
    }

    @Override
    public int done(long taskId) throws SQLException {
        return new TaskStorageCommand(table).status(devOpsDataSource, taskId, BatchStatus.DONE, "success");
    }

    @Override
    public int cancel(long taskId) throws SQLException {
        return new TaskStorageCommand(table).status(devOpsDataSource, taskId, BatchStatus.CANCEL, "task canceled");
    }

    @Override
    public int error(IDevOpsTaskInfo taskInfo) throws SQLException {
        return new TaskStorageCommand(table).error(devOpsDataSource, ((DevOpsTaskInfo)taskInfo));
    }

    @Override
    public Either<SQLException, Integer> resumeTask(IDevOpsTaskInfo devOpsTaskInfo) {
        try {
            Optional<IDevOpsTaskInfo> unique = selectUnique(devOpsTaskInfo.getMaintainid());
            if (unique.isPresent()) {
                int task = new TaskStorageCommand(table).resumeTask(devOpsDataSource, devOpsTaskInfo.getMaintainid());
                return Either.right(task);
            }
            return Either.left(new SQLException(String.format("resume Task failed, no match task found, %d", devOpsTaskInfo.getMaintainid())));
        } catch (SQLException e) {
            return Either.left(e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<IDevOpsTaskInfo> selectActive(long entityClassId) throws SQLException {
        return new TaskStorageCommand(table).selectActive(devOpsDataSource, entityClassId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<IDevOpsTaskInfo> selectUnique(long taskId) throws SQLException {
        return new TaskStorageCommand(table).selectByUnique(devOpsDataSource, taskId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<IDevOpsTaskInfo> listActives(Page page) throws SQLException {
        return new TaskStorageCommand(table).listActives(devOpsDataSource, page);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<IDevOpsTaskInfo> listAll(Page page) throws SQLException {
        return new TaskStorageCommand(table).listAll(devOpsDataSource, page);
    }
}
