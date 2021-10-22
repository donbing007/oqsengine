package com.xforceplus.ultraman.oqsengine.testcontainer.container.impl;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.AbstractContainerExtension;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.FixedContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.RemoteCallUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
        ContainerWrapper containerWrapper = null;

        if (null != uid) {
            containerWrapper = RemoteCallUtils.startUseRemoteContainer(uid, containerSupport());

            if (null == containerWrapper) {
                throw new RuntimeException("get remote container failed.");
            }
            /*
             * 设置oqs中的环境变量
             */
            setSystemProperties(containerWrapper.host(), containerWrapper.port());
        } else {
            GenericContainer manticore = new GenericContainer<>("manticoresearch/manticore:3.5.4")
                .withExposedPorts(9306)
                .withNetwork(Global.NETWORK)
                .withNetworkAliases("manticore")
                .withClasspathResourceMapping("manticore.conf", "/manticore.conf", BindMode.READ_ONLY)
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

            containerWrapper = new FixedContainerWrapper(manticore);

            try {
                init();
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }

        }

        return containerWrapper;
    }

    private void init() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        File path = new File(ManticoreContainer.class.getResource("/manticore").getPath());
        String[] sqlFiles = path.list((dir, name) -> {
            String[] names = name.split("\\.");
            if (names.length == 2 && names[1].equals("sql")) {
                return true;
            }
            return false;
        });

        List<String> sqls = new ArrayList();
        for (String file : sqlFiles) {
            String fullPath = String.format("%s%s%s", path.getAbsolutePath(), File.separator, file);
            LOGGER.info("Reader manticore sql file: {}", fullPath);
            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(fullPath), "utf8"))) {
                String line;
                StringBuilder buff = new StringBuilder();
                while ((line = in.readLine()) != null) {
                    buff.append(line);
                    if (buff.charAt(buff.length() - 1) == ';') {
                        buff.deleteCharAt(buff.length() - 1);
                        sqls.add(buff.toString());

                        LOGGER.info(buff.toString());

                        buff.delete(0, buff.length());
                    }
                }
            }
        }

        try (Connection conn = DriverManager.getConnection(System.getProperty("MANTICORE_JDBC"))) {
            try (Statement statement = conn.createStatement()) {
                for (String sql : sqls) {
                    statement.execute(sql);
                }
            }
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
