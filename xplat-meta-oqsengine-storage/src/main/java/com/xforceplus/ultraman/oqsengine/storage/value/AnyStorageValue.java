package com.xforceplus.ultraman.oqsengine.storage.value;

/**
 * 任意对象储存.
 *
 * @author dongbin
 * @version 0.1 2020/3/5 00:40
 * @since 1.8
 */
public class AnyStorageValue extends AbstractStorageValue<Object> {

    /**
     * JSON属性字段前辍.
     */
    public static final char ATTRIBUTE_PREFIX = 'F';
    /**
     * JSON属性附件字段前辍.
     */
    public static final char ATTACHMENT_PREFIX = 'A';

    /**
     * 使用物理字段名和名构造一个储存值实例.
     *
     * @param name      字段名称.
     * @param value     储存的值.
     * @param logicName true 逻辑名称,false 物理储存名称.
     */
    public AnyStorageValue(String name, Object value, boolean logicName) {
        super(name, value, logicName);
    }

    /**
     * 根据物理储存名称获得一个通用的物理储存实例.
     * 其不含有任何值,只作为解析使用.
     *
     * @param storageName 物理储存名称.
     * @return 实例.
     */
    public static StorageValue getInstance(String storageName) {
        return new AnyStorageValue(compatibleStorageName(storageName), null, false);
    }

    /**
     * 为了兼容老的主库存中的字段定义.
     *
     * @param name 主库存字段标识.
     * @return 兼容处理后的结果.
     */
    public static String compatibleStorageName(String name) {
        char flag = name.charAt(0);
        if (ATTRIBUTE_PREFIX == flag || ATTACHMENT_PREFIX == flag) {
            //去除开头的F或者A.
            return name.substring(1);
        } else {
            return name;
        }
    }

    /**
     * 判断是否为普通属性的物理储存名称.
     *
     * @param name 目标名称.
     * @return true 是, false 不是.
     */
    public static boolean isStorageValueName(String name) {
        char flag = name.charAt(0);
        if (ATTRIBUTE_PREFIX == flag) {
            return true;
        } else if (ATTRIBUTE_PREFIX != flag && ATTACHMENT_PREFIX != flag) {
            return true;
        }
        return false;
    }

    /**
     * 判断名称是不是表示一个附件.
     *
     * @param name 目标名称.
     * @return true 是附件, false不是.
     */
    public static boolean isAttachemntStorageName(String name) {
        char flag = name.charAt(0);
        return ATTACHMENT_PREFIX == flag;
    }
}
