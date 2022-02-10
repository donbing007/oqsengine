package com.xforceplus.ultraman.oqsengine.testcontainer.container;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public abstract class AbstractContainerExtension implements BeforeAllCallback, AfterAllCallback {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractContainerExtension.class);

    /**
     * 一个boolean值环境变量,如果是true表示在CI环境中,否则不是.
     */
    private static final String CI_RUNTIME = "OQS_CI_RUNTIME";

    // 启动错误的最大重试次数.
    private static final int MAX_TRY_NUMBER = 6;

    // 启动错误再次尝试的等待时间.
    private static final int REPLAY_WAIT_TIME_MS = 1000 * 60;

    // 全局名称.
    private static final String GLOBAL_NAME = UUID.randomUUID().toString();

    /**
     * 每个测试用例类开启执行前执行.
     *
     * @param extensionContext 上下文.
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) {

        LOGGER.info("Start the container {}...(ci = {})", containerSupport().name(), isCiRuntime());

        GenericContainer container;
        // 容器启动错误,重试最多 MAX_TRY_NUMBER 次数.
        for (int i = 0; i < MAX_TRY_NUMBER; i++) {

            container = buildContainer();

            if (Global.startContainer(container)) {

                container.followOutput((Consumer<OutputFrame>) outputFrame -> {
                    LOGGER.info(outputFrame.getUtf8String());
                });

                init();

                LOGGER.info("Start the container {}...(ci = {}) OK!", containerSupport().name(), isCiRuntime());

                return;
            } else {

                LOGGER.info("Failed to start container {} (ci = {}), wait {} seconds and try again.[{}/{}]",
                    containerSupport().name(), isCiRuntime(),
                    TimeUnit.MILLISECONDS.toSeconds(REPLAY_WAIT_TIME_MS), i + 1,
                    MAX_TRY_NUMBER);

                LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(REPLAY_WAIT_TIME_MS));
            }
        }

        throw new IllegalStateException(String.format("Failed to start container %s.", containerSupport().name()));
    }


    /**
     * 每个测试用例类执行完毕退出前执行.
     */
    @Override
    public void afterAll(ExtensionContext extensionContext) {

        LOGGER.info("Close the container {}...", containerSupport().name());

        clean();
        Global.closeContainer(getGenericContainer());

        LOGGER.info("Close the container {}...OK!", containerSupport().name());
    }

    /**
     * 当前是否处于CI环境.
     *
     * @return true 是, false 不是.
     */
    public boolean isCiRuntime() {
        String value = System.getenv(CI_RUNTIME);
        return Boolean.parseBoolean(value);
    }

    /**
     * 全局名称.
     *
     * @return 全局名称.
     */
    public static String globalName() {
        return GLOBAL_NAME;
    }

    /**
     * 构造一个网络别名.
     *
     * @param name 原始名称.
     * @return 别名.
     */
    public static String buildAliase(String name) {
        return String.format("%s-%s", GLOBAL_NAME, name);
    }

    /**
     * 构建子容器，由子类来实现.
     *
     * @return 容器实例
     */
    protected abstract GenericContainer buildContainer();

    /**
     * 初始化.
     */
    protected abstract void init();

    /**
     * 构建子容器，由子类来实现.
     *
     * @return 容器实例
     */
    protected abstract void clean();

    /**
     * 返回容器类型.
     *
     * @return 支持的容器类型.
     */
    protected abstract ContainerSupport containerSupport();

    /**
     * 获得当前的容器.
     *
     * @return 容器.
     */
    protected abstract GenericContainer getGenericContainer();
}
