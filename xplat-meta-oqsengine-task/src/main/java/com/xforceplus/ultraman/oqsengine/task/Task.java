package com.xforceplus.ultraman.oqsengine.task;

import java.util.Map;

/**
 * 任务实体.
 *
 * @author dongbin
 *
 * @version 0.1 2021/08/05 15:27
 * @since 1.8
 */
public interface Task {

    /**
     * 任务唯一标识.
     *
     * @return 任务标识.
     */
    public String id();

    /**
     * 任务创建时间.
     *
     * @return 创建时间.
     */
    public long createTime();

    /**
     * 可执行此任务的Runner类型.
     *
     * @return 可运行此任务的类型信息.
     */
    public Class runnerType();

    /**
     * 任务参数.
     *
     * @return 任务参数.
     */
    public Map<String, String> parameter();
}
