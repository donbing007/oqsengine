package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper;

import java.sql.PreparedStatement;

/**
 * 写入的静态源.
 *
 * @author dongbin
 * @version 0.1 2022/3/14 14:36
 * @since 1.8
 */
public class WriteJdbcOriginalSource extends AbstractJdbcOriginalSource {

    private PreparedStatement preparedStatement;

    public WriteJdbcOriginalSource(int columnNumber, PreparedStatement preparedStatement) {
        super(columnNumber);
        this.preparedStatement = preparedStatement;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }
}
