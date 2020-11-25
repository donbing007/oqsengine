package com.xforceplus.ultraman.oqsengine.common.lock.process;

import com.xforceplus.ultraman.oqsengine.common.lock.ILock;
import com.xforceplus.ultraman.oqsengine.common.lock.ILockFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.common.lock.LockHelper.ProcessHandlerLevel.getHandler;

/**
 * desc :
 * name : ProcessLockFactory
 *
 * @author : xujia
 * date : 2020/9/7
 * @since : 1.8
 */
public class ProcessLockFactory implements ILockFactory {

    private DefaultProcessLockHandler defaultProcessLockHandler;

    public ProcessLockFactory(String handlerType) throws InstantiationException, IllegalAccessException {
        defaultProcessLockHandler = getHandler(handlerType);
    }

    @Override
    public ILock buildLock(String resourceId, long time, TimeUnit timeUnit) {
        return new ProcessLock(resourceId, time, timeUnit, defaultProcessLockHandler);
    }

    /**
     * desc :
     * name : LocalProcessLockHandler
     *
     * @author : xujia
     * date : 2020/9/7
     * @since : 1.8
     */
    public static class LocalProcessLockHandler implements DefaultProcessLockHandler {

        private static final Map<String, String> lockMapping = new ConcurrentHashMap<>();

        @Override
        public boolean remove(String key) {
            return null != lockMapping.remove(key);
        }

        @Override
        public boolean lock(String key, String value) {
            return null == lockMapping.putIfAbsent(key, value);
        }

        @Override
        public String isLocked(String key) {
            return lockMapping.get(key);
        }
    }
}
