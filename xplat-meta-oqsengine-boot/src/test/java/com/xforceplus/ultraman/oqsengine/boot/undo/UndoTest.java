package com.xforceplus.ultraman.oqsengine.boot.undo;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootTestApplication;
import com.xforceplus.ultraman.oqsengine.boot.config.UndoConfiguration;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;
import com.xforceplus.ultraman.oqsengine.storage.transaction.TransactionManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/24/2020 4:13 PM
 * 功能描述:
 * 修改历史:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {OqsengineBootTestApplication.class})
@Import(UndoConfiguration.class)
public class UndoTest {

    @Autowired
    private LongIdGenerator longIdGenerator;

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private EntityManagementService entityManagementService;

    private IEntityClass fatherEntityClass;

    @Before
    public void before() throws Exception {

        fatherEntityClass = new EntityClass(longIdGenerator.next(), "father", Arrays.asList(
                new Field(longIdGenerator.next(), "f1", FieldType.LONG, FieldConfig.build().searchable(true)),
                new Field(longIdGenerator.next(), "f2", FieldType.STRING, FieldConfig.build().searchable(false)),
                new Field(longIdGenerator.next(), "f2", FieldType.DECIMAL, FieldConfig.build().searchable(true))
        ));
    }

    @Test
    public void testBuild() throws SQLException {


        Transaction tx = transactionManager.create();

        tx.getUndoExecutor().setMockError(true);

        IEntity entity = buildEntity(fatherEntityClass, false);

        entityManagementService.build(entity);

    }



    private IEntity buildEntity(IEntityClass entityClass, boolean buildId) {
        long entityId = 0;
        if (buildId) {
            entityId = longIdGenerator.next();
        }

        IEntityValue entityValue = new EntityValue(entityId);

        entityValue.addValues(buildValues(entityClass.fields()));

        IEntityClass extendEntityClass = entityClass.extendEntityClass();
        if (extendEntityClass != null) {
            entityValue.addValues(buildValues(extendEntityClass.fields()));
        }

        return new Entity(entityId, entityClass, entityValue, 0);
    }

    private Collection<IValue> buildValues(Collection<IEntityField> fields) {
        return fields.stream().map(f -> {
            switch (f.type()) {
                case LONG:
                    return new LongValue(f, (long) buildRandomLong(0, 1000));
                case STRING:
                    return new StringValue(f, buildRandomString(5));
                case DECIMAL:
                    return new DecimalValue(
                            f,
                            new BigDecimal(
                                    Long.toString(buildRandomLong(0, 10))
                                            + "."
                                            + Long.toString(buildRandomLong(0, 100))
                            )
                    );
                default:
                    throw new IllegalStateException("Error " + f.type().name());
            }
        }).collect(Collectors.toList());
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }
}
