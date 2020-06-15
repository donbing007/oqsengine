package com.xforceplus.ultraman.oqsengine.sdk;

import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.DefaultEndpointConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.RuntimeConfigAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.StorageAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.listener.ConfigListener;
import com.xforceplus.xplat.galaxy.framework.configuration.AsyncTaskExecutorAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceDispatcherAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceInvokerAutoConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {TestConfiguration.class
        , InitServiceAutoConfiguration.class
        , DefaultEndpointConfiguration.class
        , AuthSearcherConfig.class
        , ServiceInvokerAutoConfiguration.class
        , AsyncTaskExecutorAutoConfiguration.class
        , ServiceDispatcherAutoConfiguration.class
        , com.xforceplus.xplat.galaxy.framework.configuration.ContextConfiguration.class
        , RuntimeConfigAutoConfiguration.class
        , RestTemplateAutoConfiguration.class
})
public class ContextWareBaseTest {

    @Test
    public void testMock(){

    }
}
