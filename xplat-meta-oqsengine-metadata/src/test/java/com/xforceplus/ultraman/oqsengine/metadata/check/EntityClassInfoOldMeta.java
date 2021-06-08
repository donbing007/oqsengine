package com.xforceplus.ultraman.oqsengine.metadata.check;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by justin.xu on 06/2021.
 *
 * @since 1.8
 */
public class EntityClassInfoOldMeta {

    public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
        .enable(DeserializationFeature.USE_LONG_FOR_INTS)
        .build();

    @Test
    public void check_120_To_121() throws JsonProcessingException {
        EntityField entityField = EntityField.Builder.anEntityField()
            .withId(1001)
            .withName("test")
            .withCnName("testCn")
            .withFieldType(FieldType.LONG)
            .withDictId("dict")
            .withDefaultValue("dv")
            .withConfig(toFieldConfig()).build();

        String entityFieldStr = OBJECT_MAPPER.writeValueAsString(entityField);

        IEntityField e1 = OBJECT_MAPPER.readValue(entityFieldStr, EntityField.class);
        Assert.assertNotNull(e1);

        IEntityField e2 = cloneEntityField(e1);
        Assert.assertNotNull(e2);
    }


    private static FieldConfig toFieldConfig() {
        return FieldConfig.Builder.anFieldConfig()
            .build();
    }


    private IEntityField cloneEntityField(IEntityField entityField) {
        if (null != entityField) {
            EntityField.Builder builder = EntityField.Builder.anEntityField()
                .withName(entityField.name())
                .withCnName(entityField.cnName())
                .withFieldType(entityField.type())
                .withDictId(entityField.dictId())
                .withId(entityField.id())
                .withDefaultValue(entityField.defaultValue());

            if (null == entityField.calculator()) {
                builder.withCalculator(Calculator.Builder.anCalculator()
                    .withCalculateType(Calculator.Type.NORMAL)
                    .withFailedPolicy(Calculator.FailedPolicy.UNKNOWN)
                    .build()
                );
            } else {
                builder.withCalculator(Calculator.Builder.anCalculator()
                    .withCalculateType(entityField.calculator().getType())
                    .withExpression(entityField.calculator().getExpression())
                    .withMin(entityField.calculator().getMin())
                    .withMax(entityField.calculator().getMax())
                    .withCondition(entityField.calculator().getCondition())
                    .withEmptyValueTransfer(entityField.calculator().getEmptyValueTransfer())
                    .withValidator(entityField.calculator().getValidator())
                    .withModel(entityField.calculator().getModel())
                    .withStep(entityField.calculator().getStep())
                    .withLevel(entityField.calculator().getLevel())
                    .withPatten(entityField.calculator().getPatten())
                    .withArgs(null != entityField.calculator().getArgs() ? entityField.calculator().getArgs() : new ArrayList<>())
                    .withFailedPolicy(entityField.calculator().getFailedPolicy())
                    .withFailedDefaultValue(entityField.calculator().getFailedDefaultValue())
                    .build());
            }

            if (null != entityField.config()) {
                FieldConfig config = entityField.config();
                builder.withConfig(FieldConfig.Builder.anFieldConfig()
                    .withDelimiter(config.getDelimiter())
                    .withDisplayType(config.getDisplayType())
                    .withFieldSense(config.getFieldSense())
                    .withFuzzyType(config.getFuzzyType())
                    .withIdentifie(config.isIdentifie())
                    .withMax(config.getMax())
                    .withMin(config.getMin())
                    .withPrecision(config.getPrecision())
                    .withRequired(config.isRequired())
                    .withSearchable(config.isSearchable())
                    .withSplittable(config.isSplittable())
                    .withUniqueName(config.getUniqueName())
                    .withValidateRegexString(config.getValidateRegexString())
                    .withWildcardMaxWidth(config.getWildcardMaxWidth())
                    .withWildcardMinWidth(config.getWildcardMinWidth())
                    .withCrossSearch(config.isCrossSearch())
                    .build()
                );
            }

            return builder.build();
        }
        return null;
    }
}
