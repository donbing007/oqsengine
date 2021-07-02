package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.getEntityClass;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.xforceplus.ultraman.oqsengine.cdc.CDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc :.
 * name : BigBatchSyncTest
 *
 * @author : xujia 2020/11/23
 * @since : 1.8
 */

public class BigBatchSyncTest extends CDCTestHelper {
    final Logger logger = LoggerFactory.getLogger(BigBatchSyncTest.class);

    private static int expectedSize = 0;
    private static int maxTestSize = 10;


    @BeforeEach
    public void before() throws Exception {
        super.init(true);
    }

    @AfterEach
    public void after() throws Exception {
        super.destroy(true);
    }

    @Test
    public void test() throws Exception {
        initData();

        boolean isStartUpdate = false;
        long start = 0;
        long duration = 0;
        while (true) {
            if (!isStartUpdate && mockRedisCallbackService.getExecuted().get() > ZERO) {
                start = System.currentTimeMillis();
                isStartUpdate = true;
            }
            if (mockRedisCallbackService.getExecuted().get() < expectedSize) {
                Thread.sleep(1_000);
            } else {
                duration = System.currentTimeMillis() - start;
                break;
            }
        }

        Assertions.assertEquals(expectedSize, mockRedisCallbackService.getExecuted().get());
        logger.info("total build use time, {}", duration);

        mockRedisCallbackService.reset();
        Thread.sleep(5_000);
        Assertions.assertEquals(ZERO, mockRedisCallbackService.getExecuted().get());
    }

    private void initData() throws Exception {
        try {
            int i = 1;
            for (; i < maxTestSize; ) {
                IEntity[] entities = EntityGenerateToolBar.generateFixedEntities(i, 0);
                for (IEntity entity : entities) {
                    MasterDBInitialization.getInstance().getMasterStorage().build(entity, getEntityClass(entity.entityClassRef().getId()));
                }
                expectedSize += entities.length;
                i += entities.length;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}
