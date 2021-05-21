package com.xforceplus.ultraman.oqsengine.synchronizer.server.state;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.xforceplus.ultraman.oqsengine.sdk.LockRequest;
import com.xforceplus.ultraman.oqsengine.sdk.LockResponse;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.CriticalResource;
import com.xforceplus.ultraman.oqsengine.synchronizer.server.thread.MockThread;
import io.vavr.control.Either;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * mock state for test server.
 */
public class MockState {
    //TODO redis
    Map<CriticalResource, Integer> countMapping = new ConcurrentHashMap<>();

    //
    Map<CriticalResource, MockThread> nodeMapping = new ConcurrentHashMap<>();

    Map<String, MockThread> threadNodeMap = new ConcurrentHashMap<>();

    //TODO current mock is not aware interrupted thread
    Map<CriticalResource, List<MockThread>> queue = new ConcurrentHashMap<>();

    Map<MockThread, ActorRef> channelMapping = new ConcurrentHashMap<>();


    Map<String, ActorRef> nodeChannelMapping = new ConcurrentHashMap<>();

    private ActorSystem system;

    public MockState(ActorSystem system) {
        this.system = system;
    }

    public ActorRef getNodeChannel(String node) {
        return nodeChannelMapping.get(node);
    }

    public void initNodeChannel(String node, ActorRef channel) {
        nodeChannelMapping.put(node, channel);
    }

    public void addWaiter(CriticalResource resource, MockThread mockThread, String node) {

        ActorRef channel = nodeChannelMapping.get(node);
        channelMapping.put(mockThread, channel);

        queue.compute(resource, (k, v) -> {
            if (v == null) {
                v = new LinkedList<>();

            }

            v.add(mockThread);

            System.out.println("currennt node:" + v);

            return v;
        });
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

    synchronized public Boolean tryRelease(List<CriticalResource> criticalResourceList, MockThread current) {


        System.out.println(current + "try to release");


        List<CriticalResource> criticalList = criticalResourceList
                .stream()
                .sorted()
                .collect(Collectors.toList());


        boolean retVal = true;

        for (CriticalResource resource : criticalList) {
            MockThread threadNode = nodeMapping.get(resource);
            if (threadNode == null) {
                //warning !
                System.out.println("warning try to release a not self");
            } else {
                if (threadNode.equals(current)) {
                    countMapping.computeIfPresent(resource, (k, v) -> {
                        return v - 1;
                    });
                    if (countMapping.get(resource) != 0) {
                        retVal = false;
                    } else {
                        countMapping.remove(resource);
                        nodeMapping.remove(resource);

                        //TODO notify
                        unParkingNext(resource, current);
                    }
                } else {
                    System.out.println("warning try to release a not self");
                }
            }
        }

        return retVal;
    }

    private void unParkingNext(CriticalResource resource, MockThread current) {

        System.out.println("Do Unparking ");

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

            System.out.println("Do Unparking： " + nextThread);

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

    synchronized private boolean isHead(CriticalResource resource, MockThread current) {
        List<MockThread> mockThreads = queue.get(resource);
        if (mockThreads != null && !mockThreads.isEmpty()) {

            System.out.println("current queue for resource is" + queue);

            return mockThreads.get(0).equals(current);
        }

        return true;
    }

    synchronized public Either<CriticalResource, Boolean> tryAcquire(List<CriticalResource> criticalResourceList, MockThread current) {

        System.out.println(current + "try to acquire");

        List<CriticalResource> criticalList = criticalResourceList
                .stream()
                .sorted()
                .collect(Collectors.toList());

        List<CriticalResource> footprint = new LinkedList<>();

        Either<CriticalResource, Boolean> retVal = Either.right(true);

        for (CriticalResource resource : criticalList) {

            if (isHead(resource, current)) {


                System.out.println(current + "is head");
                System.out.println("current" + queue.get(resource));
                System.out.println("current thread is " + nodeMapping.get(resource));

                MockThread threadNode = nodeMapping.get(resource);
                if (threadNode == null) {
                    nodeMapping.put(resource, current);
                    countMapping.put(resource, 1);
                    footprint.add(resource);
                } else {
                    if (threadNode.equals(current)) {
                        countMapping.computeIfPresent(resource, (k, v) -> {
                            return v + 1;
                        });
                    } else {
                        //failed;

                        System.out.println(current + " got lock failed");

                        revertFootprint(footprint);
                        retVal = Either.left(resource);
                        break;
                    }
                }
            } else {
                System.out.println(current + "is not head");
                revertFootprint(footprint);
                retVal = Either.left(resource);
                break;
            }
        }

        return retVal;
    }

    private void revertFootprint(List<CriticalResource> footprint) {
        footprint.forEach(x -> {
            countMapping.remove(x);
            nodeMapping.remove(x);
        });
    }
}
