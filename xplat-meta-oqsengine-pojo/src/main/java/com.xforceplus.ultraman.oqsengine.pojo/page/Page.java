package com.xforceplus.ultraman.oqsengine.pojo.page;

import com.xforceplus.ultraman.oqsengine.pojo.conditions.Order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class Page<T> implements Serializable {
    /**
     * 查询数据列表
     */
    private List<T> rows = Collections.emptyList();

    /**
     * 统计数据
     */
    private Summary summary = new Summary();

    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;

    /**
     * 当前页
     */
    private long current = 1;

    /**
     * 排序字段信息
     */
    private Order order;
}