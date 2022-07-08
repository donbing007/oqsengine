package com.xforceplus.ultraman.oqsengine.calculation.function.aggregation;

import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.CollectFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.aggregation.impl.CountFunction;
import com.xforceplus.ultraman.oqsengine.calculation.utils.ValueChange;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.EmptyTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.Opt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 07/2022.
 *
 * @since 1.8
 */
public class CollectFunctionTest {


    private IEntityField aggCollectField = EntityField.Builder.anEntityField()
        .withId(100)
        .withFieldType(FieldType.STRINGS)
        .withName("agg-collect")
        .withConfig(
            FieldConfig.Builder.anFieldConfig()
                .withCalculation(
                    Aggregation.Builder.anAggregation()
                        .withAggregationType(AggregationType.COLLECT)
                        .build()
                ).build()
        ).build();

    /**
     * 测试创建.
     */
    @Test
    public void testBuild() {

        List<String> cases = Arrays.asList("A", "B", "C");

        CollectFunction function = new CollectFunction();

        IValue aggValue = new StringsValue(aggCollectField, new String[0], "");

        Optional<IValue> newAggValueOp = Optional.of(aggValue);

        int i = 1;
        for (String caseValue : cases) {
            newAggValueOp = function.excute(newAggValueOp,
                ValueChange.build(
                    i++,
                    new EmptyTypedValue(aggCollectField),
                    new StringValue(aggCollectField, caseValue)));
        }

        IValue<String[]> stringsValue = newAggValueOp.get();
        Assertions.assertEquals(cases.size(), stringsValue.getValue().length);
        Assertions.assertEquals("1,1,1", stringsValue.getAttachment().get());
    }

    @Test
    public void testMixed() {
        //  test add
        String[] expectedStrings = new String[]{"A", "C", "B", "D"};
        String expectedAttachment = "3,1,2,1";

        List<String> addCases = Arrays.asList("A", "A", "A", "C", "B", "D", "B");


        CollectFunction function = new CollectFunction();

        IValue aggValue = new StringsValue(aggCollectField, new String[0], "");

        Optional<IValue> newAggValueOp = Optional.of(aggValue);

        int i = 1;
        for (String caseValue : addCases) {
            newAggValueOp = function.excute(newAggValueOp,
                ValueChange.build(
                    i++,
                    new EmptyTypedValue(aggCollectField),
                    new StringValue(aggCollectField, caseValue)));
        }

        IValue<String[]> stringsValue = newAggValueOp.get();

        Assertions.assertEquals(expectedStrings.length, stringsValue.getValue().length);
        for (int j = 0; j < expectedStrings.length; j++) {
            Assertions.assertEquals(expectedStrings[j], stringsValue.getValue()[j]);
        }

        Assertions.assertEquals(expectedAttachment, stringsValue.getAttachment().get());

        //  test del
        List<String> delCases = Arrays.asList("A", "D", "B");
        expectedStrings = new String[]{"A", "C", "B"};
        expectedAttachment = "2,1,1";

        for (String caseValue : delCases) {
            newAggValueOp = function.excute(newAggValueOp,
                ValueChange.build(
                    i++,
                    new StringValue(aggCollectField, caseValue),
                    new EmptyTypedValue(aggCollectField)));
        }

        stringsValue = newAggValueOp.get();

        Assertions.assertEquals(expectedStrings.length, stringsValue.getValue().length);

        for (int j = 0; j < expectedStrings.length; j++) {
            Assertions.assertEquals(expectedStrings[j], stringsValue.getValue()[j]);
        }

        Assertions.assertEquals(expectedAttachment, stringsValue.getAttachment().get());

        //  test replace
        List<Tuple2<String, String>> updateCases = Arrays.asList(new Tuple2<>("A", "F"), new Tuple2<>(null, "F"), new Tuple2<>("B", "H"));
        expectedStrings = new String[]{"A", "C", "F", "H"};
        expectedAttachment = "1,1,2,1";

        for (Tuple2<String, String> caseValue : updateCases) {

            IValue oldValue = null == caseValue._1() ? new EmptyTypedValue(aggCollectField) : new StringValue(aggCollectField, caseValue._1());
            IValue newValue = null == caseValue._2() ? new EmptyTypedValue(aggCollectField) : new StringValue(aggCollectField, caseValue._2());
            newAggValueOp = function.excute(newAggValueOp,
                ValueChange.build(
                    i++,
                    oldValue,
                    newValue));
        }

        stringsValue = newAggValueOp.get();

        Assertions.assertEquals(expectedStrings.length, stringsValue.getValue().length);

        for (int j = 0; j < expectedStrings.length; j++) {
            Assertions.assertEquals(expectedStrings[j], stringsValue.getValue()[j]);
        }

        Assertions.assertEquals(expectedAttachment, stringsValue.getAttachment().get());
    }

    @Test
    public void initTest() {



    }
}
