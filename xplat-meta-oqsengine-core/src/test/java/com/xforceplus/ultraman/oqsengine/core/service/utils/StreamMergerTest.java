package com.xforceplus.ultraman.oqsengine.core.service.utils;

import static java.util.stream.Collectors.toList;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/**
 * StreamMerger Tester.
 *
 * @author dongbin
 * @version 1.0 12/03/2020
 * @since <pre>Dec 3, 2020</pre>
 */
public class StreamMergerTest {

    @Test
    public void mergeTest() {
        List<EntityRef> masterRefs = new LinkedList<>();

        AtomicLong al = new AtomicLong(0);

        EntityRef entityRef = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("-0.46").build();
        masterRefs.add(entityRef);

        EntityRef entityRef2 = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("-0.32").build();
        masterRefs.add(entityRef2);

        EntityRef entityRef3 = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("0.32").build();
        masterRefs.add(entityRef3);

        EntityRef entityRef4 = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("0.46").build();
        masterRefs.add(entityRef4);

        List<EntityRef> indexRefs = new LinkedList<>();


        EntityRef ientityRef = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("-1232.32").build();
        indexRefs.add(ientityRef);

        EntityRef ientityRef2 = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("-1232.12").build();
        indexRefs.add(ientityRef2);

        EntityRef ientityRef3 = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("100.50").build();
        indexRefs.add(ientityRef3);

        EntityRef ientityRef4 = EntityRef.Builder.anEntityRef()
            .withId(al.getAndIncrement())
            .withOrderValue("1232.12").build();
        indexRefs.add(ientityRef4);

        StreamMerger<EntityRef> streamMerger = new StreamMerger<>();
        List<EntityRef> collect = streamMerger
            .merge(masterRefs.stream(), indexRefs.stream(), new EntityRefComparator(FieldType.DECIMAL), true)
            .collect(toList());

        System.out.println(collect);
    }
} 
