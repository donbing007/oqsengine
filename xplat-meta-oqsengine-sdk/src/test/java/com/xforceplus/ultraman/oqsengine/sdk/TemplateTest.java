package com.xforceplus.ultraman.oqsengine.sdk;

import com.github.benmanes.caffeine.cache.*;
import com.xforceplus.ultraman.oqsengine.sdk.util.flow.QueueFlow;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.stringtemplate.v4.*;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class TemplateTest {

    @Test
    public void testTemplate(){

        String template = "<a>$name$</a>";
        ST st = new ST(template,'$','$');
        st.add("name", "World");
        assertEquals("<a>World</a>", st.render());
    }


    @Test
    public void optional(){
        //optional
        Optional.of("5").map(x -> null).ifPresent(x -> System.out.println("OK"));
    }

    @Test
    public void cache() throws InterruptedException {

//        System.out.println(Thread.currentThread());
//
//        CountDownLatch latch = new CountDownLatch(1);
//
//        LoadingCache<String, String> graphs = Caffeine.newBuilder()
//                .maximumSize(1000)
////                .expireAfter(new Expiry<String, String>() {
////                    @Override
////                    public long expireAfterCreate(@NonNull String key, @NonNull String value, long currentTime) {
////                        return  TimeUnit.SECONDS.toNanos(1);
////                    }
////
////                    @Override
////                    public long expireAfterUpdate(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
////                        return TimeUnit.SECONDS.toNanos(1);
////                    }
////
////                    @Override
////                    public long expireAfterRead(@NonNull String key, @NonNull String value, long currentTime, @NonNegative long currentDuration) {
////                        return TimeUnit.SECONDS.toNanos(1);
////                    }
////                })
//                .expireAfterWrite(5, TimeUnit.SECONDS)
////                .expireAfterAccess(1, TimeUnit.SECONDS)
//                .removalListener(new RemovalListener<String, String>() {
//                    @Override
//                    public void onRemoval(@Nullable String s, @Nullable String queueFlow, @NonNull RemovalCause removalCause) {
//
//                        System.out.println(Thread.currentThread());
//                        System.out.println("Flow for is over due to time expiry");
//                        latch.countDown();
//                    }
//                })
//                .build(key -> {
//                    System.out.println("Flow for {} is setup");
//                    return key;
//                });
//
//
//        graphs.get("abc");
//
//        latch.await();
    }
}
