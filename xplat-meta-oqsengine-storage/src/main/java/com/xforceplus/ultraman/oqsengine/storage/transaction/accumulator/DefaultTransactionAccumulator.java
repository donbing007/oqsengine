package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import com.alibaba.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.LongStream;

/**
 * 累加器的默认实现.
 *
 * @author dongbin
 * @version 0.1 2020/12/11 15:30
 * @since 1.8
 */
public class DefaultTransactionAccumulator implements TransactionAccumulator {

    private final Logger logger = LoggerFactory.getLogger(DefaultTransactionAccumulator.class);

    private AtomicLong buildNumbers = new AtomicLong(0);
    private AtomicLong replaceNumbers = new AtomicLong(0);
    private AtomicLong deleteNumbers = new AtomicLong(0);

    private volatile Set<Long> processIds = null;
    private final Object processIdsLock = new Object();

    @Override
    public void accumulateBuild(long id) {
        buildNumbers.incrementAndGet();

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: create number +1.[{}]", id);
        }
    }

    @Override
    public void accumulateReplace(long id) {
        replaceNumbers.incrementAndGet();

        getProcessIdsIdsSet(true).add(id);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: replace number +1.[{}]", id);
        }
    }

    @Override
    public void accumulateDelete(long id) {
        deleteNumbers.incrementAndGet();

        getProcessIdsIdsSet(true).add(id);

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: delete number +1.[{}]", id);
        }
    }

    @Override
    public long getBuildNumbers() {
        return buildNumbers.get();
    }

    @Override
    public long getReplaceNumbers() {
        return replaceNumbers.get();
    }

    @Override
    public long getDeleteNumbers() {
        return deleteNumbers.get();
    }

    @Override
    public Set<Long> getUpdateIds() {
        Set<Long> ids = getProcessIdsIdsSet(false);
        if (Collections.emptySet().equals(ids)) {
            return ids;
        } else {
            return new HashSet(ids);
        }
    }

    @Override
    public void reset() {
        getProcessIdsIdsSet(false).clear();
        buildNumbers.set(0);
        replaceNumbers.set(0);
        deleteNumbers.set(0);
    }

    private Set<Long> getProcessIdsIdsSet(boolean build) {
        if (null == processIds) {
            if (!build) {
                return Collections.emptySet();
            } else {
                synchronized (processIdsLock) {
                    if (null == processIds) {
                        processIds = Sets.newConcurrentHashSet();
                    }
                }
            }
        }

        return processIds;
    }
}
