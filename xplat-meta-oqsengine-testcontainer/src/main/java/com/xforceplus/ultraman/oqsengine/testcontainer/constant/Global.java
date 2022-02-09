package com.xforceplus.ultraman.oqsengine.testcontainer.constant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class Global {
    private static final Logger LOGGER = LoggerFactory.getLogger(Global.class);
    public static final int WAIT_START_TIME_OUT = 200;

    private static Network network;

    private static int containerSize = 0;

    /**
     * 启动容器.
     *
     * @param container 目标容器.
     */
    public static synchronized boolean startContainer(GenericContainer container) {
        try {
            containerSize++;

            if (network == null) {
                LOGGER.info("The first container is created, creating the network.");
                network = Network.newNetwork();
            }

            container.withNetwork(network);
            container.start();
        } catch (Throwable ex) {

            survey(ex);

            LOGGER.error(ex.getMessage(), ex);

            containerSize--;

            container.close();

            releaseNetwork();

            return false;
        }

        return true;
    }

    // 为了检查问题,打印错误可能的现场信息.
    private static void survey(Throwable ex) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        ex.printStackTrace(pout);
        String msg = new String(out.toByteArray());

        LOGGER.warn("There was an error starting the container. The following is the field information.\n [{}]", msg);

        final String target = "proxy: listen tcp 0.0.0.0:";
        final char endChar = ':';
        int index = msg.indexOf(target);
        StringBuilder buff = new StringBuilder();
        if (index > -1) {
            char point;
            for (int i = index; i < msg.length(); i++) {
                point = msg.charAt(i);
                if (endChar == point) {
                    break;
                } else {
                    buff.append(point);
                }
            }

            String command = String.format("netstat -a | grep %s", buff.toString());
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(command);
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                buff.delete(0, buff.length());
                String line;
                while ((line = reader.readLine()) != null) {
                    buff.append(line).append('\n');
                }

                LOGGER.warn(buff.toString());

            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                return;
            } finally {
                if (process != null) {
                    process.destroy();
                }
            }
        }
    }

    /**
     * 关闭容器.
     *
     * @param container 目标容器.
     */
    public static synchronized void closeContainer(GenericContainer container) {
        containerSize--;

        container.close();

        while (container.isRunning()) {
            try {
                LOGGER.info("The {} container is not closed, etc. 5 ms.", container.getDockerImageName());
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }

        releaseNetwork();
    }

    private static void releaseNetwork() {
        if (containerSize <= 0 && network != null) {

            LOGGER.info("The last container is closed, shutting down the network.");

            network.close();
            network = null;
        }
    }
}
