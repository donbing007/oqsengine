package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.xforceplus.ultraman.oqsengine.core.service.utils.EntityRefComparator;
import com.xforceplus.ultraman.oqsengine.core.service.utils.StreamMerger;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Test;

public class CompareTest {

    @Test
    public void testStream() {

        EntityRef entityRefA = new EntityRef();
        entityRefA.setOrderValue("1");
        EntityRef entityRefB = new EntityRef();
        entityRefB.setOrderValue("12");
        EntityRef entityRefC = new EntityRef();
        entityRefC.setOrderValue("11");
        EntityRef entityRefD = new EntityRef();
        entityRefD.setOrderValue("201");
        EntityRef entityRefE = new EntityRef();
        entityRefE.setOrderValue("1205");

        Stream<EntityRef> listStreamA = Arrays.asList(entityRefA, entityRefB, entityRefE).stream();
        Stream<EntityRef> listStreamB = Arrays.asList(entityRefC, entityRefD).stream();

        StreamMerger streamMerger = new StreamMerger();
        Stream<EntityRef> merge = streamMerger.merge(listStreamA, listStreamB
            , new EntityRefComparator(FieldType.STRING), true);

        merge.collect(Collectors.toList()).forEach(System.out::println);
    }
}