package com.xforceplus.ultraman.oqsengine.storage;

/**
 * 保留关键字.
 * 表示这些字符串只能为OQS内部使用.
 *
 * @author dongbin
 * @version 0.1 2022/5/11 13:00
 * @since 1.8
 */
public final class ReservedFieldNameWord {

    private static String[] words = new String[] {
        "id"
    };

    private ReservedFieldNameWord() {
    }

    /**
     * 判断是否保留字.
     * 不区分大小写.
     *
     * @param word 需要检测的字串.
     * @return true 是保留字, false不是保留字.
     */
    public static boolean isReservedWorkd(String word) {
        for (String w : words) {
            if (w.equalsIgnoreCase(word)) {
                return true;
            }
        }

        return false;
    }
}
