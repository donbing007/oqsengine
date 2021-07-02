package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator;

import static com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockCalculatorMetaManager.L1_ENTITY_CLASS;

import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.TimeUtils;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.core.service.impl.BaseInit;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockCalculatorMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.FormulaTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.StorageUniqueEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class ManagementWithCalculatorTest {

    private EntityManagementServiceImpl impl;
    private MockMasterStorage masterStorage;

    @BeforeEach
    public void before() throws Exception {
        impl = BaseInit.entityManagementService(new MockCalculatorMetaManager());

        masterStorage = new MockMasterStorage();
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        impl.init();
    }

    private enum COMPARE {
        LT,
        GT,
        EQ,
        LE,
        GE,
        NOT,
        START_WITH,
        NOTHING;

        public static boolean compareTwoValue(Object a, Object b, COMPARE compare) {
            a = a instanceof Integer ? ((Integer) a).longValue() : a;
            b = b instanceof Integer ? ((Integer) b).longValue() : b;

            switch (compare) {
                case LT: {
                    return (Long) a < (Long) b;
                }
                case GT: {
                    return (Long) a > (Long) b;
                }
                case EQ: {
                    return a.equals(b);
                }
                case LE : {
                    return (Long) a <= (Long) b;
                }
                case GE : {
                    return (Long) a >= (Long) b;
                }
                case NOT : {
                    return !a.equals(b);
                }
                case START_WITH: {
                    return ((String) b).startsWith((String) a);
                }
                case NOTHING: {
                    return null != b;
                }
            }

            return false;
        }
    }

    private final long expectedId = 2;
    private final long expectedFailedId = 20;
    private final Map<Long, AbstractMap.SimpleEntry<Object, COMPARE>> expectedResult = new HashMap<>();
    private String expectedAutoFill = null;
    private static Long idGeneratorLocal = 0L;

    @Test
    public void buildTest() throws SQLException {
        expectedAutoFill = (Long.MAX_VALUE - 4) + "-" + (idGeneratorLocal++);
        Map<String, Object> params = new HashMap<>();

        Long expectedValue = 10000L;
        params.put("longValue0", expectedValue);
        setExpectedResult(expectedValue);

        initAndAssert(expectedId, expectedValue, params, true);
    }

    @Test
    public void replaceTest() throws SQLException {

        buildTest();

        Map<String, Object> params = new HashMap<>();

        Long expectedValue = 20000L;
        params.put("longValue0", expectedValue);
        params.put("longValue1", 200L);
        params.put("longValue2", 100L);
        params.put("dateValue0", 123L);
        params.put("stringAutoFill", "111");
        params.put("stringValueMix", "222");

        initAndAssert(expectedId, expectedValue, params, false);
    }

    List<String> expectedFailed = Arrays.asList("longValue1", "longValue2", "stringValueMix");

    @Test
    public void buildHalfSuccessTest() throws SQLException {
        expectedAutoFill = (Long.MAX_VALUE - 4) + "-" + (idGeneratorLocal++);
        Map<String, Object> params = new HashMap<>();

        expectedResult.clear();

        initAndAssert(expectedFailedId, null, params, true);

        initAndAssert(expectedFailedId, null, params, false);
    }


    private void initAndAssert(long id, Long expectedValue, Map<String, Object> params, boolean insert)
        throws SQLException {
        setExpectedResult(expectedValue);

        IEntityValue entityValue = EntityValue.build()
            .addValue(
                new FormulaTypedValue(MockCalculatorMetaManager.L0_ENTITY_CLASS.field("longValue1").get(), params))
            .addValue(
                new FormulaTypedValue(MockCalculatorMetaManager.L0_ENTITY_CLASS.field("longValue2").get(), params)
            )
            .addValue(
                new FormulaTypedValue(L1_ENTITY_CLASS.field("dateValue0").get(), params)
            )
            .addValue(
                new FormulaTypedValue(L1_ENTITY_CLASS.field("stringAutoFill").get(), params)
            )
            .addValue(
                new FormulaTypedValue(L1_ENTITY_CLASS.field("stringValueMix").get(), params)
            );

        if (null != expectedValue) {
            entityValue.addValue(
                new LongValue(MockCalculatorMetaManager.L0_ENTITY_CLASS.field("longValue0").get(), expectedValue));
        }

        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(L1_ENTITY_CLASS.id(), L1_ENTITY_CLASS.code()))
            .withId(id)
            .withTime(System.currentTimeMillis())
            .withEntityValue(entityValue)
            .build();
        OperationResult operationResult = insert ? impl.build(replaceEntity) : impl.replace(replaceEntity);
        if (null == expectedValue) {
            Assertions.assertNotEquals(ResultStatus.SUCCESS, operationResult.getResultStatus());
            if (operationResult.getResultStatus().equals(ResultStatus.HALF_SUCCESS)) {
                Assertions.assertEquals(expectedFailed.size(), operationResult.getFailedMap().size());
                expectedFailed.forEach(
                    failed -> {
                        Assertions.assertTrue(operationResult.getFailedMap().containsKey(failed));
                    }
                );
            }
        } else {
            Assertions.assertEquals(ResultStatus.SUCCESS, operationResult.getResultStatus());
        }

        if (operationResult.getResultStatus().equals(ResultStatus.SUCCESS) ||
                operationResult.getResultStatus().equals(ResultStatus.HALF_SUCCESS)) {
            Optional<IEntity> eOp = masterStorage.selectOne(replaceEntity.id(), L1_ENTITY_CLASS);
            Assertions.assertTrue(eOp.isPresent());
            IEntity entity = eOp.get();

            expectedResult.forEach((key, value) -> {
                Optional<IValue> vOp = entity.entityValue().getValue(key);
                Assertions.assertTrue(vOp.isPresent());
                Assertions.assertTrue(COMPARE.compareTwoValue(value.getKey(), vOp.get().getValue(), value.getValue()));
                if (vOp.get().getField().calculateType().equals(Calculator.Type.AUTO_FILL)) {
                    Assertions.assertEquals(expectedAutoFill, vOp.get().getValue());
                }
            });
        }
    }

    private void setExpectedResult(Long expectedValue) {
        if (expectedValue != null) {
            expectedResult.put(Long.MAX_VALUE - 1, new AbstractMap.SimpleEntry<>(expectedValue * 3, COMPARE.EQ));
            expectedResult.put(Long.MAX_VALUE - 2, new AbstractMap.SimpleEntry<>(expectedValue * 3 / 2, COMPARE.EQ));
            expectedResult
                .put(Long.MAX_VALUE - 5, new AbstractMap.SimpleEntry<>(expectedValue + "-", COMPARE.START_WITH));
        } else {
            expectedResult.put(Long.MAX_VALUE - 1, new AbstractMap.SimpleEntry<>(0, COMPARE.EQ));
            expectedResult.put(Long.MAX_VALUE - 2, new AbstractMap.SimpleEntry<>(1, COMPARE.EQ));
            expectedResult.put(Long.MAX_VALUE - 5, new AbstractMap.SimpleEntry<>("0", COMPARE.EQ));
        }

        expectedResult.put(Long.MAX_VALUE - 3,
            new AbstractMap.SimpleEntry<>(TimeUtils.convert(System.currentTimeMillis()), COMPARE.NOTHING));
        expectedResult.put(Long.MAX_VALUE - 4, new AbstractMap.SimpleEntry<>(0L, COMPARE.NOTHING));

    }

    public static class MockMasterStorage implements MasterStorage {
        private final Map<Long, IEntity> entityMap = new HashMap<>();
        public int build(IEntity entity, IEntityClass entityClass) throws SQLException {
            entityMap.put(entity.id(), entity);
            return 1;
        }

        public int replace(IEntity entity, IEntityClass entityClass) throws SQLException {
            entityMap.put(entity.id(), entity);
            return 1;
        }

        public int delete(IEntity entity, IEntityClass entityClass) throws SQLException {
            entityMap.remove(entity.id());
            return 1;
        }

        @Override
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId) throws SQLException {
            return null;
        }

        @Override
        public Optional<StorageUniqueEntity> select(List<BusinessKey> businessKeys, IEntityClass entityClass)
            throws SQLException {
            return Optional.empty();
        }

        @Override
        public void writeError(ErrorStorageEntity errorStorageEntity) {

        }

        @Override
        public Collection<ErrorStorageEntity> selectErrors(QueryErrorCondition queryErrorCondition)
            throws SQLException {
            return null;
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            return null;
        }

        @Override
        public Optional<IEntity> selectOne(long id) throws SQLException {
            return Optional.empty();
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(entityMap.get(id));
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids) throws SQLException {
            return null;
        }

        @Override
        public Collection<IEntity> selectMultiple(long[] ids, IEntityClass entityClass) throws SQLException {
            return null;
        }

        @Override
        public boolean exist(long id) throws SQLException {
            return entityMap.containsKey(id);
        }
    }
}
