package com.xforceplus.ultraman.oqsengine.common.timerwheel;

import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import java.util.Date;

/**
 * 时间轮接口.
 *
 * @param <T> 管理的元素类型.
 * @author weikai
 * @version 1.0 2021/5/21 15:50
 * @since 1.5
 */
public interface ITimerWheel<T> extends Lifecycle {

    /**
     * 添加任务.
     *
     * @param target    目标.
     * @param timeoutMs 超时时间
     */
    void add(T target, long timeoutMs);

    /**
     * 增加新任务.
     *
     * @param target     目标.
     * @param expireDate 超时时间.
     */
    void add(T target, Date expireDate);

    /**
     * 判断是否存在指定对象.
     *
     * @param target 要检查的目标对象.
     * @return true存在, false不存在.
     */
    boolean exist(T target);

    /**
     * 当前持续中的目标数量.
     *
     * @return 数量.
     */
    int size();

    /**
     * 删除任务.
     *
     * @param target 目标.
     */
    void remove(T target);
}
