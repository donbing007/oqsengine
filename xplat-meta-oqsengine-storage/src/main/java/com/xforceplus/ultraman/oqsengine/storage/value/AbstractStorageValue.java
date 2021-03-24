package com.xforceplus.ultraman.oqsengine.storage.value;

import com.xforceplus.ultraman.oqsengine.storage.StorageType;

import java.util.Objects;

/**
 * 储存类型的抽像.
 * 实现了基本的多值处理.
 * 短名称假定
 *
 * @param <V> 实际值类型.
 * @author dongbin
 * @version 0.1 2020/3/4 13:45
 * @since 1.8
 */
public abstract class AbstractStorageValue<V> implements StorageValue<V> {

    static final int EMPTY_LOCATION = -1;

    private StorageValue<V> next;
    private String logicName;
    private int location;
    private StorageType type;
    private V value;

    /**
     * 使用物理字段名和名构造一个储存值实例.
     *
     * @param name      字段名称.
     * @param value     储存的值.
     * @param logicName true 逻辑名称,false 物理储存名称.
     */
    public AbstractStorageValue(String name, V value, boolean logicName) {
        if (logicName) {
            this.logicName = name;
            this.location = EMPTY_LOCATION;
            this.type = parseValueType(value);
        } else {
            this.logicName = parseLocigName(name);
            this.location = parseStorageFieldLocation(name);
            this.type = parseStorageType(name);
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
    public StorageType type() {
        return type;
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
        return doStorageName(logicName);
    }

    @Override
    public ShortStorageName shortStorageName() {
        String nameRadix36 = Long.toString(Long.parseLong(logicName), 36);

        String shortStorageName = doStorageName(nameRadix36);

        int middle = shortStorageName.length() / 2 - 1;
        return new ShortStorageName(shortStorageName.substring(0, middle + 1), shortStorageName.substring(middle + 1));
    }

    private String doStorageName(String base) {
        StringBuilder buff = new StringBuilder();
        buff.append(base)
            .append(this.type().getType());
        if (location != EMPTY_LOCATION) {
            buff.append(location);
        }

        return buff.toString();
    }

    @Override
    public String groupStorageName() {
        return String.join("", logicName, Character.toString(this.type().getType()));
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

    // 根据值类型解析.
    private static StorageType parseValueType(Object value) {
        if (value == null) {
            return StorageType.UNKNOWN;
        }
        if (String.class.isInstance(value)) {
            return StorageType.STRING;
        } else if (Integer.class.isInstance(value) || Long.class.isInstance(value)) {
            return StorageType.LONG;
        } else {
            return StorageType.UNKNOWN;
        }
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

    // 解析类型
    private static StorageType parseStorageType(String target) {
        int index = findTypeFlagIndex(target);

        char t = target.charAt(index);
        t = Character.toUpperCase(t);
        return StorageType.valueOf(t);
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

        if (index <= 0) {
            throw new IllegalArgumentException(
                String.format(
                    "Invalid physical storage field because the type identifier could not be located.[%s]", target));
        }

        return index;
    }
}
