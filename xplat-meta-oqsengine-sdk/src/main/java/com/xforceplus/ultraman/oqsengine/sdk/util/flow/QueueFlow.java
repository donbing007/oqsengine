package com.xforceplus.ultraman.oqsengine.sdk.util.flow;

import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import io.vavr.Tuple2;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class QueueFlow<T> {

    private SourceQueueWithComplete<Tuple2<CompletableFuture<T>, Supplier<T>>> queue;

    private ActorMaterializer mat;

    public QueueFlow(String name, ActorMaterializer mat) {

        this.mat = mat;

        queue = Source.<Tuple2<CompletableFuture<T>, Supplier<T>>>queue(100, OverflowStrategy.backpressure())
                //.throttle(1, Duration.ofMillis(30))
                .map(x -> {
                    try {
                        T t = x._2().get();
                        //fill result
                        x._1().complete(t);

                    }catch(Exception ex){
                        if(x._1() != null){
                            x._1().completeExceptionally(ex);
                        }
                    }
                    return x;
                })
                .log(name)
                .to(Sink.ignore())
                .run(mat);
    }

    public void feed(Tuple2<CompletableFuture<T>, Supplier<T>> pairSupplier) {
        Source.single(pairSupplier).map(x -> queue.offer(x))
                .runWith(Sink.ignore(), mat);
    }

    public void close() {
        queue.complete();
    }
}
