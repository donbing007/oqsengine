package com.xforceplus.ultraman.oqsengine.pojo.page.interfaces;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces.IConditions;
import com.xforceplus.ultraman.oqsengine.pojo.page.Summary;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * 分页 Page 对象接口
 *
 * @author wangzheng
 * @since 2020-02-20
 */
public interface IPage<T> extends Serializable {

    /**
     * 进行 count 查询 【 默认: true 】
     *
     * @return true 是 / false 否
     */
    default boolean isSearchCount() {
        return true;
    }

    /**
     * 计算当前分页偏移量
     */
    default long offset() {
        return getCurrent() > 0 ? (getCurrent() - 1) * getSize() : 0;
    }

    /**
     * 当前分页总页数
     */
    default long getPages() {
        if (getSize() == 0) {
            return 0L;
        }
        long pages = getTotal() / getSize();
        if (getTotal() % getSize() != 0) {
            pages++;
        }
        return pages;
    }

    /**
     * 内部什么也不干
     * <p>只是为了 json 反序列化时不报错</p>
     */
    default IPage<T> setPages(long pages) {
        // to do nothing
        return this;
    }

    /**
     * 获取查询条件信息
     */
    IConditions getConditions();

    /**
     * 设置条件信息-order等
     * @param conditions
     * @return
     */
    IPage<T> setConditions(IConditions conditions);

    /**
     * 分页记录列表
     *
     * @return 分页对象记录列表
     */
    List<T> getRows();

    /**
     * 设置分页记录列表
     */
    IPage<T> setRows(List<T> rows);

    /**
     * 当前满足条件总行数
     *
     * @return 总条数
     */
    long getTotal();

    /**
     * 设置当前满足条件总行数
     */
    IPage<T> setTotal(long total);

    /**
     * 用于适配前端的总数需求 - 获取summary
     * @return Summary
     */
    Summary getSummary();

    /**
     * 设置总数
     */
    IPage<T> setSummary(Summary summary);

    /**
     * 获取每页显示条数
     *
     * @return 每页显示条数
     */
    long getSize();

    /**
     * 设置每页显示条数
     */
    IPage<T> setSize(long size);

    /**
     * 当前页，默认 1
     *
     * @return 当前页
     */
    long getCurrent();

    /**
     * 设置当前页
     */
    IPage<T> setCurrent(long current);

    /**
     * IPage 的泛型转换
     *
     * @param mapper 转换函数
     * @param <R>    转换后的泛型
     * @return 转换泛型后的 IPage
     */
    @SuppressWarnings("unchecked")
    default <R> IPage<R> convert(Function<? super T, ? extends R> mapper) {
        List<R> collect = this.getRows().stream().map(mapper).collect(toList());
        return ((IPage<R>) this).setRows(collect);
    }
}
