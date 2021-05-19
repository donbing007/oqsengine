package com.xforceplus.ultraman.oqsengine.metadata.remote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerRunner;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.ContainerType;
import com.xforceplus.ultraman.oqsengine.testcontainer.junit4.DependentContainers;
import java.util.Optional;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * test.
 *
 * @author j.xu
 * @version 0.1 2021/05/2021/5/17
 * @since 1.8
 */
@RunWith(ContainerRunner.class)
@DependentContainers({ContainerType.REDIS})
public class SyncReadTest extends RemoteBaseRequest {

    private RemoteBase remoteBase;
    public static final boolean IF_TEST = false;

    @Before
    public void before() throws Exception {
        if (IF_TEST) {

            remoteBase = new RemoteBase();
            remoteBase.init();

            baseInit(remoteBase.entityClassSyncExecutor);

            remoteBase.initStorage(requestHandler);

            entityClassSyncClient.start();
        }
    }

    @After
    public void after() {
        if (IF_TEST) {
            entityClassSyncClient.stop();

            remoteBase.clear();
        }
    }


    @Test
    public void testGetFormula() throws JsonProcessingException, InterruptedException {
        if (IF_TEST) {
            try {
                remoteBase.storageMetaManager.need(RemoteConstant.TEST_APP_ID, RemoteConstant.TEST_ENV);
            } catch (Exception e) {

            }
            int count = 0;
            Optional<IEntityClass> entityClassOptional = null;
            while (count < 500) {
                entityClassOptional = remoteBase.storageMetaManager.load(RemoteConstant.TEST_ENTITY_CLASS_ID);
                if (entityClassOptional.isPresent()) {
                    break;
                }
                count++;
                Thread.sleep(1_000);
            }
            Assert.assertTrue(entityClassOptional.isPresent());

            IEntityClass iEntityClass = entityClassOptional.get();
        }
    }
}

