package com.xforceplus.ultraman.oqsengine.sdk;

import com.github.benmanes.caffeine.cache.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.sdk.util.ConditionQueryRequestHelper;
import com.xforceplus.ultraman.oqsengine.sdk.util.flow.QueueFlow;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.*;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.Test;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.stringtemplate.v4.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 *
 */
public class UtilsTest {

    @Test
    public void testTemplate(){

        String template = "<a>$name$</a>";
        ST st = new ST(template,'$','$');
        st.add("name", "World");
        assertEquals("<a>World</a>", st.render());
    }

    @Test
    public void testHelper(){

        ConditionQueryRequest conditionQueryRequestA = new ConditionQueryRequest();

        conditionQueryRequestA.setPageNo(1);
        conditionQueryRequestA.setPageSize(10);
        Conditions conditions = new Conditions();
        SubFieldCondition subFieldCondition = new SubFieldCondition();
        FieldCondition fieldCondition = new FieldCondition();
        conditions.setEntities(Arrays.asList(subFieldCondition));
        conditions.setFields(Arrays.asList(fieldCondition));
        conditionQueryRequestA.setConditions(conditions);
        EntityItem item = new EntityItem();
        item.setFields(Arrays.asList("ssss"));
        SubEntityItem subItem = new SubEntityItem();
        item.setEntities(Arrays.asList(subItem));
        conditionQueryRequestA.setEntity(item);
        NameMapping nameMapping = new NameMapping();
        conditionQueryRequestA.setMapping(Arrays.asList(nameMapping));
        FieldSort fieldSort = new FieldSort();
        conditionQueryRequestA.setSort(Arrays.asList(fieldSort));

        ConditionQueryRequest conditionQueryRequestB = ConditionQueryRequestHelper.copyOrNew(conditionQueryRequestA);

        //assertEquals(conditionQueryRequestA, conditionQueryRequestB);

    }

    @Test
    public void testDate(){
        System.out.println(LocalDate.now().format(DateTimeFormatter.ofPattern("YYYY_MM_dd")));
    }


}
