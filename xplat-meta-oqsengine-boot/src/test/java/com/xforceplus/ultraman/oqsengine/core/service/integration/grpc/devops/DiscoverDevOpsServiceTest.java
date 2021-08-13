package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.DiscoverDevOpsService;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;
import com.xforceplus.ultraman.oqsengine.testcontainer.basic.AbstractContainerExtends;
import javax.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Created by justin.xu on 08/2021.
 *
 * @since 1.8
 */
@ActiveProfiles("discover")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DiscoverDevOpsServiceTest extends AbstractContainerExtends {

    @Autowired
    private DiscoverDevOpsService discoverDevOpsService;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

    @MockBean(name = "keyValueStorage")
    private KeyValueStorage keyValueStorage;

    boolean waitForDebug = false;

    @Test
    public void test() throws InterruptedException {
        if (waitForDebug) {
            Thread.sleep(10000_000);
        } else {
            Thread.sleep(5_000);
        }

    }
}
