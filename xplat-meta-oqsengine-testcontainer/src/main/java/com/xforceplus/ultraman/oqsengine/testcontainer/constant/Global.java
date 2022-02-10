package com.xforceplus.ultraman.oqsengine.testcontainer.constant;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class Global {
    private static final Logger LOGGER = LoggerFactory.getLogger(Global.class);
    public static final int WAIT_START_TIME_OUT = 200;

//    private static Network network;

    private static int containerSize = 0;

    /**
     * 启动容器.
     *
     * @param container 目标容器.
     */
    public static synchronized boolean startContainer(GenericContainer container) {
        try {
            containerSize++;

//            if (network == null) {
//                LOGGER.info("The first container is created, creating the network.");
//                network = Network.newNetwork();
//            }

//            container.withNetwork(network);
            container.start();
        } catch (Throwable ex) {

            LOGGER.error(ex.getMessage(), ex);

            containerSize--;

            container.close();

            releaseNetwork();

            return false;
        }

        return true;
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
//        if (containerSize <= 0 && network != null) {
//
//            LOGGER.info("The last container is closed, shutting down the network.");
//
//            network.close();
//            network = null;
//        }
    }
}
