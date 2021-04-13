package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import com.alibaba.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.transaction.cache.CacheEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

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
    private AtomicLong opNumber = new AtomicLong(-1);

    private CacheEventHandler cacheEventHandler;
    private long txId;

    public DefaultTransactionAccumulator(long txId, CacheEventHandler cacheEventHandler) {
        this.txId = txId;
        this.cacheEventHandler = cacheEventHandler;
    }

    @Override
    public boolean accumulateBuild(IEntity entity) {
        buildNumbers.incrementAndGet();
        opNumber.incrementAndGet();

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: create number +1.[{}]", entity.id());
        }

        return cacheEventHandler.create(txId, opNumber.get(), entity);
    }

    @Override
    public boolean accumulateReplace(IEntity newEntity, IEntity oldEntity) {
        replaceNumbers.incrementAndGet();
        opNumber.incrementAndGet();

        getProcessIdsIdsSet(true).add(newEntity.id());

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: replace number +1.[{}]", newEntity.id());
        }

        return cacheEventHandler.replace(txId, opNumber.get(), newEntity, oldEntity);
    }

    @Override
    public boolean accumulateDelete(IEntity entity) {
        deleteNumbers.incrementAndGet();

        getProcessIdsIdsSet(true).add(entity.id());

        opNumber.incrementAndGet();

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: delete number +1.[{}]", entity.id());
        }

        return cacheEventHandler.delete(txId, opNumber.get(), entity);
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
        opNumber.set(0);
    }

    @Override
    public long operationNumber() {
        return opNumber.get();
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
