package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityRefComparator;
import com.xforceplus.ultraman.oqsengine.core.service.utils.StreamMerger;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.stream.Collectors.toList;

public class MergeTest {


    @Test
    public void mergeTest(){
        StreamMerger<EntityRef> streamMerger = new StreamMerger<>();

        List<EntityRef> masterRefs = new LinkedList<>();

        AtomicLong al = new AtomicLong(0);

        EntityRef entityRef = new EntityRef();
        entityRef.setId(al.getAndIncrement());
        entityRef.setOrderValue("-0.46");
        masterRefs.add(entityRef);

        EntityRef entityRef2 = new EntityRef();
        entityRef2.setId(al.getAndIncrement());
        entityRef2.setOrderValue("-0.32");
        masterRefs.add(entityRef2);

        EntityRef entityRef3 = new EntityRef();
        entityRef3.setId(al.getAndIncrement());
        entityRef3.setOrderValue("0.32");
        masterRefs.add(entityRef3);

        EntityRef entityRef4 = new EntityRef();
        entityRef4.setId(al.getAndIncrement());
        entityRef4.setOrderValue("0.46");
        masterRefs.add(entityRef4);

        List<EntityRef> indexRefs = new LinkedList<>();


        EntityRef ientityRef = new EntityRef();
        ientityRef.setId(al.getAndIncrement());
        ientityRef.setOrderValue("-1232.32");
        indexRefs.add(ientityRef);

        EntityRef ientityRef2 = new EntityRef();
        ientityRef2.setId(al.getAndIncrement());
        ientityRef2.setOrderValue("-1232.12");
        indexRefs.add(ientityRef2);

        EntityRef ientityRef3 = new EntityRef();
        ientityRef3.setId(al.getAndIncrement());
        ientityRef3.setOrderValue("100.50");
        indexRefs.add(ientityRef3);

        EntityRef ientityRef4 = new EntityRef();
        ientityRef4.setId(al.getAndIncrement());
        ientityRef4.setOrderValue("1232.12");
        indexRefs.add(ientityRef4);

        List<EntityRef> collect = streamMerger.merge(masterRefs.stream(), indexRefs.stream(), new EntityRefComparator(FieldType.DECIMAL), true).collect(toList());

        System.out.println(collect);

    }
}
