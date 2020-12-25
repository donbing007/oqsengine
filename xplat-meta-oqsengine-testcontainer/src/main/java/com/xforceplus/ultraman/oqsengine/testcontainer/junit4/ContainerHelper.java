package com.xforceplus.ultraman.oqsengine.testcontainer.junit4;

import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.runners.model.TestClass;

/**
 * @author dongbin
 * @version 0.1 2020/12/25 18:32
 * @since 1.8
 */
public class ContainerHelper {

    public static ContainerType[] parseContainers(TestClass testClass) {
        DependentContainers dc = testClass.getAnnotation(DependentContainers.class);
        if (dc == null) {
            return new ContainerType[0];
        } else {
            return dc.value();
        }
    }

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
