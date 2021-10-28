package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.FixedContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.SqlInitUtils;
import java.time.Duration;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class ManticoreContainer extends AbstractContainerExtension {

    private static final Logger
        LOGGER = LoggerFactory.getLogger(ManticoreContainer.class);

    @Override
    protected ContainerWrapper setupContainer(String uid) {
        GenericContainer manticore = new GenericContainer<>("manticoresearch/manticore:3.5.4")
            .withExposedPorts(9306)
            .withNetwork(Global.NETWORK)
            .withNetworkAliases("manticore")
            .withClasspathResourceMapping("manticore/manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));
        manticore.start();
        manticore.followOutput((Consumer<OutputFrame>) outputFrame -> {
            LOGGER.info(outputFrame.getUtf8String());
        });

        /*
         * 设置oqs中的环境变量
         */
        setSystemProperties(manticore.getContainerIpAddress(), manticore.getFirstMappedPort().toString());

        ContainerWrapper containerWrapper = new FixedContainerWrapper(manticore);

        try {
            SqlInitUtils.execute("/manticore", "MANTICORE_JDBC");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return containerWrapper;
    }

    @Override
    protected void containerClose() {
        try {
            SqlInitUtils.execute("/manticore/drop", "MANTICORE_JDBC");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.MANTICORE;
    }

    private void setSystemProperties(String address, String port) {
        if (null == address || null == port) {
            throw new RuntimeException(
                String.format("container manticore init failed of null value, address[%s] or port[%s]", address, port));
        }

        System.setProperty("ds", "./src/test/resources/oqsengine-ds.conf");

        System.setProperty("MANTICORE_HOST", address);
        System.setProperty("MANTICORE_PORT", port);

        System.setProperty("MANTICORE_JDBC",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?characterEncoding=utf8&maxAllowedPacket=512000&"
                    + "useHostsInPrivileges=false&useLocalSessionState=true&serverTimezone=Asia/Shanghai", address,
                port)
        );

        LOGGER.info("Start manticore server.({}:{})", address, port);
    }
}
