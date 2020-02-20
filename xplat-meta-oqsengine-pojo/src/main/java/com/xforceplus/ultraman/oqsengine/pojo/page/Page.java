package com.xforceplus.ultraman.oqsengine.pojo.page;


import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces.IConditions;
import com.xforceplus.ultraman.oqsengine.pojo.page.interfaces.IPage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * 分页对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class Page<T> implements IPage<T> {

    private static final long serialVersionUID = 8545996863226528798L;

    /**
     * 查询数据列表
     */
    private List<T> rows = Collections.emptyList();
    /**
     * 統計數據
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
     * 自动优化 COUNT SQL
     */
    private boolean optimizeCountSql = true;

    /**
     * 是否进行 count 查询
     */
    private boolean isSearchCount = true;

    /**
     * 查询条件信息
     */
    private IConditions conditions;

    public Page() {
    }

    /**
     * 分页构造函数
     *
     * @param current 当前页
     * @param size    每页显示条数
     */
    public Page(long current, long size) {
        this(current, size, 0);
    }

    public Page(long current, long size, long total) {
        this(current, size, total, true);
    }

    public Page(long current, long size, boolean isSearchCount) {
        this(current, size, 0, isSearchCount);
    }

    public Page(long current, long size, long total, boolean isSearchCount) {
        if (current > 1) {
            this.current = current;
        }
        this.size = size;
        this.summary.setTotal(total);
        this.isSearchCount = isSearchCount;
    }

    /**
     * 是否存在上一页
     *
     * @return true / false
     */
    public boolean hasPrevious() {
        return this.current > 1;
    }

    /**
     * 是否存在下一页
     *
     * @return true / false
     */
    public boolean hasNext() {
        return this.current < this.getPages();
    }

    @Override
    public List<T> getRows() {
        return this.rows;
    }

    @Override
    public Page<T> setRows(List<T> rows) {
        this.rows = rows;
        return this;
    }

    @Override
    public long getTotal() {
        return this.summary.getTotal();
    }

    @Override
    public Page<T> setTotal(long total) {
        this.summary.setTotal(total);
        return this;
    }

    @Override
    public Summary getSummary() {

        return this.summary;
    }

    @Override
    public Page<T> setSummary(Summary summary) {
        this.summary = summary;
        return this;
    }

    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public Page<T> setSize(long size) {
        this.size = size;
        return this;
    }

    @Override
    public long getCurrent() {
        return this.current;
    }

    @Override
    public Page<T> setCurrent(long current) {
        this.current = current;
        return this;
    }



    @Override
    public boolean isSearchCount() {
        if (summary.getTotal() < 0) {
            return false;
        }
        return isSearchCount;
    }

    @Override
    public IConditions getConditions() {
        return this.conditions;
    }

    @Override
    public IPage<T> setConditions(IConditions conditions) {
        this.conditions = conditions;
        return this;
    }

    public Page<T> setSearchCount(boolean isSearchCount) {
        this.isSearchCount = isSearchCount;
        return this;
    }

    public Page<T> setOptimizeCountSql(boolean optimizeCountSql) {
        this.optimizeCountSql = optimizeCountSql;
        return this;
    }
    /**
     * IPage 的泛型转换
     *
     * @param mapper 转换函数
     * @param <R>    转换后的泛型
     * @return 转换泛型后的 IPage
     */
    @SuppressWarnings("unchecked")
    public <R> IPage<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> collect = this.getRows().stream().map(mapper).collect(toList());
        return ((IPage<R>) this).setRows(collect);
    }

}