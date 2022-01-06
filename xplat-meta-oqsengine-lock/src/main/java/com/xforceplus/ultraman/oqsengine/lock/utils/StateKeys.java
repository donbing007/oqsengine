package com.xforceplus.ultraman.oqsengine.lock.utils;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 一个状态key列表,其包含一个游标来标识当前处理进度.
 *
 * @author dongbin
 * @version 0.1 2022/1/4 16:15
 * @since 1.8
 */
public class StateKeys implements Serializable {

    private String[] keys;
    // 0 first.
    private int cursor;

    public StateKeys(String[] keys) {
        this.keys = keys;
        this.cursor = 0;
    }

    /**
     * 移动游标,从0开始.
     * 如果移动超出最大偏移,那么将标示为不可移动.
     */
    public void move() {
        move(1);
    }

    /**
     * 移动游标指定偏移量, 从0开始.
     * 如果移动超出最大偏移,那么将标示为不可移动.
     *
     * @param n 需要移动的量.需要正数.
     */
    public void move(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("Only positive numbers are accepted.");
        }
        if (cursor == -1) {
            throw new IllegalStateException("The cursor is not movable.");
        }

        cursor += n;

        if (cursor > keys.length - 1) {
            cursor = -1;
        }
    }

    public String[] getKeys() {
        return keys;
    }

    public String[] getNoCompleteKeys() {
        return Arrays.stream(keys).skip(cursor).toArray(String[]::new);
    }

    public String[] getCompleteKeys() {
        return Arrays.copyOfRange(keys, 0, cursor);
    }

    public int size() {
        return this.keys.length;
    }

    public String getCurrentKey() {
        return keys[cursor];
    }

    public int getCursor() {
        return cursor;
    }

    public boolean isComplete() {
        return cursor == -1;
    }

    public StateKeys copy() {
        return new StateKeys(keys);
    }

}
