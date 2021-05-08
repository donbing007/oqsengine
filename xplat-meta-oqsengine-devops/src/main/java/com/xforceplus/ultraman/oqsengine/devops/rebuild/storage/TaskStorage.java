package com.xforceplus.ultraman.oqsengine.devops.rebuild.storage;

import com.xforceplus.ultraman.oqsengine.devops.rebuild.enums.BatchStatus;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import io.vavr.control.Either;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * 任务储存.
 *
 * @author xujia 2020/8/24
 * @since 1.8
 */
public interface TaskStorage {
    /*
        任务生成
     */
    Either<SQLException, Integer> build(DevOpsTaskInfo taskInfo);

    /*
       任务信息更新
     */
    int update(DevOpsTaskInfo taskInfo, BatchStatus status) throws SQLException;

    /*
       任务完成
     */
    int done(long taskId) throws SQLException;

    /*
       设置为任务取消
     */
    int cancel(long taskId) throws SQLException;

    /*
       设置为任务异常
     */
    int error(DevOpsTaskInfo taskInfo) throws SQLException;

    /*
        任务失败重试, 从最后一次保存点开始
     */
    Either<SQLException, Integer> resumeTask(DevOpsTaskInfo devOpsTaskInfo) throws SQLException;

    /*
        当前活动任务
     */
    Collection<DevOpsTaskInfo> selectActive(long entityClassId) throws SQLException;

    /*
        获取taskId的任务
     */
    Optional<DevOpsTaskInfo> selectUnique(long taskId) throws SQLException;

    /*
        查询当前所有的活动任务
     */
    Collection<DevOpsTaskInfo> listActives(Page page) throws SQLException;

    /*
        查询所有的任务，包括历史任务
     */
    Collection<DevOpsTaskInfo> listAll(Page page) throws SQLException;
}
