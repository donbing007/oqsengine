package com.xforceplus.ultraman.oqsengine.testcontainer.junit4;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

/**
 * 容器启动的Junit4 Runner实现.
 * 会在每一个测试开始启动由DependenContainers指定的容器.
 * 容器启动顺序以定义的顺序为准.
 * 执行后会关闭容器.
 *
 * @author dongbin
 * @version 0.1 2020/12/25 17:24
 * @since 1.8
 */
public class ContainerRunner extends BlockJUnit4ClassRunner {
    /**
     * 创建一个容器的测试类.
     *
     * @param klass 目标类型.
     * @throws InitializationError if the test class is malformed.
     */
    public ContainerRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        ContainerStarter starter = null;
        try {
            ContainerType[] containerTypes = parseContainers(getTestClass());
            if (containerTypes.length > 0) {
                starter = new ContainerStarter();
                starter.init();

                for (ContainerType containerType : containerTypes) {
                    startContainer(containerType, starter);
                }
            }

            super.run(notifier);

        } finally {

            if (starter != null) {
                starter.destroy();
            }

        }
    }

    // 解析需要的目标容器类型.
    private ContainerType[] parseContainers(TestClass testClass) {
        DependentContainers dc = testClass.getAnnotation(DependentContainers.class);
        if (dc == null) {
            return new ContainerType[0];
        } else {
            return dc.value();
        }
    }

    // 启动指定类型容器.
    private void startContainer(ContainerType type, ContainerStarter starter) {
        switch (type) {
            case REDIS: {
                starter.startRedis();
                break;
            }
            case MYSQL: {
                starter.startMysql();
                break;
            }
            case MANTICORE: {
                starter.startManticore();
                break;
            }
            case CANNAL: {
                starter.startCannal();
                break;
            }
            default:
                throw new IllegalArgumentException(String.format("%s is an unsupported container type."));
        }
    }
}
