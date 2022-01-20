package com.xforceplus.ultraman.oqsengine.common.load.loadfactor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * 当前堆内存的负载.
 *
 * @author dongbin
 * @version 0.1 2022/1/17 16:29
 * @since 1.8
 */
public class HeapMemoryLoadFactory extends AbstractLoadFactor {

    /*
    如果无法获取内存上限,假定为6G.
     */
    private static final long DEFAULT_MAX = 6 * 1024 * 1024 * 1024;
    private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();

    public HeapMemoryLoadFactory() {
        this(LoadFactor.MAX_WEIGHT);
    }

    public HeapMemoryLoadFactory(double weight) {
        super(weight);
    }

    @Override
    public double now() {
        MemoryUsage memoryUsage = MEMORY_MX_BEAN.getHeapMemoryUsage();

        long max = memoryUsage.getMax();
        if (max < 0) {
            max = DEFAULT_MAX;
        }

        long used = memoryUsage.getUsed();

        return used / max;
    }
}
