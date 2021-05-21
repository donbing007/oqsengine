package com.xforceplus.ultraman.oqsengine.synchronizer.server.state;

import akka.actor.ActorRef;
import com.xforceplus.ultraman.oqsengine.sdk.LockRequest;
import com.xforceplus.ultraman.oqsengine.sdk.LockResponse;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.thread.MockThread;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.redisson.OqsLock;
import org.redisson.RedissonLockEntry;
import org.redisson.api.RFuture;
import org.redisson.api.RLock;
import org.redisson.api.RLockAsync;
import org.redisson.command.CommandSyncService;
import org.redisson.config.Config;
import org.redisson.config.ConfigSupport;
import org.redisson.connection.ConnectionManager;
import org.redisson.liveobject.core.RedissonObjectBuilder;
import org.redisson.misc.RPromise;
import org.redisson.misc.RedissonPromise;
import org.redisson.pubsub.LockPubSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * redis state.
 */
public class RedisState implements CritialResourceState {

    private Logger logger = LoggerFactory.getLogger(RedisState.class);

    private CommandSyncService commandExecutor;

    Map<MockThread, ActorRef> channelMapping = new ConcurrentHashMap<>();
    Map<String, ActorRef> nodeChannelMapping = new ConcurrentHashMap<>();
    Map<String, MockThread> threadNodeMap = new ConcurrentHashMap<>();
    Map<CriticalResource, List<MockThread>> queue = new ConcurrentHashMap<>();

    protected final LockPubSub pubSub;

    public ActorRef getNodeChannel(String node) {
        return nodeChannelMapping.get(node);
    }

    public void initNodeChannel(String node, ActorRef channel) {
        nodeChannelMapping.put(node, channel);
    }

    public MockThread getOrError(LockRequest lockRequest) {
        String uuid = lockRequest.getUuid();
        MockThread threadNode = threadNodeMap.get(uuid);
        if (threadNode == null) {
            throw new RuntimeException("No such Thread");
        }
        return threadNode;
    }

    public MockThread getOrCreate(LockRequest lockRequest) {

        String uuid = lockRequest.getUuid();
        MockThread threadNode = threadNodeMap.get(uuid);
        if (threadNode == null) {
            //TODO
            threadNode = new MockThread(uuid);
            threadNodeMap.put(uuid, threadNode);
        }

        return threadNode;
    }

    /**
     * new configuration
     *
     * @param redisAddress
     */
    public RedisState(String redisAddress) {
        Config config = new Config();
        config.useSingleServer().setAddress(redisAddress);
        Config configCopy = new Config(config);
        ConnectionManager connectionManager = ConfigSupport.createConnectionManager(configCopy);
        RedissonObjectBuilder objectBuilder = null;
        commandExecutor = new CommandSyncService(connectionManager, objectBuilder);
        this.pubSub = commandExecutor.getConnectionManager().getSubscribeService().getLockPubSub();
    }

    public OqsLock create(String key) {
        return new OqsLock(commandExecutor, key);
    }


    public void addWaiter(CriticalResource resource, MockThread mockThread, String node) {
        ActorRef channel = nodeChannelMapping.get(node);
        channelMapping.put(mockThread, channel);

        RPromise<Void> result = new RedissonPromise<Void>();

        RFuture<RedissonLockEntry> subscribe = create(resource.getRes().toString()).subscribe();

        subscribe.onComplete((res, ex) -> {
            if (ex != null) {
                result.tryFailure(ex);
                return;
            }

            //register

            RedissonLockEntry entry = res;

            res.addListener(() -> {
                logger.debug("Trigger listener");
            });
        });

        commandExecutor.get(subscribe);
    }

    @Override
    public Either<CriticalResource, Boolean> tryAcquire(List<CriticalResource> criticalResourceList, MockThread current) {

        logger.debug("Try acquire" + current);

        List<Tuple2<CriticalResource, OqsLock>> oqsLocks = criticalResourceList.stream().sorted().map(x -> {
            String s = x.getRes().toString();
            return Tuple.of(x, create(s));
        }).collect(Collectors.toList());

        List<OqsLock> locks = new LinkedList<>();
        for (Tuple2<CriticalResource, OqsLock> lock : oqsLocks) {
            if (lock._2().tryLock(current.getUuid())) {
                locks.add(lock._2());
            } else {
                for (OqsLock oqsLock : locks) {
                    oqsLock.unlock(current.getUuid());
                }

                return Either.left(lock._1());
            }
        }

        logger.debug("Got lock" + current);
        return Either.right(true);
    }

    protected long calcLockWaitTime(long remainTime) {
        return remainTime;
    }

    protected int failedLocksLimit() {
        return 0;
    }

    protected void unlockInner(Collection<RLock> locks) {
        locks.stream()
                .map(RLockAsync::unlockAsync)
                .forEach(RFuture::awaitUninterruptibly);
    }

    public Boolean tryRelease(List<CriticalResource> criticalResourceList, MockThread current) {

        List<Tuple2<CriticalResource, OqsLock>> oqsLocks = criticalResourceList.stream().sorted().map(x -> {
            String s = x.getRes().toString();
            return Tuple.of(x, create(s));
        }).collect(Collectors.toList());


        if (oqsLocks.isEmpty()) {
            return true;
        }

        RPromise<Boolean> result = new RedissonPromise<Boolean>();
        AtomicInteger counter = new AtomicInteger(oqsLocks.size());

        for (Tuple2<CriticalResource, OqsLock> lock : oqsLocks) {
            lock._2().unlockAsync(current.getUuid()).onComplete((res, e) -> {
                if (e != null) {
                    result.tryFailure(e);
                    return;
                }

                if (counter.decrementAndGet() == 0) {
                    logger.debug("release is ok");
                    result.trySuccess(true);
                }
            });
        }

        return commandExecutor.get(result);
    }

    private void unParkingNext(CriticalResource resource, MockThread current) {

        logger.debug("Do Unparking ");

        List<MockThread> mockThreads = queue.get(resource);
        if (mockThreads != null && !mockThreads.isEmpty()) {

            MockThread head = mockThreads.get(0);
            MockThread nextThread;
            if (!current.equals(head)) {
                nextThread = mockThreads.get(0);
            } else {
                mockThreads.remove(0);
                nextThread = mockThreads.get(0);
            }

            logger.debug("Do Unparking： " + nextThread);

            ActorRef channel = channelMapping.get(nextThread);
            //TODO retry to send

            if (channel != null) {
                channel.tell(LockResponse.newBuilder()
                        .setRespType(LockResponse.ResponseType.UNPARKING)
                        .setUuid(nextThread.getUuid())
                        .build(), ActorRef.noSender());
            }
        }
    }
}
