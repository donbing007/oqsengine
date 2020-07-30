package com.xforceplus.ultraman.oqsengine.sdk.vo;

import java.util.List;

/**
 * @param <T>
 */
public class DataCollection<T> {

    private Integer rowNum;

    private List<T> rows;

    public DataCollection(Integer rowNum, List<T> rows) {
        this.rowNum = rowNum;
        this.rows = rows;
    }

    public Integer getRowNum() {
        return rowNum;
    }

    public void setRowNum(Integer rowNum) {
        this.rowNum = rowNum;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }
}
