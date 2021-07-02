package com.xforceplus.ultraman.oqsengine.common.mock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class InitializationHelper {
    private static List<BeanInitialization> clearList = new ArrayList<>();

    /**
     * 加入.
     */
    public static void add(BeanInitialization beanInitialization) {
        clearList.add(beanInitialization);
    }

    /**
     * 清理.
     */
    public static void clearAll() throws Exception  {
        for (BeanInitialization beanInitialization : clearList) {
            beanInitialization.destroy();
        }
    }
}
