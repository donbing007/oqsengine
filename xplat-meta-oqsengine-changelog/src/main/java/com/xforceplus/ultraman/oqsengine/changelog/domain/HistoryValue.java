package com.xforceplus.ultraman.oqsengine.changelog.domain;

/**
 * show the value at some
 */
public class HistoryValue implements Comparable<HistoryValue>{

    private long commitId;

    private long fieldId;

    private ChangeValue value;

    private HistoryValue preview;

    private HistoryValue next;

    public long getCommitId() {
        return commitId;
    }

    public void setCommitId(long commitId) {
        this.commitId = commitId;
    }

    public ChangeValue getValue() {
        return value;
    }

    public void setValue(ChangeValue value) {
        this.value = value;
    }

    public long getFieldId() {
        return fieldId;
    }

    public void setFieldId(long fieldId) {
        this.fieldId = fieldId;
    }

    public HistoryValue getPreview() {
        return preview;
    }

    public void setPreview(HistoryValue preview) {
        this.preview = preview;
    }

    public HistoryValue getNext() {
        return next;
    }

    public void setNext(HistoryValue next) {
        this.next = next;
    }

    @Override
    public int compareTo(HistoryValue o) {
        return Long.compare(this.commitId, o.commitId);
    }
}
