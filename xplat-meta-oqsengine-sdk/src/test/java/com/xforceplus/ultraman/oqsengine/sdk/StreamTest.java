package com.xforceplus.ultraman.oqsengine.sdk;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.StreamConverters;
import akka.util.ByteString;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class StreamTest {

    private ActorSystem actorSystem;

    @Before
    public void setup(){
        actorSystem = ActorSystem.create();

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
