package com.xforceplus.ultraman.oqsengine.synchronizer.server.thread;

/**
 * thread info.
 */
public class ThreadInfo {

    private String uuid;

    public ThreadInfo(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "uuid='" + uuid + '\'' +
                '}';
    }
}
