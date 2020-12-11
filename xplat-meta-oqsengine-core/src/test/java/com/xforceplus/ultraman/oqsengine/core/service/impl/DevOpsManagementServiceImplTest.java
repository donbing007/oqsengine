package com.xforceplus.ultraman.oqsengine.core.service.impl;

import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.summary.OffsetSnapShot;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DecimalValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import com.xforceplus.ultraman.oqsengine.storage.master.iterator.QueryIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

/**
 * desc :
 * name : DevOpsManagementServiceImplTest
 *
 * @author : xujia
 * date : 2020/12/11
 * @since : 1.8
 */
public class DevOpsManagementServiceImplTest {
    private IEntityClass fatherEntityClass = new EntityClass(1, "father", Arrays.asList(
            new EntityField(1, "f1", FieldType.LONG, FieldConfig.build().searchable(true)),
            new EntityField(2, "f2", FieldType.STRING, FieldConfig.build().searchable(false)),
            new EntityField(3, "f3", FieldType.DECIMAL, FieldConfig.build().searchable(true))
    ));

    private IEntityClass childEntityClass = new EntityClass(
            2,
            "chlid",
            null,
            null,
            fatherEntityClass,
            Arrays.asList(
                    new EntityField(4, "c1", FieldType.LONG, FieldConfig.build().searchable(true))
            )
    );

    private List<IEntity> entities;

    @Before
    public void before() throws Exception {
        entities = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            entities.add(
                    new Entity(2000, childEntityClass, new EntityValue(2000).addValues(
                            Arrays.asList(
                                    new LongValue(fatherEntityClass.field("f1").get(), 10000L),
                                    new StringValue(fatherEntityClass.field("f2").get(), "v1"),
                                    new DecimalValue(fatherEntityClass.field("f3").get(), new BigDecimal("123.456")),
                                    new LongValue(childEntityClass.field("c1").get(), 20000L)
                            )
                    ), new EntityFamily(1000, 0), 0, OqsVersion.MAJOR)
            );
        }
    }

    @After
    public void after() throws Exception {
        entities = null;
    }

    @Test
    public void testRepair() throws Exception {
        ExecutorService worker = Executors.newFixedThreadPool(10);
        MasterStorage masterStorage = mock(MasterStorage.class);
        when(masterStorage.newIterator(childEntityClass, 0, Long.MAX_VALUE, worker, 0, 100))
                .thenReturn(new MockQueryIterator());

        EntityManagementService entityManagementService = mock(EntityManagementService.class);
        when(entityManagementService.replace(argThat(argument -> true))).thenReturn(ResultStatus.SUCCESS);

        DevOpsManagementServiceImpl impl = new DevOpsManagementServiceImpl();
        ReflectionTestUtils.setField(impl, "worker", worker);
        ReflectionTestUtils.setField(impl, "masterStorage", masterStorage);
        ReflectionTestUtils.setField(impl, "entityManagementService", entityManagementService);

        impl.entityRepair(childEntityClass);

        // wait done
        while (!impl.isEntityRepaired()) {
        }

        verify(masterStorage, times(1))
                .newIterator(childEntityClass, 0, Long.MAX_VALUE, worker, 0, 100);
        verify(entityManagementService, times(entities.size())).replace(argThat(argument -> true));

        worker.shutdown();
    }

    class MockQueryIterator implements QueryIterator {

        private int point;

        @Override
        public int size() {
            return entities.size();
        }

        @Override
        public boolean hasNext() {
            return point < entities.size();
        }

        @Override
        public List<IEntity> next() throws SQLException {
            final int batchSize = 10;
            List<IEntity> buff = new ArrayList<>(batchSize);
            for (; point < entities.size(); point++) {
                buff.add(entities.get(point));
            }
            return buff;
        }

        @Override
        public OffsetSnapShot snapShot() {
            return null;
        }

        @Override
        public boolean resetCheckPoint(OffsetSnapShot offsetSnapShot) {
            return false;
        }
    }
}