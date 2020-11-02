package com.xforceplus.ultraman.oqsengine.common.version;

/**
 * 版本帮助工具.
 *
 * @author dongbin
 * @version 0.1 2020/10/23 14:49
 * @since 1.8
 */
public class VersionHelp {

    /**
     * 万能版本号,此版本和任何版本都匹配.
     */
    public static int OMNIPOTENCE_VERSION = Integer.MAX_VALUE;

    /**
     * 是否为万能版本号.
     *
     * @param version
     * @return true 是,false不是.
     */
    public static boolean isOmnipotence(int version) {
        return version == Integer.MAX_VALUE;
    }
}
