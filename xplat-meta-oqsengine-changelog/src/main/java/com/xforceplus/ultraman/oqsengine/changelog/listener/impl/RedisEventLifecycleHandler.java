package com.xforceplus.ultraman.oqsengine.changelog.listener.impl;

import com.xforceplus.ultraman.oqsengine.changelog.listener.EventLifecycleAware;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.RollbackPayload;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * TODO abstract
 * redis lifecycle handler implementation
 */
public class RedisEventLifecycleHandler implements EventLifecycleAware {


    private RedisClient redisClient;

    private RedisCommands<String, String> syncCommands;

    private Logger logger = LoggerFactory.getLogger(RedisEventLifecycleHandler.class);

    private static final String MISSING_PAYLOAD = "Event {} has no payload";

    public RedisEventLifecycleHandler(RedisClient redisClient) {
        this.redisClient = redisClient;
        this.syncCommands = redisClient.connect().sync();
    }

    /**
     * new tx
     */
    @Override
    public void onTxCreate(ActualEvent<BeginPayload> begin) {
        extract(begin, payload -> {
            long txId = payload.getTxId();
            createQueueIfNotExists(txId);
        });
    }

    @Override
    public void onEntityCreate(ActualEvent<BuildPayload> create) {
        extract(create, createPayload -> {
            createPayload.getEntity();
            pushQueue(txId, createPayload.getEntity());

        });
    }

    @Override
    public void onEntityUpdate(ActualEvent<ReplacePayload> update) {

    }

    @Override
    public void onEntityDelete(ActualEvent<DeletePayload> delete) {

    }

    @Override
    public void onTxPreCommit(ActualEvent<CommitPayload> preCommit) {

    }

    @Override
    public void onTxCommitted(ActualEvent<CommitPayload> commited) {
        //trigger a combine
    }

    @Override
    public void onTxPreRollBack(ActualEvent<RollbackPayload> preRollBack) {

    }

    @Override
    public void onTxRollBack(ActualEvent<RollbackPayload> preRollBack) {

    }

    /**
     * TODO which queue
     * create the tx queue
     */
    private void createQueueIfNotExists(long txId) {

    }

    private <T extends Serializable> void extract(ActualEvent<T> evt, Consumer<T> consumer) {
        Optional<T> payloadOp = evt.payload();
        if (payloadOp.isPresent()) {
            consumer.accept(payloadOp.get());
        } else {
            logger.error(MISSING_PAYLOAD, evt);
        }
    }
}
