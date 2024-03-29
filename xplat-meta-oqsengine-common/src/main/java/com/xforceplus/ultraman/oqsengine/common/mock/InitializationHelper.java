package com.xforceplus.ultraman.oqsengine.common.mock;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class InitializationHelper {

    static final Logger LOGGER = LoggerFactory.getLogger(InitializationHelper.class);

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
        CommonInitialization commonInitialization = null;
        for (BeanInitialization beanInitialization : clearList) {
            if (beanInitialization instanceof CommonInitialization) {
                commonInitialization = (CommonInitialization) beanInitialization;
            } else {
                beanInitialization.clear();
            }
        }
        if (null != commonInitialization) {
            commonInitialization.clear();
        }
    }

    /**
     * 销毁.
     */
    public static void destroy() {
        CommonInitialization commonInitialization = null;
        try {
            for (BeanInitialization beanInitialization : clearList) {
                //  commonInitialization必须最后关闭
                if (beanInitialization instanceof CommonInitialization) {
                    commonInitialization = (CommonInitialization) beanInitialization;
                } else {
                    try {
                        LOGGER.info("destroy beanInitialization {}...",
                            beanInitialization.getClass().getCanonicalName());
                        beanInitialization.destroy();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } finally {
            try {
                if (null != commonInitialization) {
                    LOGGER.info("destroy beanInitialization {}...",
                        commonInitialization.getClass().getCanonicalName());
                    commonInitialization.destroy();
                }
            } finally {
                clearList.clear();
            }
        }
    }
}
