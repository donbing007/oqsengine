package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.SqlInitUtils;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class ManticoreContainer extends AbstractContainerExtension {

    private static final Logger
        LOGGER = LoggerFactory.getLogger(ManticoreContainer.class);

    private GenericContainer container;

    @Override
    protected GenericContainer buildContainer() {
        container = new GenericContainer<>("manticoresearch/manticore:3.5.4")
            .withNetworkAliases(buildAliase("manticore"))
            .withExposedPorts(9306)
            .withClasspathResourceMapping("manticore/manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
            .withCommand("/usr/bin/searchd", "--nodetach", "--config", "/manticore.conf")
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(Global.WAIT_START_TIME_OUT)));

        return container;
    }

    @Override
    protected void init() {
        setSystemProperties(container.getHost(), container.getMappedPort(9306).toString());

        try {
            SqlInitUtils.execute("/manticore", "MANTICORE_JDBC");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    protected void clean() {
        if (this.container != null) {
            try {
                SqlInitUtils.execute("/manticore/drop", "MANTICORE_JDBC");
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    protected ContainerSupport containerSupport() {
        return ContainerSupport.MANTICORE;
    }

    @Override
    protected GenericContainer getGenericContainer() {
        return this.container;
    }

    private void setSystemProperties(String address, String port) {
        if (null == address || null == port) {
            throw new RuntimeException(
                String.format("container manticore init failed of null value, address[%s] or port[%s]", address, port));
        }

        System.setProperty("MANTICORE_HOST", address);
        System.setProperty("MANTICORE_PORT", port);

        System.setProperty("MANTICORE_JDBC",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?"
                    + "characterEncoding=utf8"
                    + "&useHostsInPrivileges=false"
                    + "&useLocalSessionState=true"
                    + "&serverTimezone=Asia/Shanghai",
                address,
                port)
        );

        LOGGER.info("Start manticore server.({}:{})", address, port);
    }
}
