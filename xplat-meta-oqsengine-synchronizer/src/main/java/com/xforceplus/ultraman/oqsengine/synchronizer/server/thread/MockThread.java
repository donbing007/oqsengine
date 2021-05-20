package com.xforceplus.ultraman.oqsengine.synchronizer.server.thread;

import java.util.Objects;

/**
 * mock thread.
 */
public class MockThread {

    private String uuid;

    public MockThread(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MockThread that = (MockThread) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "MockThread{" +
                "uuid='" + uuid + '\'' +
                '}';
    }
}
