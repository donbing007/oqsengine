package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityRepo;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CDCRunnerTest extends AbstractCdcHelper {

    private RunnerContext runnerContext;

    private Thread asyncTask;

    @BeforeEach
    public void before() throws Exception {
        super.init(true, null);
        runnerContext = cdcRunner.getContext();
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(true);
    }

    @AfterAll
    public static void afterAll() {
        try {
            InitializationHelper.destroy();
        } catch (Exception e) {

        }
    }

    @Test
    public void runTest() throws Exception {
        long insertSize = 512;
        long expectedStartId = 1;

        AtomicBoolean rs = new AtomicBoolean(false);

        asyncTask = new Thread( () -> {
            long batchId = -1;

            while (true) {

                if (batchId != runnerContext.getCdcMetrics().getBatchId()) {
                    batchId = runnerContext.getCdcMetrics().getBatchId();
                }

                if (insertSize == runnerContext.totalExecutedRecords()) {
                    rs.set(true);
                    break;
                }

                TimeWaitUtils.wakeupAfter(1, TimeUnit.SECONDS);
            }
        });

        asyncTask.start();

        List<IEntity> expectedEntities =
            initDynamicData(expectedStartId, insertSize, EntityRepo.DYNAMIC_ENTITY_CASES);

        Assertions.assertEquals(insertSize, expectedEntities.size());


        int fails = 0;
        int maxFails = 10;

        while(!rs.get()) {
            TimeWaitUtils.wakeupAfter(1, TimeUnit.SECONDS);

            Assertions.assertTrue(fails < maxFails);

            fails++;
        }
    }


    private List<IEntity> initDynamicData(long startId, long insertSize, List<Tuple2<IEntityClass, List<IEntityField>>> cases) {

        List<IEntity> entities = new ArrayList<>();

        try {
            for (int i = 0; i < insertSize; i++) {
                long pos = i + startId;
                int indexCase = i % cases.size();

                Tuple2<IEntityClass, List<IEntityField>> c = cases.get(indexCase);

                IEntity entity = EntityBuilder.buildEntity(pos, c._1(), c._2());

                if (MasterDBInitialization
                    .getInstance().getMasterStorage().build(entity, c._1())) {
                    entities.add(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entities;
    }
}
