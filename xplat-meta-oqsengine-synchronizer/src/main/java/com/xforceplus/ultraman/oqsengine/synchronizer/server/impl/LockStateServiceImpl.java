package com.xforceplus.ultraman.oqsengine.synchronizer.server.impl;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.sdk.CriticalRes;
import com.xforceplus.ultraman.oqsengine.sdk.LockRequest;
import com.xforceplus.ultraman.oqsengine.sdk.LockResponse;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.LockStateService;
import com.xforceplus.xplat.galaxy.grpc.MessageSource;
import com.xforceplus.xplat.galaxy.grpc.actor.ChannelActor;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.redisson.OqsLock;
import org.redisson.OqsMultiLock;
import org.redisson.PubSubMessageListener;
import org.redisson.client.ChannelName;
import org.redisson.client.codec.LongCodec;
import org.redisson.command.CommandSyncService;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;
import org.redisson.pubsub.AsyncSemaphore;
import org.redisson.pubsub.LockPubSub;
import org.redisson.pubsub.PublishSubscribeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lock state Service implemention.
 */
public class LockStateServiceImpl implements LockStateService {

    @Resource
    private ActorSystem system;

    private Map<String, ActorRef> channelMapping = new ConcurrentHashMap<>();

    private CommandSyncService commandSyncService;

    private PublishSubscribeService pubsub;

    private Logger logger = LoggerFactory.getLogger(LockStateService.class);

    /**
     * listened topic.
     */
    private Set<String> channels = new ConcurrentSkipListSet<>();

    public LockStateServiceImpl(CommandSyncService commandSyncService) {
        this.commandSyncService = commandSyncService;
        this.pubsub = commandSyncService.getConnectionManager().getSubscribeService();
    }

    /**
     * setup node communication.
     *
     * @param in   user input
     * @param node node name
     */
    @Override
    public Source<LockResponse, NotUsed> setupCommunication(Source<LockRequest, NotUsed> in, String node) {
        logger.debug("Setup communication with {}", node);

        ActorRef channel = system.actorOf(Props.create(ChannelActor.class, LockResponse.class));
        channelMapping.put(node, channel);
        MessageSource<LockResponse> source = new MessageSource<>(channel, LockResponse.class);

        return Source.fromGraph(source).log("what ???");
    }

    @Override
    public CompletionStage<LockResponse> tryAcquire(LockRequest in, String node) {
        List<CriticalRes> criticalResList = in.getCResList();
        List<OqsLock> locks = criticalResList.stream().map(x -> {
            return Long.parseLong(x.getId());
        }).sorted().map(x -> new OqsLock(commandSyncService, x.toString())).collect(Collectors.toList());

        boolean gotLock = false;

        OqsMultiLock oqsMultiLock = new OqsMultiLock(locks);
        try {
            //TODO
            gotLock = oqsMultiLock.tryLock(in.getUuid(), -1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (gotLock) {
            logger.debug("got lock {}", in);
            return CompletableFuture.completedFuture(LockResponse.newBuilder()
                .setUuid(in.getUuid())
                .setRespType(LockResponse.ResponseType.LOCKED).build());
        } else {
            logger.debug("failed to get lock {}", in);
            return CompletableFuture.completedFuture(LockResponse.newBuilder()
                .setUuid(in.getUuid())
                .setRespType(LockResponse.ResponseType.ERR).setMessage("lock not acquired").build());
        }
    }

    @Override
    public CompletionStage<LockResponse> tryRelease(LockRequest in, String node) {

        List<CriticalRes> criticalResList = in.getCResList();
        List<OqsLock> locks = criticalResList.stream().map(x -> {
            return new OqsLock(commandSyncService, x.getId());
        }).collect(Collectors.toList());

        return new OqsMultiLock(locks).unlockAsync(in.getUuid()).toCompletableFuture().thenApply(x -> {
            return LockResponse.newBuilder().setRespType(LockResponse.ResponseType.RELEASED).setUuid(in.getUuid())
                .build();
        });
    }

    @Override
    public CompletionStage<LockResponse> addWaiter(LockRequest in, String node) {

        List<CriticalRes> criticalResList = in.getCResList();

        List<OqsLock> locks = criticalResList.stream().map(x -> {
            return new OqsLock(commandSyncService, x.getId());
        }).collect(Collectors.toList());

        RPromise<Void> result = new RedissonPromise<Void>();

        boolean b = false;
        try {
            b = new OqsMultiLock(locks).tryLock(in.getUuid(), -1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (b) {
            return CompletableFuture.completedFuture(LockResponse.newBuilder()
                .setRespType(LockResponse.ResponseType.LOCKED).setUuid(in.getUuid()).build());
        } else {
            subscribe(locks);
            return CompletableFuture
                .completedFuture(
                    LockResponse.newBuilder().setRespType(LockResponse.ResponseType.ERR).setUuid(in.getUuid()).build());
        }
    }

    @Override
    public OqsLock createLock(String name) {
        return new OqsLock(commandSyncService, name);
    }

    private void subscribe(List<OqsLock> locks) {

        locks.forEach(lock -> {

            String channelName = lock.getChannelName();

            if (!channels.contains(channelName)) {

                logger.debug("subscribe channelName {}", channelName);
                ChannelName channelNameTyped = new ChannelName(channelName);
                AsyncSemaphore semaphore = pubsub.getSemaphore(channelNameTyped);

                pubsub.subscribe(LongCodec.INSTANCE, channelName, semaphore, new PubSubMessageListener<Long>(Long.class,
                    (channel, msg) -> {

                        if (msg.equals(LockPubSub.UNLOCK_MESSAGE)) {
                            LockResponse response = LockResponse.newBuilder().setRespType(
                                LockResponse.ResponseType.RELEASED)
                                .setCRes(CriticalRes.newBuilder().setType(CriticalRes.ResourceType.ID)
                                    .setId(lock.getName()).build()).build();
                            broadcast(response);
                        }
                    }, channelName));
                channels.add(channelName);
            }
        });
    }

    private void broadcast(LockResponse response) {
        channelMapping.values().forEach(x -> {
            x.tell(response, ActorRef.noSender());
        });
    }
}

