package com.xforceplus.ultraman.oqsengine.meta.server;

import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.connect.GRpcServer;
import com.xforceplus.ultraman.oqsengine.meta.dto.AppUpdateEvent;
import com.xforceplus.ultraman.oqsengine.meta.dto.ResponseWatcher;
import com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.SyncResponseHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import static com.xforceplus.ultraman.oqsengine.meta.Commons.*;
import static com.xforceplus.ultraman.oqsengine.meta.executor.ResponseWatchExecutor.keyAppWithEnv;

/**
 * desc :
 * name : TestServerStart
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@ActiveProfiles("server")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestServerStart {

    private Logger logger = LoggerFactory.getLogger(TestServerStart.class);

    @Autowired
    private EntityClassGeneratorTestImpl entityClassGeneratorTest;

    @Resource
    private GRpcServer gRpcServer;

    @Resource
    private ResponseWatchExecutor responseWatchExecutor;

    @Resource
    private SyncResponseHandler syncResponseHandler;

    boolean ifTest = false;

    private Thread[] executors = new Thread[3];

    int max = 300;

    @Before
    public void before() {
        if (ifTest) {
            executors[0] = ThreadUtils.create(() -> heartBeatTest(caseHeartBeat, watchElementHeartBeat));
            executors[1] = ThreadUtils.create(() -> registerPullTest(caseRegisterPull, watchElementRegisterPull));
            executors[2] = ThreadUtils.create(() -> registerPushTest(caseRegisterPush, watchElementRegisterPush));

            for (Thread exec : executors) {
                exec.start();
            }
        }
    }

    @After
    public void after() {
        if (ifTest) {
            for (Thread exec : executors) {
                Assert.assertFalse(exec.isAlive());
            }
        }
    }

    @Test
    public void testStart() throws InterruptedException {
        if (ifTest) {
            int max = 3;
            while (true) {
                int down = 0;
                for (int i = 0; i < max; i++) {
                    if (!executors[i].isAlive()) {
                        down++;
                    }
                }

                if (down == max) {
                    break;
                }

                Thread.sleep(1_000);
            }
        }
    }

    public boolean heartBeatTest(String caseName, WatchElement w) {
        boolean ret = false;
        try {
            setAppEnvVersion(caseName, w.getEnv(), w.getVersion() + 1);
            int loop = 0;
            while (loop < max) {
                Set<String> uidSets = getWatchersByApp(caseName, w.getEnv());
                if (null != uidSets) {
                    for (String uid : uidSets) {
                        ResponseWatcher responseWatcher = getWatchersByApp(uid);
                        if (null != responseWatcher) {
                            WatchElement watchElement = responseWatcher.watches().get(caseName);
                            if (null != watchElement) {
                                Assert.assertEquals(w.getEnv(), watchElement.getEnv());
                                Assert.assertEquals(w.getAppId(), watchElement.getAppId());
                                Assert.assertEquals(w.getVersion(), watchElement.getVersion());
                                ret = true;
                                break;
                            }
                        }
                    }

                    if (ret) {
                        break;
                    }
                }
                Thread.sleep(1_000);
                loop++;
            }

            Assert.assertTrue(ret);
            ResponseWatcher watcher = getWatchersFirst();
            Assert.assertNotNull(watcher);
            logger.info("finish heartBeat test, current uid : {}, watchers : {}", watcher.uid(), watcher.watches().values().toString());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("heartBeat error, message " + e.getMessage());
        }
    }

    public boolean registerPullTest(String caseName, WatchElement w) {
        ResponseWatcher responseWatcher = null;
        int loop = 0;
        while (loop < max) {
            try {
                responseWatcher = getWatchersFirst();
                if (null != responseWatcher && null != responseWatcher.watches().get(caseName)) {
                    break;
                }
                Thread.sleep(1_000);
                loop++;
            } catch (Exception e) {
                //  ignore
            }
        }
        Assert.assertNotNull(responseWatcher);
        WatchElement we = responseWatcher.watches().get(caseName);
        try {
            Assert.assertNotNull(we);
            if (we.getVersion() <= w.getVersion()) {
                Thread.sleep(5_000);
            }
            we = responseWatcher.watches().get(caseName);

            Assert.assertTrue(w.getVersion() < we.getVersion());

            Assert.assertEquals(w.getEnv(), we.getEnv());
            Assert.assertEquals(w.getAppId(), we.getAppId());

            logger.info("finish registerPullTest test, current uid : {}, watchers : {}"
                    , responseWatcher.uid(), responseWatcher.watches().values().toString());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("registerPullTest error, message " + e.getMessage());
        }
    }

    public boolean registerPushTest(String caseName, WatchElement w) {
        ResponseWatcher responseWatcher = null;
        int loop = 0;

        responseWatchExecutor.addVersion(caseName, w.getEnv(), w.getVersion());
        int expectedVersion = w.getVersion() + 1;

        while (loop < max) {
            try {
                responseWatcher = getWatchersFirst();
                if (null != responseWatcher && null != responseWatcher.watches().get(caseName)) {
                    break;
                }
                Thread.sleep(1_000);
                loop++;
            } catch (Exception e) {
                //  ignore
            }
        }

        EntityClassGeneratorTestImpl.version = expectedVersion;

        syncResponseHandler.push(new AppUpdateEvent("mock", caseName, w.getEnv(),
                w.getVersion() + 1, EntityClassSyncRspProto.newBuilder().build()));


        while (loop < max) {
            try {
                responseWatcher = getWatchersFirst();
                if (null != responseWatcher && null != responseWatcher.watches().get(caseName)) {
                    WatchElement.AppStatus appStatus = responseWatcher.watches().get(caseName).getStatus();
                    if (appStatus.equals(WatchElement.AppStatus.Confirmed)) {
                        break;
                    }
                }
                Thread.sleep(1_000);
                loop++;
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }

        Assert.assertNotNull(responseWatcher);

        WatchElement we = responseWatcher.watches().get(caseName);
        Assert.assertNotNull(we);

        Assert.assertEquals(w.getEnv(), we.getEnv());
        Assert.assertEquals(w.getAppId(), we.getAppId());
        Assert.assertTrue(w.getVersion() < we.getVersion());

        Integer ver = null;
        try {
            ver = getAppEnvVersion(caseName, w.getEnv());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("registerPushTest error.");
        }
        Assert.assertNotNull(ver);
        Assert.assertEquals((int) ver, we.getVersion());


        logger.info("finish registerPushTest test, current uid : {}, watchers : {}"
                , responseWatcher.uid(), responseWatcher.watches().values().toString());
        return true;
    }

    private Integer getAppEnvVersion(String appId, String env) throws NoSuchFieldException, IllegalAccessException {
        Map<String, Integer> map = (Map<String, Integer>) getResponseWatchExecutorProperty("appVersionMap");

        if (null != map) {
            return map.get(keyAppWithEnv(appId, env));
        }

        return null;
    }

    private void setAppEnvVersion(String appId, String env, int version) throws NoSuchFieldException, IllegalAccessException {
        Map<String, Integer> map = (Map<String, Integer>) getResponseWatchExecutorProperty("appVersionMap");

        if (null != map) {
            map.put(keyAppWithEnv(appId, env), version);
        }
    }

    private Set<String> getWatchersByApp(String appId, String env) throws NoSuchFieldException, IllegalAccessException {
        Map<String, Set<String>> map = (Map<String, Set<String>>) getResponseWatchExecutorProperty("watchersByApp");

        if (null != map) {
            return map.get(keyAppWithEnv(appId, env));
        }

        return null;
    }

    private ResponseWatcher getWatchersByApp(String uid) throws NoSuchFieldException, IllegalAccessException {
        Map<String, ResponseWatcher> map = (Map<String, ResponseWatcher>) getResponseWatchExecutorProperty("watchers");

        if (null != map) {
            return map.get(uid);
        }

        return null;
    }

    private ResponseWatcher getWatchersFirst() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ResponseWatcher> map = (Map<String, ResponseWatcher>) getResponseWatchExecutorProperty("watchers");


        if (null != map) {
            return map.entrySet().stream().findFirst().map(Map.Entry::getValue).orElse(null);
        }

        return null;
    }

    private Object getResponseWatchExecutorProperty(String name) throws NoSuchFieldException, IllegalAccessException {
        Field field = responseWatchExecutor.getClass().getDeclaredField(name);

        field.setAccessible(true);

        return field.get(responseWatchExecutor);
    }
}
