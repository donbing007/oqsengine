package com.xforceplus.ultraman.oqsengine.metadata.check;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AbstractCalculation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Formula;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Lookup;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.StaticCalculation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


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
        Assertions.assertNotNull(e1);

        IEntityField e2 = cloneEntityField(e1);
        Assertions.assertNotNull(e2);
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
                    .withScale(config.scale())
                    .withRequired(config.isRequired())
                    .withSearchable(config.isSearchable())
                    .withSplittable(config.isSplittable())
                    .withUniqueName(config.getUniqueName())
                    .withValidateRegexString(config.getValidateRegexString())
                    .withWildcardMaxWidth(config.getWildcardMaxWidth())
                    .withWildcardMinWidth(config.getWildcardMinWidth())
                    .withCrossSearch(config.isCrossSearch())
                    .withCalculation(toCalculation(config.getCalculation()))
                    .build()
                );
            }

            return builder.build();
        }
        return null;
    }

    public <R extends AbstractCalculation> R toCalculation(AbstractCalculation calculation) {
        switch (calculation.getCalculationType()) {
            case FORMULA: {
                Formula f = (Formula) calculation;

                return (R) Formula.Builder.anFormula()
                    .withExpression(f.getExpression())
                    .withLevel(f.getLevel())
                    .withArgs(f.getArgs())
                    .withFailedPolicy(f.getFailedPolicy())
                    .withFailedDefaultValue(f.getFailedDefaultValue())
                    .build();
            }
            case AUTO_FILL: {
                AutoFill f = (AutoFill) calculation;

                return (R) AutoFill.Builder.anAutoFill()
                    .withMin(f.getMin())
                    .withMax(f.getMax())
                    .withStep(f.getStep())
                    .withModel(f.getModel())
                    .withPatten(f.getPatten())
                    .build();
            }
            case LOOKUP: {
                Lookup f = (Lookup) calculation;

                return (R) Lookup.Builder.anLookup()
                    .withClassId(f.getClassId())
                    .withFieldId(f.getFieldId())
                    .build();
            }
            default: {
                return (R) StaticCalculation.Builder.anStaticCalculation().build();
            }
        }
    }
}
