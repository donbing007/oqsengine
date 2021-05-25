package com.xforceplus.ultraman.oqsengine.core.service.utils;

import static java.util.stream.Collectors.toList;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * StreamMerger Tester.
 *
 * @author dongbin
 * @version 1.0 12/03/2020
 * @since <pre>Dec 3, 2020</pre>
 */
public class StreamMergerTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void mergeTest() {
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

        StreamMerger<EntityRef> streamMerger = new StreamMerger<>();
        List<EntityRef> collect = streamMerger
            .merge(masterRefs.stream(), indexRefs.stream(), new EntityRefComparator(FieldType.DECIMAL), true)
            .collect(toList());

        System.out.println(collect);
    }

    @Test
    public void sortedTest() {

        List<EntityRef> masterRefs = new ArrayList<>();

        EntityRef entityRefA = new EntityRef();
        EntityRef entityRefB = new EntityRef();
        EntityRef entityRefC = new EntityRef();
        EntityRef entityRefD = new EntityRef();
        EntityRef entityRefE = new EntityRef();
        EntityRef entityRefF = new EntityRef();

        entityRefA.setOrderValue("12");
        entityRefB.setOrderValue("200");
        entityRefC.setOrderValue("201");
        entityRefD.setOrderValue("120");
        entityRefE.setOrderValue("10");
        entityRefF.setOrderValue("192");

        masterRefs.add(entityRefA);
        masterRefs.add(entityRefB);
        masterRefs.add(entityRefC);
        masterRefs.add(entityRefD);
        masterRefs.add(entityRefE);
        masterRefs.add(entityRefF);

        EntityRefComparator entityRefComparator = new EntityRefComparator(FieldType.STRING);

        List<EntityRef> sortedMasterRefs =
            masterRefs.stream()
                .sorted(entityRefComparator)
                .collect(toList());


        sortedMasterRefs.forEach(System.out::println);
    }

}
