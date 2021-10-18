package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator;

import static com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockCalculatorMetaManager.L1_ENTITY_CLASS;

import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.TestInitTools;
import com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockCalculatorMetaManager;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.FormulaTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.TimeUtils;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.condition.QueryErrorCondition;
import com.xforceplus.ultraman.oqsengine.storage.master.pojo.ErrorStorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 计算字段管理测试.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
@ExtendWith({RedisContainer.class})
public class ManagementWithCalculatorTest {

    private EntityManagementServiceImpl impl;

    private MasterStorage masterStorage;

    private static String bizType = Long.MAX_VALUE - 4 + "";
    private static Long TEST_OFFSET_DATA = Long.MAX_VALUE - 7;


    @BeforeAll
    public static void beforeAll() throws IllegalAccessException {
        TestInitTools.bizIdGenerator(bizType);
    }

    /**
     * 测试初始化.
     */
    @BeforeEach
    public void before() throws Exception {

        impl = TestInitTools.entityManagementService(new MockCalculatorMetaManager());

        masterStorage = new MockMasterStorage();
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);

        impl.init();
    }

    @AfterAll
    public static void after() throws Exception {
        TestInitTools.close();
    }

    private enum Compare {
        LT,
        GT,
        EQ,
        LE,
        GE,
        NOT,
        START_WITH,
        NOTHING;

        public static boolean compareTwoValue(Object a, Object b, Compare compare) {
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
                case LE: {
                    return (Long) a <= (Long) b;
                }
                case GE: {
                    return (Long) a >= (Long) b;
                }
                case NOT: {
                    return !a.equals(b);
                }
                case START_WITH: {
                    return ((String) b).startsWith((String) a);
                }
                case NOTHING: {
                    return null != b;
                }
                default: {
                    return false;
                }
            }
        }
    }

    private final long expectedId = 2;
    private final long expectedFailedId = 20;
    private final Map<Long, AbstractMap.SimpleEntry<Object, Compare>> expectedResult = new HashMap<>();
    private String expectedAutoFill = null;
    private static Long idGeneratorLocal = 0L;
    private static Long staticGeneratorSenior = 1L;

    private static int startCheckYear = LocalDateTime.now().getYear();
    private static int replaceCheckYear = 1970;

    @Test
    public void buildTest() throws SQLException {
        expectedAutoFill = bizType + "-" + (idGeneratorLocal++);
        Map<String, Object> params = new HashMap<>();

        Long expectedValue = 10000L;
        params.put("longValue0", expectedValue);
        params.put("createTime", LocalDateTime.now());
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
        params.put("createTime", LocalDateTime.of(replaceCheckYear, 01, 01, 01, 01));

        initAndAssert(expectedId, expectedValue, params, false);
    }

    List<String> expectedFailed = Arrays.asList("longValue1", "longValue2", "stringValueMix");

    @Test
    public void buildHalfSuccessTest() throws SQLException {
        expectedAutoFill = bizType + "-" + (idGeneratorLocal++);
        Map<String, Object> params = new HashMap<>();
        params.put("createTime", LocalDateTime.now());

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

        entityValue.addValue(new FormulaTypedValue(L1_ENTITY_CLASS.field("senior autoFill").get(), params));
        entityValue.addValue(new FormulaTypedValue(L1_ENTITY_CLASS.field("offset data").get(), params));
        entityValue.addValue(
            new DateTimeValue(L1_ENTITY_CLASS.field("createTime").get(), (LocalDateTime) params.get("createTime")));

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
                Assertions.assertEquals(expectedFailed.size(), operationResult.getHints().size());
                operationResult.getHints().forEach(
                    hints -> {
                        Assertions.assertTrue(expectedFailed.contains(hints.getField().name()));
                    }
                );
            }
        }

        if (operationResult.getResultStatus().equals(ResultStatus.SUCCESS)
            || operationResult.getResultStatus().equals(ResultStatus.HALF_SUCCESS)) {
            Optional<IEntity> entityOp = masterStorage.selectOne(replaceEntity.id(), L1_ENTITY_CLASS);
            Assertions.assertTrue(entityOp.isPresent());
            IEntity entity = entityOp.get();

            expectedResult.forEach((key, value) -> {
                Optional<IValue> valueOp = entity.entityValue().getValue(key);
                Assertions.assertTrue(valueOp.isPresent());
                Assertions
                    .assertTrue(Compare.compareTwoValue(value.getKey(), valueOp.get().getValue(), value.getValue()));
                if (valueOp.get().getField().calculationType().equals(CalculationType.AUTO_FILL)) {
                    AutoFill autoFill = (AutoFill) valueOp.get().getField().config().getCalculation();
                    if (autoFill.getDomainNoType().equals(AutoFill.DomainNoType.NORMAL)) {
                        Assertions.assertEquals(expectedAutoFill, valueOp.get().getValue());
                    } else {
                        if (insert) {
                            String[] parts =
                                ((String) valueOp.get().getValue()).split(":");

                            Assertions.assertEquals(2, parts.length);

                            Assertions.assertEquals(entity.entityValue().getValue("stringValueMix").get().getValue(),
                                parts[0]);
                            Assertions.assertEquals(String.format("%04d", staticGeneratorSenior.intValue()), parts[1]);
                        }
                    }
                } else if (valueOp.get().getField().calculationType().equals(CalculationType.FORMULA)) {
                    if (valueOp.get().getField().id() == TEST_OFFSET_DATA) {
                        if (insert) {
                            Assertions
                                .assertEquals(startCheckYear + 1, ((LocalDateTime) valueOp.get().getValue()).getYear());
                        } else {
                            Assertions
                                .assertEquals(replaceCheckYear + 1,
                                    ((LocalDateTime) valueOp.get().getValue()).getYear());
                        }
                    }
                }
            });
        }
    }

    private void setExpectedResult(Long expectedValue) {
        if (expectedValue != null) {
            expectedResult.put(Long.MAX_VALUE - 1, new AbstractMap.SimpleEntry<>(expectedValue * 3, Compare.EQ));
            expectedResult.put(Long.MAX_VALUE - 2, new AbstractMap.SimpleEntry<>(expectedValue * 3 / 2, Compare.EQ));
            expectedResult
                .put(Long.MAX_VALUE - 5, new AbstractMap.SimpleEntry<>(expectedValue + "-", Compare.START_WITH));
        } else {
            expectedResult.put(Long.MAX_VALUE - 1, new AbstractMap.SimpleEntry<>(0, Compare.EQ));
            expectedResult.put(Long.MAX_VALUE - 2, new AbstractMap.SimpleEntry<>(1, Compare.EQ));
            expectedResult.put(Long.MAX_VALUE - 5, new AbstractMap.SimpleEntry<>("0", Compare.EQ));
        }

        expectedResult.put(Long.MAX_VALUE - 3,
            new AbstractMap.SimpleEntry<>(TimeUtils.convert(System.currentTimeMillis()), Compare.NOTHING));
        expectedResult.put(Long.MAX_VALUE - 4, new AbstractMap.SimpleEntry<>(0L, Compare.NOTHING));

        expectedResult.put(Long.MAX_VALUE - 6, new AbstractMap.SimpleEntry<>(0L, Compare.NOTHING));
        expectedResult.put(Long.MAX_VALUE - 7, new AbstractMap.SimpleEntry<>(0L, Compare.NOTHING));
    }

    private static class MockMasterStorage implements MasterStorage {

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
        public DataIterator<OriginalEntity> iterator(IEntityClass entityClass, long startTime, long endTime,
                                                     long lastId, int size) throws SQLException {
            return null;
        }

        @Override
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void writeError(ErrorStorageEntity errorStorageEntity) {

        }

        @Override
        public Collection<ErrorStorageEntity> selectErrors(QueryErrorCondition queryErrorCondition)
            throws SQLException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IEntity> selectOne(long id) throws SQLException {
            return Optional.ofNullable(entityMap.get(id));
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
