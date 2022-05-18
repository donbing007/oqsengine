package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import java.sql.Types;

/**
 * Types.TINYINT 支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 11:37
 * @since 1.8
 */
public class JdbcTinyintOriginalFieldAgent extends JdbcIntegerOriginalFieldAgent {

    @Override
    public int supportJdbcType() {
        return Types.TINYINT;
    }
}
