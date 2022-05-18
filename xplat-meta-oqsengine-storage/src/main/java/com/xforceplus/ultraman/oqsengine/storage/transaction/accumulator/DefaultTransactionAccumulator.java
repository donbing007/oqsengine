package com.xforceplus.ultraman.oqsengine.storage.transaction.accumulator;

import com.alibaba.google.common.collect.Sets;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 累加器的默认实现.
 * 累加器工作在事务中,每一个事务中不应该出现并发.
 * 所以累加器的实现不需要考虑在并发安全.
 *
 * @author dongbin
 * @version 0.1 2020/12/11 15:30
 * @since 1.8
 */
public class DefaultTransactionAccumulator implements TransactionAccumulator {

    private final Logger logger = LoggerFactory.getLogger(DefaultTransactionAccumulator.class);

    private int buildNumbers = 0;
    private int replaceNumbers = 0;
    private int deleteNumbers = 0;

    private volatile Set<Long> processIds = null;
    private final Object processIdsLock = new Object();
    /**
     * 当前操作的最大序号,从0开始.
     */
    private int opNumber = -1;

    private long txId;

    public DefaultTransactionAccumulator(long txId) {
        this.txId = txId;
    }

    @Override
    public boolean accumulateBuild(IEntity entity) {
        buildNumbers++;
        opNumber++;

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: create number +1.[{}]", entity.id());
        }

        return true;
    }

    @Override
    public boolean accumulateReplace(IEntity entity) {
        replaceNumbers++;
        opNumber++;

        getProcessIdsIdsSet(true).add(entity.id());

        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: replace number +1.[{}]", entity.id());
        }

        return true;
    }

    @Override
    public boolean accumulateDelete(IEntity entity) {
        deleteNumbers++;
        opNumber++;

        getProcessIdsIdsSet(true).add(entity.id());


        if (logger.isDebugEnabled()) {
            logger.debug("Transaction Accumulator: delete number +1.[{}]", entity.id());
        }

        return true;
    }

    @Override
    public long getBuildNumbers() {
        return buildNumbers;
    }

    @Override
    public long getReplaceNumbers() {
        return replaceNumbers;
    }

    @Override
    public long getDeleteNumbers() {
        return deleteNumbers;
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
        buildNumbers = 0;
        replaceNumbers = 0;
        deleteNumbers = 0;
        opNumber = 0;
    }

    @Override
    public long operationNumber() {
        return opNumber;
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
