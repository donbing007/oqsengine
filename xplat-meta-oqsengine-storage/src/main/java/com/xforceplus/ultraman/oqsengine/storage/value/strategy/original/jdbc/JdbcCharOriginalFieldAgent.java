package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc;

import java.sql.Types;

/**
 * Types.CHAR 支持.
 *
 * @author dongbin
 * @version 0.1 2022/3/9 14:21
 * @since 1.8
 */
public class JdbcCharOriginalFieldAgent extends JdbcVarcharOriginalFieldAgent {

    @Override
    public int supportJdbcType() {
        return Types.CHAR;
    }
}
