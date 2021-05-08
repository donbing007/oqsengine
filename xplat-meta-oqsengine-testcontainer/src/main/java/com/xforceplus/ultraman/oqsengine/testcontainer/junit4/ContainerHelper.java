package com.xforceplus.ultraman.oqsengine.testcontainer.junit4;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.runners.model.TestClass;

/**
 * 启动容器帮助类.
 *
 * @author dongbin
 * @version 0.1 2020/12/25 18:32
 * @since 1.8
 */
public class ContainerHelper {

    /**
     * 解析测试类需要容器类型.
     *
     * @param testClass 测试目标类.
     * @return 容器类型列表.
     */
    public static ContainerType[] parseContainers(TestClass testClass) {
        DependentContainers dc = testClass.getAnnotation(DependentContainers.class);
        if (dc == null) {
            return new ContainerType[0];
        } else {
            return dc.value();
        }
    }

    /**
     * 启动/关闭目标容器.
     *
     * @param type 容器类型.
     * @param start true启动,false关闭.
     */
    public static void processContainer(ContainerType type, boolean start) {
        switch (type) {
            case REDIS: {
                if (start) {
                    ContainerStarter.startRedis();
                } else {
                    ContainerStarter.stopRedis();
                }
                break;
            }
            case MYSQL: {
                if (start) {
                    ContainerStarter.startMysql();
                } else {
                    ContainerStarter.stopMysql();
                }
                break;
            }
            case MANTICORE: {
                if (start) {
                    ContainerStarter.startManticore();
                } else {
                    ContainerStarter.stopManticore();
                }
                break;
            }
            case CANNAL: {
                if (start) {
                    ContainerStarter.startCannal();
                } else {
                    ContainerStarter.stopCannal();
                }
                break;
            }
            default:
                throw new IllegalArgumentException(String.format("%s is an unsupported container type."));
        }
    }
}
