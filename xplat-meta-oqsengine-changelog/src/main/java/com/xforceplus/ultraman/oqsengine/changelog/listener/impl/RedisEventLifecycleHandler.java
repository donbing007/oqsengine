package com.xforceplus.ultraman.oqsengine.changelog.listener.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.ChangelogHandler;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangedEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.TransactionalChangelogEvent;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ValueWrapper;
import com.xforceplus.ultraman.oqsengine.changelog.listener.EventLifecycleAware;
import com.xforceplus.ultraman.oqsengine.changelog.listener.flow.FlowRegistry;
import com.xforceplus.ultraman.oqsengine.changelog.listener.flow.QueueFlow;
import com.xforceplus.ultraman.oqsengine.changelog.utils.ChangelogHelper;
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
import io.vavr.Tuple;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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

    private FlowRegistry flowRegistry;

    @Resource
    private EventBus eventBus;

    public RedisEventLifecycleHandler(RedisClient redisClient, ChangelogHandler changelogHandler, ObjectMapper mapper, FlowRegistry flowRegistry, MetaManager manager) {
        this.redisClient = redisClient;
        this.syncCommands = redisClient.connect().sync();
        this.changelogHandler = changelogHandler;
        this.mapper = mapper;
        this.flowRegistry = flowRegistry;
        this.metaManager = manager;
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
        logger.debug("Got tx create");
        extract(begin, payload -> {
            long txId = payload.getTxId();
            String msg = payload.getMsg();
            logger.debug("Got message {}", msg);
            QueueFlow<Void> flow = flowRegistry.flow(Long.toString(txId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            flow.feed(Tuple.of(future, () -> {
                createQueueIfNotExists(txId, msg);
                return null;
            }));
        });
    }

    @Override
    public void onEntityCreate(ActualEvent<BuildPayload> create) {
        logger.debug("Got entity create");
        extract(create, createPayload -> {
            long txId = createPayload.getTxId();
            QueueFlow<Void> flow = flowRegistry.flow(Long.toString(txId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            flow.feed(Tuple.of(future, () -> {
                pushQueue(txId, entityToChangedEvent(createPayload.getEntity()));
                return null;
            }));
        });
    }

    @Override
    public void onEntityUpdate(ActualEvent<ReplacePayload> update) {
        logger.debug("Got entity update");
        extract(update, updatePayload -> {
            long txId = updatePayload.getTxId();
            QueueFlow<Void> flow = flowRegistry.flow(Long.toString(txId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            flow.feed(Tuple.of(future, () -> {
                pushQueue(txId, entityToChangedEvent(updatePayload.getEntity()));
                return null;
            }));
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

        logger.debug("Got entity delete");
        //TODO if delete should consider in changelog
    }

    @Override
    public void onTxPreCommit(ActualEvent<CommitPayload> preCommit) {
        logger.debug(NO_OPERATION, preCommit.type());
    }

    @Override
    public void onTxCommitted(ActualEvent<CommitPayload> committed) {

        logger.debug("Got tx committed");

        //trigger a combine
        extract(committed, commitPayload -> {
            long txId = commitPayload.getTxId();
            long commitId = commitPayload.getCommitId();
            long time = committed.time();

            QueueFlow<Void> flow = flowRegistry.flow(Long.toString(txId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            flow.feed(Tuple.of(future, () -> {
                List<ChangedEvent> changedEvents = popQueue(txId);
                if(changedEvents.size() > 0) {
                    String comment = changedEvents.get(0).getComment();
                    TransactionalChangelogEvent changelogEvent = toTransactionalChangelogEvent(commitId, time, Optional.ofNullable(comment).orElse(""), changedEvents);
                    changelogHandler.handle(changelogEvent);
                }
                dropQueue(txId);
                return null;
            }));
        });
    }

    @Override
    public void onTxPreRollBack(ActualEvent<RollbackPayload> preRollBack) {
        logger.debug(NO_OPERATION, preRollBack.type());
    }

    @Override
    public void onTxRollBack(ActualEvent<RollbackPayload> preRollBack) {
        logger.debug("Got tx rollback");
        extract(preRollBack, payload -> {
            long txId = payload.getTxId();
            QueueFlow<Void> flow = flowRegistry.flow(Long.toString(txId));
            CompletableFuture<Void> future = new CompletableFuture<>();
            flow.feed(Tuple.of(future, () -> {
                dropQueue(txId);
                return null;
            }));
        });
    }

    /**
     * entity to Changed event
     *
     * @param entity
     * @return
     */
    private ChangedEvent entityToChangedEvent(IEntity entity) {
        ChangedEvent changedEvent = new ChangedEvent();
        changedEvent.setEntityClassId(entity.entityClassRef().getId());
        changedEvent.setTimestamp(entity.time());
        changedEvent.setId(entity.id());
        Map<Long, ValueWrapper> valueMap = new HashMap<>();
        changedEvent.setValueMap(valueMap);
        entity.entityValue().values().stream().forEach(x -> {
            ValueWrapper valueWrapper = new ValueWrapper(ChangelogHelper.serialize(x), x.getField().type(), x.getField().id());
            valueMap.put(x.getField().id(), valueWrapper);
        });
        return changedEvent;
    }


    /**
     * TODO which queue
     * create the tx queue
     */
    private void createQueueIfNotExists(long txId, String msg) {
        String queue = QUEUE_PREFIX.concat(Long.toString(txId));
        if(syncCommands.llen(queue) == 0) {
            logger.debug("create tx {} with {}", txId, msg);
            syncCommands.rpush(queue, msg);
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

        //update commitId
        changedEventList.forEach( x -> x.setCommitId(commitId));


        Map<Long, Optional<ChangedEvent>> groupedEvent = changedEventList
                .stream()
                .collect(Collectors.groupingBy(x -> x.getId(), Collectors.reducing((prev, next) -> {

                    ChangedEvent changedEvent = new ChangedEvent();
                    changedEvent.setEntityClassId(prev.getEntityClassId());
                    changedEvent.setComment(comment);
                    changedEvent.setUsername(getUsername(prev));
                    changedEvent.setTimestamp(timestamp);
                    changedEvent.setCommitId(commitId);
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

        String comment = "";
        if(orderedList.size() > 0){
            comment = orderedList.get(0);
        }

        logger.debug("comment in {} is {}", txId, comment);

        String finalComment = comment;
        List<ChangedEvent> changedEvents = orderedList.stream().skip(1).map(x -> {
            try {
                ChangedEvent changedEvent = mapper.readValue(x, ChangedEvent.class);
                changedEvent.setComment(finalComment);
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
