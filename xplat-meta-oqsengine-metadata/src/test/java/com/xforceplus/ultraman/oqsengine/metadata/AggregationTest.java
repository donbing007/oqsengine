package com.xforceplus.ultraman.oqsengine.metadata;

import static com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant.PROFILE_CODE_1;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.DomainCondition;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralConstant;
import com.xforceplus.ultraman.oqsengine.metadata.mock.generator.GeneralEntityUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 06/2022.
 *
 * @since 1.8
 */
public class AggregationTest extends AbstractMetaTestHelper {

    private static long entityIdMain = 1L;
    private static long entityFiledIdMain = 2L;

    private static String operator = "=";
    private static String value = "10";
    private static long entityIdAgg = 3L;
    private static long entityFiledIdAgg = 4L;

    @BeforeEach
    public void before() throws Exception {
        super.init();
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy();
    }

    @Test
    public void aggregationTest() throws IllegalAccessException {
        String expectedAppId = "testAggregationId";
        String expectedAppCode = "testAggregationCode";
        int expectedVersion = 1;

        List<EntityStoragePack> entityStoragePacks = Arrays.asList(
            new EntityStoragePack(entityIdMain, entityFiledIdMain, false, null, null),
            new EntityStoragePack(entityIdAgg, entityFiledIdAgg, true, entityIdMain, entityFiledIdMain)
        );

        EntityClassSyncResponse entityClassSyncResponse =
            Response.entityClassSyncResponse(expectedAppId, expectedAppCode, expectedVersion, entityStoragePacks);

        mockRequestHandler.invoke(entityClassSyncResponse, null);

        Optional<IEntityClass> entityClassOp =
            MetaInitialization.getInstance().getMetaManager().load(entityIdAgg, null);
        Assertions.assertTrue(entityClassOp.isPresent());
        Assertions.assertEquals(expectedAppCode, entityClassOp.get().appCode());

        Optional<IEntityField> eOp = entityClassOp.get().field(entityFiledIdAgg);
        Assertions.assertTrue(eOp.isPresent());
        Assertions.assertEquals(String.format("%d_field_%d %s %s", entityIdMain, entityFiledIdMain, operator, value),
            ((Aggregation) eOp.get().config().getCalculation()).getConditions().get().toString());
    }

    public static class EntityStoragePack {
        private Long classId;
        private Long entityFieldId;
        private Boolean isAggregation;
        private Long aggregationClassId;
        private Long aggregationFieldId;

        public EntityStoragePack(Long classId, Long entityFieldId, Boolean isAggregation, Long aggregationClassId,
                                 Long aggregationFieldId) {
            this.classId = classId;
            this.entityFieldId = entityFieldId;
            this.isAggregation = isAggregation;
            this.aggregationClassId = aggregationClassId;
            this.aggregationFieldId = aggregationFieldId;
        }

        public Long getClassId() {
            return classId;
        }

        public Long getEntityFieldId() {
            return entityFieldId;
        }

        public Boolean getAggregation() {
            return isAggregation;
        }

        public Long getAggregationClassId() {
            return aggregationClassId;
        }

        public Long getAggregationFieldId() {
            return aggregationFieldId;
        }
    }

    public static class Response {

        public static EntityClassSyncResponse entityClassSyncResponse(String appId, String appCode, int version, List<EntityStoragePack> entityStoragePacks) {

            return EntityClassSyncResponse.newBuilder()
                .setAppId(appId)
                .setVersion(version + 1)
                .setEntityClassSyncRspProto(entityClassRspProto(appCode, entityStoragePacks))
                .build();
        }

        /**
         * 生成同步响应.
         */
        public static EntityClassSyncRspProto entityClassRspProto(
            String appCode, List<EntityStoragePack> entityStoragePacks) {
            /*
             * 生成爷爷
             */
            List<EntityClassInfo> entityClassInfos = new ArrayList<>();
            entityStoragePacks.forEach(
                e -> {
                    entityClassInfos.add(entityClassInfo(e));
                }
            );

            return EntityClassSyncRspProto.newBuilder()
                .addAllEntityClasses(entityClassInfos)
                .setAppCode(appCode)
                .build();
        }
    }

    /**
     * 生成.
     */
    public static EntityClassInfo entityClassInfo(EntityStoragePack entityStoragePack) {
        List<EntityFieldInfo> entityFieldInfos = new ArrayList<>();

        entityFieldInfos.add(entityFieldInfo(entityStoragePack));

        return EntityClassInfo.newBuilder()
            .setId(entityStoragePack.getClassId())
            .setType(EntityClassType.DYNAMIC.getType())
            .setVersion(GeneralConstant.DEFAULT_VERSION)
            .setCode(entityStoragePack.getClassId() + "")
            .setName(entityStoragePack.getClassId() + "")
            .setLevel(0)
            .addAllEntityFields(entityFieldInfos)
            .build();
    }

    /**
     * 生成通用calculator
     */
    public static Calculator genericCalculator() {
        return Calculator.newBuilder()
            .setCalculateType(CalculationType.STATIC.getSymbol())
            .build();
    }

    /**
     * 生成autoFill-calculator
     */
    public static Calculator aggregation(long boId, long filedId, long domainEntityId, long domainFieldId, String value) {

        return Calculator.newBuilder()
            .setAggregationBoId(boId)
            .setAggregationFieldId(filedId)
            .setCalculateType(CalculationType.AGGREGATION.getSymbol())
            .setAggregationType(AggregationType.MAX.getType())
            .addDomainConditions(DomainCondition
                .newBuilder()
                .setEntityId(domainEntityId)
                .setEntityFieldId(domainFieldId)
                .setFieldType(DomainCondition.FieldType.LONG)
                .setOperator(DomainCondition.Operator.EQUALS)
                .setProfile(PROFILE_CODE_1.getKey())
                .setValues(value)
                .build()
            ).build();
    }


    /**
     * 生成entityFieldInfo.
     */
    public static EntityFieldInfo entityFieldInfo(EntityStoragePack entityStoragePack) {
        long entityFieldId = entityStoragePack.entityFieldId;

        EntityFieldInfo.Builder builder = EntityFieldInfo.newBuilder()
            .setId(entityFieldId)
            .setFieldType(EntityFieldInfo.FieldType.LONG)
            .setName(entityStoragePack.getClassId() + "_field_" + entityFieldId)
            .setCname(entityStoragePack.getClassId() + "_field_" + entityFieldId)
            .setDictId(GeneralEntityUtils.EntityFieldHelper.dictId(entityFieldId))
            .setFieldConfig(fieldConfig(true, FieldType.LONG, GeneralConstant.MOCK_SYSTEM_FIELD_TYPE));

        if (entityStoragePack.isAggregation) {
            builder.setCalculator(aggregation(entityStoragePack.getClassId(), entityStoragePack.entityFieldId
                , entityStoragePack.aggregationClassId, entityStoragePack.aggregationFieldId, value));
        } else {
            builder.setCalculator(genericCalculator());
        }

        return builder.build();
    }

    /**
     * 生成.
     */
    private static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig fieldConfig(
        boolean searchable, FieldType fieldType, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.MetaFieldSense systemFieldType) {
        return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.newBuilder()
            .setSearchable(searchable)
            .setIsRequired(true)
            .setIdentifier(false)
            .setMetaFieldSense(systemFieldType)
            .setLength(15)
            .setPrecision(4)
            .setJdbcType(toJdbcType(fieldType))
            .build();
    }

    /**
     * 根据OQS字段类型转换成JDBC类型.
     */
    public static int toJdbcType(FieldType fieldType) {
        switch (fieldType) {
            case LONG: {
                return Types.BIGINT;
            }
            case DECIMAL: {
                return Types.DECIMAL;
            }
            case STRING:
            case ENUM:
            case STRINGS: {
                return Types.VARCHAR;
            }
            case DATETIME: {
                return Types.TIMESTAMP;
            }
            default:
                return Types.VARCHAR;
        }
    }
}
