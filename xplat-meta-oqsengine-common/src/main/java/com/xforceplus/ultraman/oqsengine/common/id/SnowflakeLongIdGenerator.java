package com.xforceplus.ultraman.oqsengine.common.id;

import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * snowflake 的 ID 算法生成.
 * 结果是一个64位的长整形. 组合方式如下.
 * 重置不会有任何改变.
 *
 * @author luyi
 * @version 0.1 2020/2/16 22:39
 * @since 1.8
 */
public class SnowflakeLongIdGenerator implements LongIdGenerator {

    public static final int NODE_SHIFT = 10;
    public static final int SEQ_SHIFT = 12;

    public static final short MAX_NODE = 1024 - 1;
    public static final short MAX_SEQUENCE = 4096 - 1;

    private short sequence;
    private long referenceTime;

    private long twepoch = 1288834974657L;

    private int node;

    /**
     * 实例化.
     *
     * @param nodeIdGenerator 结点ID生成器实例.
     */
    public SnowflakeLongIdGenerator(NodeIdGenerator nodeIdGenerator) {
        int nodeId = nodeIdGenerator.next();
        if (nodeId < 0 || nodeId > MAX_NODE) {
            throw new IllegalArgumentException(String.format("node is between %s and %s", 0, MAX_NODE));
        }
        this.node = nodeId;
    }

    @Override
    public Long next() {
        long currentTime;
        long counter;

        synchronized (this) {
            currentTime = System.currentTimeMillis();
            if (currentTime < referenceTime) {
                long offset = referenceTime - currentTime;
                if (offset <= 5) {
                    LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(offset << 1));
                    currentTime = timeGen();
                    if (currentTime < referenceTime) {
                        throw new ClockBackwardsException(referenceTime, currentTime);
                    }
                } else {
                    //throw
                    throw new ClockBackwardsException(referenceTime, currentTime);
                }
            }

            if (currentTime > referenceTime) {
                this.sequence = 0;
            } else {
                //time is equals
                if (this.sequence < MAX_SEQUENCE) {
                    this.sequence++;
                } else {
                    currentTime = tilNextMillis(referenceTime);
                }
            }

            counter = this.sequence;
            referenceTime = currentTime;
        }

        return (currentTime - twepoch) << NODE_SHIFT << SEQ_SHIFT | node << SEQ_SHIFT | counter;
    }

    private Long timeGen() {
        return System.currentTimeMillis();
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    @Override
    public boolean isContinuous() {
        return false;
    }

    @Override
    public boolean isPartialOrder() {
        return true;
    }

    @Override
    public void reset() {

    }
}
