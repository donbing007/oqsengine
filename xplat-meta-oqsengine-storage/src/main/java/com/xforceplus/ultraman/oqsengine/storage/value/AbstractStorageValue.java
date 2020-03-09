package com.xforceplus.ultraman.oqsengine.storage.value;

import java.util.Objects;

/**
 * 储存类型的抽像.
 * 实现了基本的多值处理.
 *
 * @author dongbin
 * @version 0.1 2020/3/4 13:45
 * @since 1.8
 */
public abstract class AbstractStorageValue<V> implements StorageValue<V> {

    static final int EMPTY_LOCATION = -1;

    private StorageValue<V> next;
    private String logicName;
    private int location;
    private V value;

    /**
     * 使用物理字段名和名构造一个储存值实例.
     * @param name 字段名称.
     * @param value 储存的值.
     * @param logicName true 逻辑名称,false 物理储存名称.
     */
    public AbstractStorageValue(String name, V value, boolean logicName) {
        if (logicName) {
            this.logicName = name;
            this.location = EMPTY_LOCATION;
        } else {
            this.logicName = parseLocigName(name);
            this.location = parseStorageFieldLocation(name);
        }
        this.value = value;
    }

    @Override
    public StorageValue<V> stick(StorageValue<V> nextValue) {
        if (this.location() == EMPTY_LOCATION) {
            throw new IllegalStateException("The current node has no specified order.");
        }

        // 表示追加到尾部
        int loc;
        if (nextValue.location() == EMPTY_LOCATION) {
            // 表示最后一个位置.
            loc = Integer.MAX_VALUE;
        } else {
            loc = nextValue.location();
        }

        StorageValue head = this;
        // 更新首结点.
        if (head.location() > loc) {

            nextValue.next(head);
            head = nextValue;

        } else {

            StorageValue point = this;
            while (true) {

                if (point.haveNext()) {
                    if (point.location() > loc) {
                        break;
                    } else {
                        point = point.next();
                    }
                } else {
                    break;
                }
            }
            AbstractStorageValue temp = (AbstractStorageValue) point.next();
            point.next(nextValue);
            nextValue.next(temp);

            if (nextValue.location() == EMPTY_LOCATION) {
                nextValue.locate(point.location() + 1);
            }
        }

        return head;
    }

    @Override
    public boolean haveNext() {
        return next != null;
    }

    @Override
    public StorageValue<V> next() {
        return next;
    }

    @Override
    public void next(StorageValue<V> value) {
        this.next = value;
    }

    @Override
    public String logicName() {
        return logicName;
    }

    @Override
    public String storageName() {
        StringBuilder buff = new StringBuilder();
        buff.append(logicName)
            .append(this.type().getType());
        if (location != EMPTY_LOCATION) {
            buff.append(location);
        }

        return buff.toString();
    }

    @Override
    public V value() {
        return value;
    }

    @Override
    public int locate(int location) {
        this.location = location;
        return this.location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractStorageValue)) {
            return false;
        }
        AbstractStorageValue<?> that = (AbstractStorageValue<?>) o;
        return Objects.equals(logicName, that.logicName) &&
            Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicName, value);
    }

    @Override
    public String toString() {
        return "AbstractStorageValue{" +
            "next=" + next +
            ", name='" + logicName + '\'' +
            ", value=" + value +
            '}';
    }

    @Override
    public int location() {
        return this.location;
    }

    // 解析逻辑字段名.
    private static String parseLocigName(String target) {
        int index = findTypeFlagIndex(target);

        return target.substring(0, index);
    }

    // 解析字段位置.
    private static int parseStorageFieldLocation(String target) {
        int index = findTypeFlagIndex(target);
        if (index == target.length() - 1) {
            return StorageValue.NOT_LOCATION;
        }
        return Integer.parseInt(target.substring(index + 1));
    }

    // 查找类型标识符位置.
    private static int findTypeFlagIndex(String target) {
        int index = target.length() - 1;
        char point;
        for (int i = target.length() - 1; i >= 0; i--) {
            point = target.charAt(i);
            if (point >= 'A' && point <= 'Z') {
                index = i;
                break;
            }
        }

        return index;
    }
}
