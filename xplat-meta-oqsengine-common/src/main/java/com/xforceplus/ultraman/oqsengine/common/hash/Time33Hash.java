package com.xforceplus.ultraman.oqsengine.common.hash;

/**
 * @author dongbin
 * @version 0.1 2020/2/16 19:23
 * @since 1.8
 */
public class Time33Hash {

    /**
     * 标示值.
     */
    private static final String MARK_CODE = "Times33";
    /**
     * 静态化实例
     */
    private static Time33Hash ALGORITHMS = new Time33Hash();

    /**
     * 返回一个可用的Times33算法实例。
     * @return 算法实例。
     */
    public static Time33Hash build() {
        return ALGORITHMS;
    }

    /**
     * 默认构造方法。
     */
    private Time33Hash() {
    }

    /**
     * 以times33算法进行哈希计算.
     * @param key 需要进行哈希计算的key.
     * @return 哈希结果.
     */
    public int hash(String key) {
        if (key == null) {
            return 0;
        }
        char c;
        int hashCode = 0;
        int keyLen = key.length();
        for (int count = 0; count < keyLen; count++) {
            c = key.charAt(count);
            //hashCode * 33 + c
            hashCode = (hashCode << 5) + hashCode + c;
        }

        return hashCode;
    }

    /**
     * 判断给定的参数是否为Times33算法实现.
     * @param obj 需要比对的对象.
     * @return true 是Times33算法实现,false不是.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (getClass().equals(obj.getClass())) {
            return true;
        }
        return false;
    }

    /**
     * 算法的哈希值.
     * @return 哈希值.
     */
    @Override
    public int hashCode() {
        return MARK_CODE.hashCode();
    }

    /**
     * Times33算法实现的字符串表示.
     * @return 字符串表示.
     */
    @Override
    public String toString() {
        return "Times33 hash algorithm.";
    }
}
