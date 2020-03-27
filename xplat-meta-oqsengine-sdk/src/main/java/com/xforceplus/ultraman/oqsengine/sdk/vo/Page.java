package com.xforceplus.ultraman.oqsengine.sdk.vo;


import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 分页转换对象
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @param <T>
 *
 * @since 1.8
 */
public class Page<T> implements Externalizable, Cloneable {

    private static final long serialVersionUID = 8545996863226528798L;

    /**
     * 查询数据列表
     */
    private List<T> rows = Collections.emptyList();
    /**
     * 统计数据
     */
    private Summary summary = new Summary();
    /**
     * 每页显示条数，默认 10
     */
    private long size = 10;

    /**
     * 当前页
     */
    private long current = 1;

    public Page(List<T> rows, Summary summary, long size, long current) {
        this.rows = rows;
        this.summary = summary;
        this.size = size;
        this.current = current;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<T> getRows() {
        return rows;
    }

    public void setRows(List<T> rows) {
        this.rows = rows;
    }

    public Summary getSummary() {
        return summary;
    }

    public void setSummary(Summary summary) {
        this.summary = summary;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

    }
    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Page)) return false;
        Page<?> page = (Page<?>) o;
        return getSize() == page.getSize() &&
                getCurrent() == page.getCurrent() &&
                Objects.equals(getRows(), page.getRows()) &&
                Objects.equals(getSummary(), page.getSummary());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRows(), getSummary(), getSize(), getCurrent());
    }
}
