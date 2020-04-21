package com.xforceplus.ultraman.oqsengine.storage.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;

/**
 * 条件组构造器.
 *
 * @param <V> 条件构造结果类型.
 * @author dongbin
 * @version 0.1 2020/2/22 16:55
 * @since 1.8
 */
public interface ConditionsBuilder<V> {

    /**
     * 构造出条件的字符串表示.
     * @param conditions 条件.
     * @return 构造结果.
     */
    V build(Conditions conditions);

}
