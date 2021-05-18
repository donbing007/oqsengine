package com.xforceplus.ultraman.oqsengine.core.service.impl.calculator;

import static com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockCalculatorMetaManager.L1_ENTITY_CLASS;

import com.xforceplus.ultraman.oqsengine.calculate.utils.TimeUtils;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.core.service.impl.BaseInit;
import com.xforceplus.ultraman.oqsengine.core.service.impl.EntityManagementServiceImpl;
import com.xforceplus.ultraman.oqsengine.core.service.impl.calculator.mock.MockCalculatorMetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculateType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.FormulaTypedValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import com.xforceplus.ultraman.oqsengine.storage.pojo.select.SelectConfig;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/18
 * @since 1.8
 */
public class ManagementWithCalculatorTest {

    private EntityManagementServiceImpl impl;
    private MockMasterStorage masterStorage;
    @Before
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
            switch (compare) {
                case LT : {
                    return (Long) a < (Long) b;
                }
                case GT : {
                    return (Long) a > (Long) b;
                }
                case EQ : {
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
    private long expectedId = 2;
    private Map<Long, AbstractMap.SimpleEntry<Object, COMPARE>> expectedResult = new HashMap<>();
    private String expectedAutoFill = null;
    private Long idGeneratorLocal = 0L;

    @Test
    public void buildTest() throws SQLException {
        expectedAutoFill = (Long.MAX_VALUE - 4) + "-" + (idGeneratorLocal++);
        Map<String, Object> params = new HashMap<>();

        Long expectedValue = 10000L;
        params.put("longValue0", expectedValue);
        setExpectedResult(expectedValue);

        initAndAssert(expectedValue, params, true);
    }


    @Test
    public void replaceTest() throws SQLException {
        expectedAutoFill = (Long.MAX_VALUE - 4) + "-" + (idGeneratorLocal++);
        buildTest();

        Map<String, Object> params = new HashMap<>();

        Long expectedValue = 20000L;
        params.put("longValue0", expectedValue);

        initAndAssert(expectedValue, params, false);
    }

    private void initAndAssert(Long expectedValue, Map<String, Object> params, boolean insert) throws SQLException {
        setExpectedResult(expectedValue);

        IEntity replaceEntity = Entity.Builder.anEntity()
            .withEntityClassRef(
                new EntityClassRef(L1_ENTITY_CLASS.id(), L1_ENTITY_CLASS.code()))
            .withId(expectedId)
            .withTime(System.currentTimeMillis())
            .withEntityValue(EntityValue.build()
                .addValue(
                    new LongValue(MockCalculatorMetaManager.L0_ENTITY_CLASS.field("longValue0").get(), expectedValue))
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
                )
            ).build();

        Assert.assertEquals(ResultStatus.SUCCESS, insert ? impl.build(replaceEntity).getResultStatus()
                            : impl.replace(replaceEntity).getResultStatus());


        Optional<IEntity> eOp = masterStorage.selectOne(replaceEntity.id(), L1_ENTITY_CLASS);
        Assert.assertTrue(eOp.isPresent());
        IEntity entity = eOp.get();

        expectedResult.forEach((key, value) -> {
            Optional<IValue> vOp = entity.entityValue().getValue(key);
            Assert.assertTrue(vOp.isPresent());
            Assert.assertTrue(COMPARE.compareTwoValue(value.getKey(), vOp.get().getValue(), value.getValue()));
            if (vOp.get().getField().calculateType().equals(CalculateType.AUTO_FILL)) {
                Assert.assertEquals(expectedAutoFill, vOp.get().getValue());
            }
        });
    }

    private void setExpectedResult(Long expectedValue) {
        expectedResult.put(Long.MAX_VALUE - 1, new AbstractMap.SimpleEntry<>(expectedValue * 3, COMPARE.EQ));
        expectedResult.put(Long.MAX_VALUE - 2, new AbstractMap.SimpleEntry<>(expectedValue * 3 / 2, COMPARE.EQ));
        expectedResult.put(Long.MAX_VALUE - 3, new AbstractMap.SimpleEntry<>(TimeUtils.convert(System.currentTimeMillis()), COMPARE.NOTHING));
        expectedResult.put(Long.MAX_VALUE - 4, new AbstractMap.SimpleEntry<>(0L, COMPARE.NOTHING));
        expectedResult.put(Long.MAX_VALUE - 5, new AbstractMap.SimpleEntry<>(expectedValue + "-", COMPARE.START_WITH));
    }

    public static class MockMasterStorage implements MasterStorage {
        private Map<Long, IEntity> entityMap = new HashMap<>();
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
        public Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, SelectConfig config)
            throws SQLException {
            return null;
        }

        @Override
        public Optional<IEntity> selectOne(long id, IEntityClass entityClass) throws SQLException {
            return Optional.ofNullable(entityMap.get(id));
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
