package com.xforceplus.ultraman.oqsengine.common.id;

/**
 * 标识生成.
 *
 * @param <V> id 类型.
 * @author dongbin
 * @version 0.1 2020/2/16 22:39
 * @since 1.8
 */
public interface IdGenerator<V> {

    /**
     * 获取一个默认的 NameSpace ID 表示.
     *
     * @return ID.
     */
    V next();

    /**
     * 获取一个指定nameSpace下的ID.
     * <br>
     * 当前实现是否支持命名空间,supportNameSpace 返回true表示支持此方法,false此方法将抛出异常.
     *
     * @param nameSpace 命名空间.
     * @return ID.
     * @throws UnsupportedOperationException 如果当前实现不支持命名空间.
     */
    default V next(String nameSpace) {
        throw new UnsupportedOperationException(
            String.format("The current ID generator(%s) does not support namespaces.", this.getClass().getSimpleName()));
    }

    /**
     * 是否支持命名空间.
     *
     * @return true 命名空间, false不支持.
     */
    default boolean supportNameSpace() {
        return false;
    }
}
