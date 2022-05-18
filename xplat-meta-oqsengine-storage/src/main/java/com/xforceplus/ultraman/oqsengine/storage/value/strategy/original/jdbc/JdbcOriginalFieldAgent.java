package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.OriginalFieldAgent;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.ReadJdbcOriginalSource;
import com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper.WriteJdbcOriginalSource;
import java.sql.Types;

/**
 * 基于JDBC的原生类型代理.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 10:57
 * @since 1.8
 * @see Types
 */
public interface JdbcOriginalFieldAgent extends OriginalFieldAgent<ReadJdbcOriginalSource, WriteJdbcOriginalSource> {

    /**
     * 支持的原生JDBC类型.
     *
     * @return JDBC类型.Types的定义值.
     * @see Types
     */
    int supportJdbcType();
}
