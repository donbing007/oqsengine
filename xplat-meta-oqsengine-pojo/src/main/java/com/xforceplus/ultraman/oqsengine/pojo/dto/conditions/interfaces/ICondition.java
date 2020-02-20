package com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.interfaces;

import java.io.Serializable;
import java.util.Collection;

/**
 * 条件封装
 *
 * @author wangzheng
 * @since 2020-02-14
 */
public interface ICondition<Children, R> extends Serializable {

    /**
     * 等于 =
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children eq(R column, Object val);

    /**
     * 不等于 &lt;&gt;
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children ne(R column, Object val);

    /**
     * 大于 &gt;
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children gt(R column, Object val);

    /**
     * 大于等于 &gt;=
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children ge(R column, Object val);

    /**
     * 小于 &lt;
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children lt(R column, Object val);

    /**
     * 小于等于 &lt;=
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children le(R column, Object val);

    /**
     * BETWEEN 值1 AND 值2
     *
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return children
     */
    Children between(R column, Object val1, Object val2);

    /**
     * NOT BETWEEN 值1 AND 值2
     *
     * @param column    字段
     * @param val1      值1
     * @param val2      值2
     * @return children
     */
    Children notBetween(R column, Object val1, Object val2);

    /**
     * LIKE '%值%'
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children like(R column, Object val);

    /**
     * NOT LIKE '%值%'
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children notLike(R column, Object val);

    /**
     * LIKE '%值'
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children likeLeft(R column, Object val);

    /**
     * LIKE '值%'
     *
     * @param column    字段
     * @param val       值
     * @return children
     */
    Children likeRight(R column, Object val);

    /**
     * 字段 IN (value.get(0), value.get(1), ...)
     * <p>例: in("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * <li> 如果集合为 empty 则不会进行 sql 拼接 </li>
     *
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children in(R column, Collection<?> coll);

    /**
     * 字段 NOT IN (value.get(0), value.get(1), ...)
     * <p>例: notIn("id", Arrays.asList(1, 2, 3, 4, 5))</p>
     *
     * @param column    字段
     * @param coll      数据集合
     * @return children
     */
    Children notIn(R column, Collection<?> coll);

    /**
     * 分组：GROUP BY 字段, ...
     * <p>例: groupBy("id", "name")</p>
     *
     * @param columns   字段数组
     * @return children
     */
    Children groupBy(R... columns);

    /**
     * 排序：ORDER BY 字段, ...
     * <p>例: orderBy(true, "id", "name")</p>
     *
     * @param isAsc     是否是 ASC 排序
     * @param columns   字段数组
     * @return children
     */
    Children orderBy(boolean isAsc, R... columns);

}
