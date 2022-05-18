package com.xforceplus.ultraman.oqsengine.core.service.integration.starter;

import com.typesafe.config.Config;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.boot.config.system.SystemInfoConfiguration;
import com.xforceplus.ultraman.oqsengine.boot.util.SystemInfoConfigUtils;
import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Created by justin.xu on 01/2022.
 *
 * @since 1.8
 */
@ExtendWith({
    RedisContainer.class,
    MysqlContainer.class,
    ManticoreContainer.class,
    CanalContainer.class,
    SpringExtension.class
})
@ActiveProfiles("starter")
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Disabled("错误暂时停止.")
public class StarterTest {

    private ApplicationContext applicationContext;

    @Resource
    private SystemInfoConfiguration systemInfoConfiguration;

    @Resource(name = "metaManager")
    private MetaManager metaManager;

    @BeforeEach
    public void before() throws Exception {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "classpath:oqsengine-ds.conf");
    }

    @Test
    @Disabled
    public void integrationTest() throws InterruptedException {
        while (true) {
            Thread.sleep(1_000);
        }
    }

    @Test
    public void systemInfoTest() {
        Map<String, String> systemInfo = systemInfoConfiguration.printSystemInfo();

        //  cdc
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("cdcHost")));
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("cdcDestination")));
        //  meta
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("metaHost")));
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("metaPort")));
        //  redis
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("redisLettuceUrl")));
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("redissonDataBase")));

        //  index
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("indexSearchName")));
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("indexWriteName")));
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("indexSimpleUri")));
        //  master
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("masterName")));
        Assertions.assertFalse(StringUtils.isEmpty(systemInfo.get("masterSimpleUri")));
    }

    @Test
    public void jdbcConfigUriTest() {
        Config config = DataSourceFactory.getConfig();

        String simpleUri = "";
        List<Config> indexConfigs =
            (List<Config>) config.getConfigList(DataSourceFactory.MASTER_PATH);
        if (null != indexConfigs && indexConfigs.size() > 0) {
            simpleUri = SystemInfoConfigUtils.getJdbcConfigUri(indexConfigs.get(0));
        }

        Assertions.assertTrue(simpleUri.startsWith("jdbc:mysql://"));
    }
}
