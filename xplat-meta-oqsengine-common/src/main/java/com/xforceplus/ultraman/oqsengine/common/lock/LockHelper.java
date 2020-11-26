package com.xforceplus.ultraman.oqsengine.common.lock;

import com.xforceplus.ultraman.oqsengine.common.lock.process.DefaultProcessLockHandler;
import com.xforceplus.ultraman.oqsengine.common.lock.process.ProcessLockFactory;

/**
 * desc :
 * name : LockHelper
 *
 * @author : xujia
 * date : 2020/9/3
 * @since : 1.8
 */
public class LockHelper {
    public static final String SLOT_LOCK_STR = "slot_init_lock";
    public static final String ENTITY_CLASS_LOCK_STR = "entity_class_lock";

    public static final String GET_LOCK = "SELECT GET_LOCK(?, ?)";
    public static final String IS_FREE_LOCK = "SELECT IS_FREE_LOCK(?)";
    public static final String RELEASE_LOCK = "SELECT RELEASE_LOCK(?)";

    //  retry 轮询间隔 10 ms
    public static final long RETRY_DELAY = 10;

    //  默认超时3秒
    public static final long DEFAULT_TIME_OUT = 3;

    public static final long DEFAULT_RETRY_TIMES = 3;
    /**
     * desc :
     * name : PermissionLevel
     *
     * @author : xujia
     * date : 2020/9/3
     * @since : 1.8
     */
    public enum ProcessHandlerLevel {
        LOCAL(ProcessLockFactory.LocalProcessLockHandler.class),
        CACHE(null);

        private Class<? extends DefaultProcessLockHandler> clazz;

        ProcessHandlerLevel(Class<? extends DefaultProcessLockHandler> clazz) {
            this.clazz = clazz;
        }

        public static DefaultProcessLockHandler getHandler(String processHandlerLevel)
                throws IllegalAccessException, InstantiationException {

            if (null == processHandlerLevel || processHandlerLevel.isEmpty()) {
                return ProcessLockFactory.LocalProcessLockHandler.class.newInstance();
            }

            for (ProcessHandlerLevel pl : ProcessHandlerLevel.values()) {
                if (processHandlerLevel.equalsIgnoreCase(pl.name())) {
                    if (null != pl.clazz) {
                        return pl.clazz.newInstance();
                    }
                }
            }
            return ProcessLockFactory.LocalProcessLockHandler.class.newInstance();
        }
    }
}
