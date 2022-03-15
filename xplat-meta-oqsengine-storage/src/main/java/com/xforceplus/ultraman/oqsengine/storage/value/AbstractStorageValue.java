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

    /**
     * 序号开始.
     */
    static final int FIRST_LOCATION = 0;

    private StorageValue<V> next;
    private String logicName;
    private int location;
    private StorageType type;
    private V value;
    private boolean locationAppend = true;
    private int partition;

    /**
     * 构造一个空值表示.
     *
     * @param name 字段名称.
     * @param logicName true 逻辑值, false物理值.
     * @param type 物理类型.
     */
    public AbstractStorageValue(String name, boolean logicName, StorageType type) {
        if (logicName) {
            this.logicName = name;
            this.location = StorageValue.NOT_LOCATION;
        } else {
            this.logicName = parseLocigName(name);
            this.location = parseStorageFieldLocation(name);
        }
        this.type = type;
    }

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
            this.location = StorageValue.NOT_LOCATION;
            this.type = parseValueType(value);
        } else {
            this.logicName = parseLocigName(name);
            this.location = parseStorageFieldLocation(name);
            this.type = parseStorageType(name);
        }
        this.value = value;
        this.partition = StorageValue.NOT_PARTITION;
    }

    @Override
    public StorageValue<V> stick(StorageValue<V> nextValue) {
        StorageValue point = this;

        int loc = point.location();
        // 首结点.
        if (loc == StorageValue.NOT_LOCATION) {
            point.locate(FIRST_LOCATION);
            loc = FIRST_LOCATION;
        }

        // 找到目标点.
        while (point.haveNext()) {
            point.locate(loc++);
            point = point.next();
        }

        point.next(nextValue);
        nextValue.locate(point.location() + 1);

        return this;
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

    /**
     * 目前只有主库代码使用了该方法.
     */
    @Override
    public String storageName() {
        StringBuilder buff = new StringBuilder();
        buff.append(logicName)
            .append(this.type().getType());
        if (location != StorageValue.NOT_LOCATION) {
            buff.append(location);
        }

        return buff.toString();
    }

    /**
     * 目前只有index库代码使用了该方法.
     */
    @Override
    public ShortStorageName shortStorageName() {
        String nameRadix36 = Long.toString(Long.parseLong(logicName), 36);


        /*
            如果是超长字段,需要拼接该部分在整个字段中的位置
            比如: 对于普通String -> rawShortname : 1a2b3h4c5j6k7
                 value AAAAAAABBBBBBBCCCCCCCDDDDDDD
                 将会被转为 P01a2b3h4AAAAAAABBBBBBBCCCCCCCDDDDc5j6k7S 和 P11a2b3h4DDDc5j6k7S的格式
                 对于Strings
                 将会被转为 P01a2b3h4AAAAAAABBBBBBBCCCCCCCDDDDc5j6k7S0 和 P11a2b3h4DDDc5j6k7S1的格式
        */
        int middle = nameRadix36.length() / 2 - 1;
        String head = (partition != StorageValue.NOT_PARTITION) ? PARTITION_FLAG + partition : "";
        return new ShortStorageName(head, nameRadix36.substring(0, middle + 1), nameRadix36.substring(middle + 1), storageTailsName());
    }

    private String storageTailsName() {
        StringBuilder buff = new StringBuilder().append(this.type().getType());
        //  只有标记为需要追加location时才拼接.
        if (locationAppend && location != StorageValue.NOT_LOCATION) {
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
        return Objects.equals(logicName, that.logicName) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logicName, value);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AbstractStorageValue{");
        sb.append("next=").append(next);
        sb.append(", logicName='").append(logicName).append('\'');
        sb.append(", location=").append(location);
        sb.append(", type=").append(type);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
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

    @Override
    public void notLocationAppend() {
        locationAppend = false;
    }

    @Override
    public void partition(int partition) {
        this.partition = partition;
    }

    @Override
    public boolean isEmpty() {
        return value == null;
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
