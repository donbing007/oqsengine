package com.xforceplus.ultraman.oqsengine.common.map;

/**
 * Map帮助工具.
 *
 * @author dongbin
 * @version 0.1 2021/3/8 11:42
 * @since 1.8
 */
public class MapUtils {

    /**
     * 计算Map的初始大小.
     * 尽量不触发rehash的大小.
     *
     * @param size       目标元素数量.
     * @param loadFactor 加载因子.
     * @return 实际大小.
     */
    public static int calculateInitSize(int size, float loadFactor) {
        return (int) (size + (size * (1.0F - loadFactor)));
    }

    /**
     * 默认加载因子的初始化大小.
     *
     * @param size 目标元素数量.
     * @return 实际大小.
     */
    public static int calculateInitSize(int size) {
        return calculateInitSize(size, 0.75F);
    }
}
