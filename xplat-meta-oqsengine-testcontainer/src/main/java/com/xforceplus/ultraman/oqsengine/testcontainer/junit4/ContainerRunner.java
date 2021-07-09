//package com.xforceplus.ultraman.oqsengine.testcontainer.junit4;
//
//import org.junit.runner.notification.RunNotifier;
//import org.junit.runners.BlockJUnit4ClassRunner;
//import org.junit.runners.model.InitializationError;
//
///**
// * 容器启动的Junit4 Runner实现.
// * 会在每一个测试开始启动由DependenContainers指定的容器.
// * 容器启动顺序以定义的顺序为准.
// * 执行后会关闭容器.
// *
// * @author dongbin
// * @version 0.1 2020/12/25 17:24
// * @since 1.8
// */
//public class ContainerRunner extends BlockJUnit4ClassRunner {
//    /**
//     * 创建一个容器的测试类.
//     *
//     * @param klass 目标类型.
//     * @throws InitializationError if the test class is malformed.
//     */
//    public ContainerRunner(Class<?> klass) throws InitializationError {
//        super(klass);
//    }
//
//    @Override
//    public void run(RunNotifier notifier) {
//        ContainerType[] containerTypes = ContainerHelper.parseContainers(getTestClass());
//        boolean start = false;
//        if (containerTypes != null && containerTypes.length > 0) {
//            start = true;
//
//            for (ContainerType containerType : containerTypes) {
//                ContainerHelper.processContainer(containerType, true);
//            }
//        }
//
//        super.run(notifier);
//
//        if (start) {
//            for (int i = containerTypes.length - 1; i >= 0; i--) {
//                ContainerHelper.processContainer(containerTypes[i], false);
//            }
//        }
//    }
//}
