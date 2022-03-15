package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.NO_TRANSACTION_COMMIT_ID;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.mock.ErrorConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.batch.BatchInit;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.rebuild.EntityGenerateTooBar;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ErrorConsumerRunnerTest extends AbstractCdcHelper {

    ConsumerService consumerService;

    private static List<IEntity> caseEntities =
        EntityGenerateTooBar.prepareEntities(1, 50, EntityClassBuilder.ENTITY_CLASS_2);

    @BeforeEach
    public void before() throws Exception {
        super.init(true, null);
        useError();
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(true);
        useReal();
    }

    @BeforeAll
    public static void beforeAll() {
        BatchInit.init();
    }

    @AfterAll
    public static void afterAll() {
        try {
            BatchInit.destroy();
            InitializationHelper.destroy();
        } catch (Exception e) {
        }
    }

    @Test
    public void startTest() throws Exception {

        Thread thread = new Thread(
            () -> {
                try {
                    boolean initOk = BatchInit.initData(caseEntities, EntityClassBuilder.ENTITY_CLASS_2, 50);

                    Assertions.assertTrue(initOk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        );

        thread.start();

        int tryTime = 0;
        int maxTry = 50;

        while (true) {
            if (((ErrorConsumerService) consumerService).isFinishTest()){
                //  首先判断数量是否一致
                Assertions.assertEquals(caseEntities.size(), ((ErrorConsumerService) consumerService).getErrors().size());

                List<String> keys = new ArrayList<>();
                for (int i = 0; i < caseEntities.size(); i ++) {
                    IEntity entity = caseEntities.get(i);

                    String key = NO_TRANSACTION_COMMIT_ID + "_" + entity.id() + "_" + i;
                    keys.add(key);

                    Assertions.assertNotNull(((ErrorConsumerService) consumerService).getErrors().remove(key));
                }

                Collection<CdcErrorTask> errorTasks = CdcInitialization.getInstance().getCdcErrorStorage().queryCdcErrors(keys);

                Assertions.assertEquals(caseEntities.size(), errorTasks.size());

                break;
            }

            Assertions.assertTrue(tryTime++ < maxTry);
            TimeWaitUtils.wakeupAfter(1, TimeUnit.SECONDS);
        }
    }

    private void useError() throws Exception {
        consumerService = new ErrorConsumerService();
        ((ErrorConsumerService) consumerService).init(
            CdcInitialization.getInstance().getErrorRecorder(), CdcInitialization.getInstance().getCdcMetricsHandler());

        CdcInitialization.getInstance().resetConsumerService(consumerService);
    }

    private void useReal() throws Exception {
        consumerService = CdcInitialization.getInstance().initConsumer();

        CdcInitialization.getInstance().resetConsumerService(consumerService);
    }
}
