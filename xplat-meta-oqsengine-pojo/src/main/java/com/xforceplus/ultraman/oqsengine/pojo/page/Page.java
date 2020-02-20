package com.xforceplus.ultraman.oqsengine.pojo.page;


import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces.IConditions;

import java.io.Serializable;
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
     * 查询条件信息
     */
    private IConditions conditions;

    public Page() {
    }

    public Page(List<T> rows, Summary summary, long size, long current, IConditions conditions) {
        this.rows = rows;
        this.summary = summary;
        this.size = size;
        this.current = current;
        this.conditions = conditions;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public IConditions getConditions() {
        return conditions;
    }

    public void setConditions(IConditions conditions) {
        this.conditions = conditions;
    }

}