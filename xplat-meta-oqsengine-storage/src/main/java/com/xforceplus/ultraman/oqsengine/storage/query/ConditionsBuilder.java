package com.xforceplus.ultraman.oqsengine.storage.query;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * 条件组构造器.
 *
 * @param <R> 请求条件.
 * @param <T> 条件构造结果类型.
 * @author dongbin
 * @version 0.1 2020/2/22 16:55
 * @since 1.8
 */
public interface ConditionsBuilder<R, T> {

    /**
     * 查询构造器.
     *
     * @param conditions    条件.
     * @param entityClasses 多个元信息.
     * @return V 构造结果.
     * @author dongbin
     */
    T build(R conditions, IEntityClass... entityClasses);

}
