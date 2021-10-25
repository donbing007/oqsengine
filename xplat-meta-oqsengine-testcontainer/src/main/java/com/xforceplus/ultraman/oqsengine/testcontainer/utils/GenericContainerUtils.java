package com.xforceplus.ultraman.oqsengine.testcontainer.utils;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public class GenericContainerUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(GenericContainerUtils.class);

    /**
     * 关闭container.
     *
     * @param genericContainer 目标容器.
     */
    public static void genericClose(GenericContainer genericContainer) {
        genericContainer.close();
        /**
         * 如果容器处于running状态，将一直轮询.
         */
        while (genericContainer.isRunning()) {
            try {
                LOGGER
                    .info("The {} container is not closed, etc. 5 ms.",
                        genericContainer.getDockerImageName());
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
}
