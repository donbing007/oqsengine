package com.xforceplus.ultraman.oqsengine.lock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * JVM级别资源锁实现.
 *
 * @author dongbin
 * @version 0.1 2021/08/09 14:57
 * @since 1.8
 */
public class LocalResourceLockerTest extends AbstractRetryResourceLockerTest {

    private LocalResourceLocker locker;

    @BeforeEach
    @Override
    public void before() throws Exception {
        super.before();

        locker = new LocalResourceLocker();
    }

    @AfterEach
    public void after() throws Exception {
        super.after();
        locker = null;
    }

    @Override
    public ResourceLocker getLocker() {
        return this.locker;
    }

}