package com.xforceplus.ultraman.oqsengine.common.id;

import java.util.UUID;

/**
 * UUID的生成器.
 *
 * @author dongbin
 * @version 0.1 2020/11/26 11:05
 * @since 1.8
 */
public class UUIDGenerator implements IdGenerator<String> {

    private static final IdGenerator<String> INSTANCE = new UUIDGenerator();

    public static IdGenerator<String> getInstance() {
        return INSTANCE;
    }

    private volatile String current;

    private UUIDGenerator() {
        current = "";
    }

    @Override
    public String next() {
        String newValue = UUID.randomUUID().toString();

        synchronized (this) {
            this.current = newValue;
        }

        return newValue;
    }

    @Override
    public String current() {
        return current;
    }
}
