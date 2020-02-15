package com.xforceplus.ultraman.oqsengine.core.conditions.interfaces;

import com.xforceplus.ultraman.oqsengine.core.enums.Link;

import java.io.Serializable;

/**
 * 条件集合封装
 *
 * @author wangzheng
 * @since 2020-02-14
 */
public interface IConditions extends Serializable {
    /**
     * 增加一个新的条件.
     * 例如: 已有条件 link 新的条件.
     *
     * @param link 新条件和已有条件的连接.
     * @param condition 表示一个独立的条件.
     */
    public void addCondition(Link link, ICondition condition);

    /**
     * 增加一系列条件,新的系列条件将和已有条件分别计算.
     * 例如: (已有条件) link (新的条件)
     *
     * @param link 新条件和已有条件的连接.
     * @param conditions 一系列新的条件.
     */
    public void addConditions(Link link, IConditions conditions);

}
