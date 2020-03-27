package com.xforceplus.ultraman.oqsengine.sdk.vo.dto;

import java.util.List;

/**
 * row item
 * @param <T>
 */
public class RowItem<T> implements ResponseItem{

    private List<T> rows;

    private SummaryItem summary;

    public RowItem() {
    }

    public RowItem(List<T> rows, SummaryItem summary) {
        this.rows = rows;
        this.summary = summary;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public SummaryItem getSummary() {
        return summary;
    }

    public void setSummary(SummaryItem summary) {
        this.summary = summary;
    }
}
