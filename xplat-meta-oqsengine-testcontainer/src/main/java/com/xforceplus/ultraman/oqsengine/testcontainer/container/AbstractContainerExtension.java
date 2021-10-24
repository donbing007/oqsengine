package com.xforceplus.ultraman.oqsengine.testcontainer.container;

import static org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL;

import com.xforceplus.ultraman.oqsengine.testcontainer.constant.Global;
import com.xforceplus.ultraman.oqsengine.testcontainer.enums.ContainerSupport;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.ContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.pojo.FixedContainerWrapper;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.GenericContainerUtils;
import com.xforceplus.ultraman.oqsengine.testcontainer.utils.RemoteCallUtils;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 09/2021.
 *
 * @since 1.8
 */
public abstract class AbstractContainerExtension implements BeforeAllCallback, AfterAllCallback, ExtensionContext.Store.CloseableResource {

    private static final Logger
        LOGGER = LoggerFactory.getLogger(AbstractContainerExtension.class);

    private static String UUID;

    /**
     * 每个测试用例类开启执行前执行.
     * @param extensionContext
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) {

        UUID = System.getProperty("request.uuid");
        LOGGER.info("before all, UUID : {}", UUID);

        Global.CONTAINER_MAP.computeIfAbsent(containerSupport(), (key) -> { return setupContainer(UUID); });

        if (!Global.HOOKED) {
            synchronized (Global.LOCK) {
                if (!Global.HOOKED) {
                    extensionContext.getRoot().getStore(GLOBAL).put("test_container_hook", this);
                    Global.HOOKED = true;
                }
            }
        }
    }


    /**
     * 每个测试用例类执行完毕退出前执行.
     * @param extensionContext
     */
    @Override
    public void afterAll(ExtensionContext extensionContext) {
        /**
         * 这里consumer只能做容器内部数据清理工作
         */
        if (null != UUID) {
            RemoteCallUtils.refreshUseRemoteContainer(UUID);
        }
    }

    /**
     * 将在model执行完毕时调用一次该服务.
     */
    @Override
    public void close() {
        LOGGER.info("Stop container ...");

        AtomicBoolean useRemoteContainer = new AtomicBoolean(false);
        try {
            /**
             * 如果使用远程服务开启容器.
             */
            Global.CONTAINER_MAP.forEach(
                (k, v) -> {
                    containerClose();

                    if (v instanceof FixedContainerWrapper) {
                        GenericContainerUtils.genericClose(((FixedContainerWrapper) v).getGenericContainer());
                    } else {
                        useRemoteContainer.set(true);
                    }
                }
            );
        } finally {
//            /**
//             * 通知远端服务释放资源.
//             */
//            if (useRemoteContainer.get()) {
//                RemoteCallUtils.finishUseRemoteContainer(UUID);
//            }
            Global.CONTAINER_MAP.clear();
        }
    }


    /**
     * 构建子容器，由子类来实现.
     *
     * @return 容器实例
     */
    protected abstract ContainerWrapper setupContainer(String uid);

    /**
     * 构建子容器，由子类来实现.
     *
     * @return 容器实例
     */
    protected abstract void containerClose();

    /**
     * 返回容器类型
     * @return
     */
    protected abstract ContainerSupport containerSupport();
}
