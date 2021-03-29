package com.xforceplus.ultraman.oqsengine.changelog.domain;

/**
 * value life
 */
public class ValueLife {

    private String value;

    private long start = -1;

    private long end = -1;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }
}
