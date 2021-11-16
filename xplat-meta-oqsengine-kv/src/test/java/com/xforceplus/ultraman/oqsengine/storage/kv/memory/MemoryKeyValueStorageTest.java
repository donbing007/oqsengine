package com.xforceplus.ultraman.oqsengine.storage.kv.memory;

import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.storage.kv.AbstractKVTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * 基于内存的kv实现测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/17 11:44
 * @since 1.8
 */
public class MemoryKeyValueStorageTest extends AbstractKVTest {

    private MemoryKeyValueStorage kv;

    @BeforeEach
    public void before() throws Exception {
        kv = new MemoryKeyValueStorage();
    }

    @AfterEach
    public void after() throws Exception {
        kv = null;
    }

    @Override
    public KeyValueStorage getKv() {
        return this.kv;
    }

}