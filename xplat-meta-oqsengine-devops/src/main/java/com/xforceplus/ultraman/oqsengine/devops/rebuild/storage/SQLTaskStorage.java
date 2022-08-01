package com.xforceplus.ultraman.oqsengine.devops.rebuild.storage;

import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.EMPTY_COLLECTION_SIZE;
import static com.xforceplus.ultraman.oqsengine.devops.rebuild.constant.ConstantDefine.NULL_UPDATE;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DefaultDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.Resource;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 任务储存.
 *
 * @author xujia 2020/8/24
 * @since 1.8
 */
public class SQLTaskStorage implements TaskStorage {

    final Logger logger = LoggerFactory.getLogger(SQLTaskStorage.class);

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
    public Integer build(DevOpsTaskInfo taskInfo) throws SQLException {
        Collection<DevOpsTaskInfo> collection = selectActive(taskInfo.getEntity());
        //  每一次重建都会使之前已经开始的相同entityClassIds的任务取消.
        if (EMPTY_COLLECTION_SIZE != collection.size()) {
            for (DevOpsTaskInfo devOpsTaskInfo : collection) {
                cancel(devOpsTaskInfo.getMaintainid());
            }
        }

        return new TaskStorageCommand(table).build(devOpsDataSource, taskInfo);
    }

    @Override
    public int update(DevOpsTaskInfo taskInfo) throws SQLException {
        return new TaskStorageCommand(table).update(devOpsDataSource, ((DefaultDevOpsTaskInfo) taskInfo));
    }

    @Override
    public int done(DevOpsTaskInfo taskInfo) throws SQLException {
        taskInfo.resetStatus(BatchStatus.DONE.getCode());
        return new TaskStorageCommand(table).update(devOpsDataSource, ((DefaultDevOpsTaskInfo) taskInfo));
    }

    @Override
    public int error(DevOpsTaskInfo taskInfo) throws SQLException {
        taskInfo.resetStatus(BatchStatus.ERROR.getCode());
        return new TaskStorageCommand(table).update(devOpsDataSource, ((DefaultDevOpsTaskInfo) taskInfo));
    }

    @Override
    public int cancel(long taskId) throws SQLException {
        return new TaskStorageCommand(table).status(devOpsDataSource, taskId, BatchStatus.CANCEL, "task canceled");
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DevOpsTaskInfo> selectActive(long entityClassId) throws SQLException {
        return new TaskStorageCommand(table).selectActive(devOpsDataSource, entityClassId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<DevOpsTaskInfo> selectUnique(long taskId) throws SQLException {
        return new TaskStorageCommand(table).selectByUnique(devOpsDataSource, taskId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DevOpsTaskInfo> listActives(Page page) throws SQLException {
        return new TaskStorageCommand(table).listActives(devOpsDataSource, page);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<DevOpsTaskInfo> listActivesWithLimits(Page page, long duration) throws SQLException {
        return new TaskStorageCommand(table).listActivesWithLimit(devOpsDataSource, page, duration);
    }


    @Override
    @SuppressWarnings("unchecked")
    public Collection<DevOpsTaskInfo> listAll(Page page) throws SQLException {
        return new TaskStorageCommand(table).listAll(devOpsDataSource, page);
    }
}
