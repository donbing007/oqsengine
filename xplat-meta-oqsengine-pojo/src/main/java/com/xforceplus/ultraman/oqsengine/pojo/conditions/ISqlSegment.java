package com.xforceplus.ultraman.oqsengine.pojo.conditions;

import java.io.Serializable;

/**
 * SQL 片段接口
 *
 * @author wangzheng
 * @since 2020-02-14
 */
@FunctionalInterface
public interface ISqlSegment extends Serializable {

    /**
     * SQL 片段
     */
    String getSqlSegment();
}