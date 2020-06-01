package com.xforceplus.ultraman.oqsengine.sdk.store;

import com.xforceplus.ultraman.metadata.grpc.DictUp;
import com.xforceplus.ultraman.metadata.grpc.DictUpDetail;
import com.xforceplus.ultraman.metadata.grpc.DictUpInfo;
import com.xforceplus.ultraman.metadata.grpc.DictUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import org.junit.Test;

import java.util.Arrays;

public class DictMapLocalStoreTest {

    @Test
    public void saveTest(){
        DictMapLocalStore store = DictMapLocalStore.create();
        DictUpResult result = DictUpResult
                .newBuilder()
                .addAllDicts(Arrays.asList(
                        DictUpInfo
                                .newBuilder()
                                .addAllDictUpDetails(Arrays.asList(DictUpDetail
                                        .newBuilder()
                                        .setCode("A")
                                        .setName("B")
                                        .build()))
                                .build()
                ))
                .build();

        store.save(result, "0");
        store.query().selectAll().execute().toRows().forEach(System.out::println);
    }
}
