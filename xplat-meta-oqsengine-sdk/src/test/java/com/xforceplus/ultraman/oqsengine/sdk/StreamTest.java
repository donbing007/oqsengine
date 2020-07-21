package com.xforceplus.ultraman.oqsengine.sdk;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.QueueOfferResult;
import akka.stream.javadsl.*;
import akka.util.ByteString;
import io.reactivex.Observable;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.junit.Before;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class StreamTest {

    private ActorSystem actorSystem;

    private ActorMaterializer mat;

    @Before
    public void setup() {
        actorSystem = ActorSystem.create();
        mat = ActorMaterializer.create(actorSystem);
    }



    @Test
    public void queueStream() throws InterruptedException {
        int bufferSize = 10;
        int elementsToProcess = 5;

        Tuple2<SourceQueueWithComplete<Integer>, Publisher<Integer>> run = Source.<Integer>queue(bufferSize, OverflowStrategy.backpressure())
                .throttle(elementsToProcess, Duration.ofSeconds(3))
                .map(x -> x * x)
                .toMat(Sink.asPublisher(AsPublisher.WITHOUT_FANOUT), Tuple::of).run(mat);

        SourceQueueWithComplete<Integer> sourceQueue = run._1();
        Publisher<Integer> integerPublisher = run._2();

        Source<Integer, NotUsed> source = Source.single(1);
        Source<Integer, NotUsed> source2 = Source.single(1);

        CompletionStage<CompletionStage<QueueOfferResult>> run1 = source.map(x -> sourceQueue.offer(x)).toMat(Sink.head(), Keep.right()).run(mat);
        source2.map(x -> sourceQueue.offer(x)).runWith(Sink.ignore(), mat);

        //入队而已
        run1.toCompletableFuture().join();

    }

    @Test
    public void test() throws IOException, InterruptedException, TimeoutException {

        ActorMaterializer mat = ActorMaterializer.create(actorSystem);

//        MessageDispatcher blocingDispatcher = actorSystem.dispatchers().lookup("my-dispatcher");

        List<String> list = Arrays.asList("a", "b");

        CountDownLatch latch = new CountDownLatch(1);

        Source.from(list).map(ByteString::fromString)
                .runWith(StreamConverters.asInputStream().mapMaterializedValue(x -> {

                    return CompletableFuture.supplyAsync(() -> {
                        InputStreamReader isReader = new InputStreamReader(x);
                        //Creating a BufferedReader object
                        BufferedReader reader = new BufferedReader(isReader);
                        StringBuffer sb = new StringBuffer();
                        String str = null;
                        while (true) {
                            try {
                                if (!((str = reader.readLine()) != null)) break;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            sb.append(str);
                        }

                        System.out.println(sb.toString());
                        latch.countDown();
                        return 1;
                    });
                }), mat);

        latch.await();

    }
}
