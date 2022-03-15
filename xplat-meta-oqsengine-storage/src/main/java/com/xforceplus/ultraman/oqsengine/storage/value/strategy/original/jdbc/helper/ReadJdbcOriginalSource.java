package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper;

import java.sql.ResultSet;

/**
 * 读取静态源.
 *
 * @author dongbin
 * @version 0.1 2022/3/14 14:35
 * @since 1.8
 */
public class ReadJdbcOriginalSource extends AbstractJdbcOriginalSource {

    private ResultSet resultSet;

    public ReadJdbcOriginalSource(int columnNumber, ResultSet resultSet) {
        super(columnNumber);
        this.resultSet = resultSet;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }
}
