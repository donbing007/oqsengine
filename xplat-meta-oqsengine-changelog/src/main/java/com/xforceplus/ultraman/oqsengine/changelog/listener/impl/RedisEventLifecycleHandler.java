package com.xforceplus.ultraman.oqsengine.changelog.listener.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.ChangelogHandler;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.TransactionalChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ValueWrapper;
import com.xforceplus.ultraman.oqsengine.changelog.listener.EventLifecycleAware;
import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.BuildPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.DeletePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.entity.ReplacePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.BeginPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.CommitPayload;
import com.xforceplus.ultraman.oqsengine.event.payload.transaction.RollbackPayload;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * TODO abstract
 * redis lifecycle handler implementation
 */
public class RedisEventLifecycleHandler implements EventLifecycleAware {


    private RedisClient redisClient;

    private RedisCommands<String, String> syncCommands;

    private ObjectMapper mapper;

    private Logger logger = LoggerFactory.getLogger(RedisEventLifecycleHandler.class);

    private static final String MISSING_PAYLOAD = "Event {} has no payload";

    private static final String NO_OPERATION = "Do nothing in {} stage";

    private static final String QUEUE_PREFIX = "com.xforceplus.ultraman.oqsengine.changelog.tx.";

    private ChangelogHandler changelogHandler;

    private MetaManager metaManager;

    @Resource
    private EventBus eventBus;

    public RedisEventLifecycleHandler(RedisClient redisClient, ChangelogHandler changelogHandler, ObjectMapper mapper) {
        this.redisClient = redisClient;
        this.syncCommands = redisClient.connect().sync();
        this.changelogHandler = changelogHandler;
        this.mapper = mapper;
    }

    @PostConstruct
    public void init(){
        eventBus.watch(EventType.ENTITY_BUILD, x -> {
            this.onEntityCreate((ActualEvent<BuildPayload>) x);
        });

        eventBus.watch(EventType.ENTITY_DELETE, x -> {
            this.onEntityDelete((ActualEvent<DeletePayload>) x);
        });

        eventBus.watch(EventType.ENTITY_REPLACE, x -> {
            this.onEntityUpdate((ActualEvent<ReplacePayload>) x);
        });

        eventBus.watch(EventType.TX_PREPAREDNESS_COMMIT, x -> {
            this.onTxPreCommit((ActualEvent<CommitPayload>) x);
        });

        eventBus.watch(EventType.TX_BEGIN, x -> {
            this.onTxCreate((ActualEvent<BeginPayload>) x);
        });

        eventBus.watch(EventType.TX_COMMITED, x -> {
            this.onTxCommitted((ActualEvent<CommitPayload>) x);
        });

        eventBus.watch(EventType.TX_PREPAREDNESS_ROLLBACK, x -> {
            this.onTxPreRollBack((ActualEvent<RollbackPayload>) x);
        });

        eventBus.watch(EventType.TX_ROLLBACKED, x -> {
            this.onTxRollBack((ActualEvent<RollbackPayload>) x);
        });
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
            long txId = createPayload.getTxId();
            pushQueue(txId, entityToChangedEvent(createPayload.getEntity()));
        });
    }

    @Override
    public void onEntityUpdate(ActualEvent<ReplacePayload> update) {
        extract(update, updatePayload -> {
            long txId = updatePayload.getTxId();
            pushQueue(txId, entityToChangedEvent(updatePayload.getEntity()));
        });
    }

    /**
     * TODO
     *
     * @param delete
     */
    @Override
    public void onEntityDelete(ActualEvent<DeletePayload> delete) {
//        extract(delete, deletePayload -> {
//            long txId = deletePayload.getTxId();
//            pushQueue(txId, entityToChangedEvent(deletePayload.getEntity()));
//        });

        //TODO if delete should consider in changelog
    }

    @Override
    public void onTxPreCommit(ActualEvent<CommitPayload> preCommit) {
        logger.info(NO_OPERATION, preCommit.type());
    }

    @Override
    public void onTxCommitted(ActualEvent<CommitPayload> committed) {
        //trigger a combine
        extract(committed, commitPayload -> {
            long txId = commitPayload.getTxId();
            long commitId = commitPayload.getCommitId();
            long time = committed.time();
            Optional<String> msg = commitPayload.getMsg();
            List<ChangedEvent> changedEvents = popQueue(txId);
            TransactionalChangelogEvent changelogEvent = toTransactionalChangelogEvent(commitId, time, msg.orElse(""), changedEvents);
            changelogHandler.handle(changelogEvent);
        });
    }

    @Override
    public void onTxPreRollBack(ActualEvent<RollbackPayload> preRollBack) {
        logger.info(NO_OPERATION, preRollBack.type());
    }

    @Override
    public void onTxRollBack(ActualEvent<RollbackPayload> preRollBack) {
        extract(preRollBack, payload -> {
            long txId = payload.getTxId();
            dropQueue(txId);
        });
    }

    /**
     * entity to Changed event
     *
     * @param entity
     * @return
     */
    private ChangedEvent entityToChangedEvent(IEntity entity) {
        return null;
    }


    /**
     * TODO which queue
     * create the tx queue
     */
    private void createQueueIfNotExists(long txId) {
        String queue = QUEUE_PREFIX.concat(Long.toString(txId));
        if(syncCommands.llen(queue) == 0) {
            syncCommands.rpush(queue, "");
        }
    }

    /**
     * popA queue and get a TransactionalChangelog
     * changedEvent within a transaction will have differenct id we should make sure one changelog only associated with one commit id
     *
     * @return
     */
    private TransactionalChangelogEvent toTransactionalChangelogEvent(long commitId, long timestamp, String comment, List<ChangedEvent> changedEventList) {
        TransactionalChangelogEvent transactionalChangelogEvent = new TransactionalChangelogEvent();
        transactionalChangelogEvent.setCommitId(commitId);

        Map<Long, Optional<ChangedEvent>> groupedEvent = changedEventList
                .stream()
                .collect(Collectors.groupingBy(x -> x.getId(), Collectors.reducing((prev, next) -> {

                    ChangedEvent changedEvent = new ChangedEvent();
                    changedEvent.setEntityClassId(prev.getEntityClassId());
                    changedEvent.setComment(comment);
                    changedEvent.setUsername(getUsername(prev));
                    changedEvent.setTimestamp(timestamp);
                    Map<Long, ValueWrapper> prevValueMap = prev.getValueMap();
                    Map<Long, ValueWrapper> nextValueMap = next.getValueMap();

                    /**
                     * casue here only override value
                     */
                    Map<Long, ValueWrapper> newValueMap = new HashMap<>(prevValueMap);
                    newValueMap.putAll(nextValueMap);

                    changedEvent.setValueMap(newValueMap);
                    return next;
                })));


        List<ChangedEvent> mergedChangeEvent = groupedEvent.values().stream().peek(x -> {
            if (!x.isPresent()) {
                logger.error("Got corrupted Change log but we can do nothing on it T T");
            }
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());


        transactionalChangelogEvent.setChangedEventList(mergedChangeEvent);
        return transactionalChangelogEvent;
    }

    private String getUsername(ChangedEvent prev){
        if(!StringUtils.isEmpty(prev.getUsername())) {
            return prev.getUsername();
        }else{

            Map<Long, ValueWrapper> valueMap = prev.getValueMap();

            Optional<IEntityClass> targetEntityClass = metaManager.load(prev.getEntityClassId());
            if(targetEntityClass.isPresent()){
                IEntityClass entityClass = targetEntityClass.get();
                Optional<IEntityField> createUserNameField = entityClass.field("create_user_name");
                Optional<Object> createUserName = createUserNameField.map(x -> {
                    ValueWrapper valueWrapper = valueMap.get(x.id());
                    if (valueWrapper != null) {
                        return valueWrapper.getValue();
                    }
                    return null;
                });
                Optional<IEntityField> updateUserNameField = entityClass.field("update_user_name");
                Optional<Object> updateUserName = updateUserNameField.map(x -> {
                    ValueWrapper valueWrapper = valueMap.get(x.id());
                    if (valueWrapper != null) {
                        return valueWrapper.getValue();
                    }
                    return null;
                });

                if(updateUserName.isPresent()){
                    return updateUserName.get().toString();
                }

                if(createUserName.isPresent()){
                    return createUserName.get().toString();
                }

            } else {
                logger.error("Cannot find related entityClass {}", prev.getEntityClassId());
            }

            return null;
        }
    }

    private List<ChangedEvent> popQueue(long txId) {

        String queue = QUEUE_PREFIX.concat(Long.toString(txId));
        List<String> orderedList = syncCommands.lrange(queue, 0, -1);

        List<ChangedEvent> changedEvents = orderedList.stream().filter(x -> StringUtils.isEmpty(x)).map(x -> {
            try {
                ChangedEvent changedEvent = mapper.readValue(x, ChangedEvent.class);
                return changedEvent;
            } catch (JsonProcessingException e) {
                logger.error("{}", e);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        return changedEvents;
    }

    private void pushQueue(long txId, ChangedEvent changedEvent) {
        String queue = QUEUE_PREFIX.concat(Long.toString(txId));
        try {
            String payloadStr = mapper.writeValueAsString(changedEvent);
            syncCommands.rpushx(queue, payloadStr);
        } catch (JsonProcessingException e) {
            logger.error("{}", e);
        }
    }

    /**
     * remove queue in redis
     *
     * @param txId
     */
    private void dropQueue(long txId) {
        String queue = QUEUE_PREFIX.concat(Long.toString(txId));
        syncCommands.del(queue);
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
