package com.xforceplus.ultraman.oqsengine.lock.utils;

/**
 * 锁定者信息帮助工具.
 *
 * @author dongbin
 * @version 0.1 2022/1/4 11:04
 * @since 1.8
 */
public class LockerSupport {

    /**
     * 锁定KEY前辍.
     */
    public static final String LOCK_KEY_PREIFX = "#locker.";

    /**
     * 构造锁定的KEY.
     *
     * @param resource 资源.
     * @return 锁定的key.
     */
    public static String buildLockKey(String resource) {
        return LOCK_KEY_PREIFX.concat(resource);
    }

    /**
     * 根据加锁key,解析出资源.
     *
     * @param lockKey 加锁key.
     * @return 资源.
     */
    public static String parseResourceFormLockKey(String lockKey) {
        return lockKey.substring(LOCK_KEY_PREIFX.length());
    }

    public static void main(String[] args) {
        String key = buildLockKey("123");
        System.out.println(parseResourceFormLockKey(key));
    }

    /*
     * 为每个操作的线程记录一个唯一ID号
     */
    private static final ThreadLocal<Locker> THREAD_LOCAL = new ThreadLocal();

    /**
     * 返回当前线程的唯一标识,如果没有标识则会生成一个.
     *
     * @return 当前线程的标识.
     */
    public static Locker getLocker() {
        Locker locker = THREAD_LOCAL.get();
        if (locker == null) {
            locker = new Locker();
            THREAD_LOCAL.set(locker);
        }

        return locker;
    }

    /**
     * 清除当前锁定者信息如果可以.
     * 可以的判断为成功锁定的数量为0.
     */
    public static void cleanLockerIfCan() {
        Locker locker = THREAD_LOCAL.get();
        if (locker != null) {
            if (locker.getSuccessLockNumber() <= 0) {
                THREAD_LOCAL.remove();
            }
        }
    }
}
