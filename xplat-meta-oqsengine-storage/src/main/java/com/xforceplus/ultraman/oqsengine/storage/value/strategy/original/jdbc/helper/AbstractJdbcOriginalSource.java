package com.xforceplus.ultraman.oqsengine.storage.value.strategy.original.jdbc.helper;

/**
 * JDBC 读取或者写入源的表示.
 *
 * @author dongbin
 * @version 0.1 2022/3/14 14:31
 * @since 1.8
 */
public abstract class AbstractJdbcOriginalSource {
    /*
    字段在当前的序号,从1开始.
     */
    private int columnNumber;

    public AbstractJdbcOriginalSource(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
