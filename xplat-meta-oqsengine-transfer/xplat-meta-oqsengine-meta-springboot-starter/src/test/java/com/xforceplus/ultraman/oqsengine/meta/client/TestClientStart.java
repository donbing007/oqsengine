package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.Commons;
import com.xforceplus.ultraman.oqsengine.meta.SpringBootApp;
import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.ThreadUtils;
import com.xforceplus.ultraman.oqsengine.meta.executor.RequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static com.xforceplus.ultraman.oqsengine.meta.Commons.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Confirmed;

/**
 * desc :
 * name : TestClientStart
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@ActiveProfiles("client")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringBootApp.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestClientStart {

    private Logger logger = LoggerFactory.getLogger(TestClientStart.class);

    @Resource
    private IRequestHandler requestHandler;

    @Resource
    private RequestWatchExecutor requestWatchExecutor;

    @Autowired
    private MockSyncExecutor mockSyncExecutor;

    boolean ifTest = false;

    private static final Map<String, BiFunction<String, WatchElement, Boolean>> functions = new HashMap<>();

    @Before
    public void before() throws InterruptedException {
        if (ifTest) {
            Thread.sleep(1_000);

            functions.put(caseHeartBeat, this::heartBeatTest);
            functions.put(caseRegisterPull, this::registerPullTest);
            functions.put(caseRegisterPush, this::registerPushTest);
        }
    }

    @After
    public void after() throws InterruptedException {
        if (ifTest) {
            Thread.sleep(1_000);
        }
    }


    @Test
    public void test() throws InterruptedException {
        if (ifTest) {
            Thread.sleep(5_000);
            for (Map.Entry<String, WatchElement> e : Commons.cases.entrySet()) {
                BiFunction<String, WatchElement, Boolean> f = functions.get(e.getKey());
                ThreadUtils.create(() -> {
                    try {
                        if (null != f) {
                            logger.info(String.format("start test [%s]...", e.getKey()));
                            Assert.assertTrue(f.apply(e.getKey(), e.getValue()));
                            logger.info(String.format("successful test [%s]...", e.getKey()));
                        }
                    } catch (Exception ex) {
                        logger.warn(ex.getMessage());
                    }
                    return true;
                }).start();

                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * case heartBeat
     * @param caseName
     * @param w
     * @return
     */
    public boolean heartBeatTest(String caseName, WatchElement w) {
        String uid = requestWatchExecutor.watcher().uid();

        boolean ret = requestHandler.register(w);

        Assert.assertTrue(ret);

        try {
            Thread.sleep(60_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(uid, requestWatchExecutor.watcher().uid());

        return assertWatchElement(caseHeartBeat, Confirmed,
                requestHandler.watchExecutor().watcher().watches().get(caseName));
    }
    /**
     * case registerPull
     * @param caseName
     * @param w
     * @return
     */
    public boolean registerPullTest(String caseName, WatchElement w) {
        boolean ret = requestHandler.register(w);

        Assert.assertTrue(ret);

        try {
            Thread.sleep(5_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        MockSyncExecutor.RequestStatusVersion requestStatusVersion =
                                    mockSyncExecutor.requestStatusHashMap.get(caseName);

        assertBySyncStatus(caseName, requestStatusVersion, RequestStatus.SYNC_OK);

        return true;
    }

    /**
     * case registerPushTest
     * 注册 1 版本，服务端检测到当前版本与注册版本相同，只返回注册成功结果
     * 服务端等待5秒后推送 2 版本到客户端，客户端当前 2 版本处理失败返回SyncError
     * 告知服务端失败，服务端重推 2 版本数据，此时客户端处理成功，返回成功结果并结束退出
     * 服务端观察当前成功标志是否为confirmed
     *
     * @param caseName
     * @param w
     * @return
     */
    public boolean registerPushTest(String caseName, WatchElement w) {
        try {
            boolean ret = requestHandler.register(w);
            Assert.assertTrue(ret);
            mockSyncExecutor.status = RequestStatus.SYNC_FAIL;

            int expectedVersion = w.getVersion() + 1;

            int loop = 0;
            int max = 50;
            boolean isOk = false;
            MockSyncExecutor.RequestStatusVersion requestStatusVersion = null;
            while (loop < max) {
                logger.info("current version : {}", requestHandler.watchExecutor().watcher().watches().get(caseName).getVersion());
                requestStatusVersion =
                        mockSyncExecutor.requestStatusHashMap.get(caseName);
                if (null != requestStatusVersion && requestStatusVersion.getVersion() > w.getVersion()) {
                    break;
                }
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loop ++;
            }
            Assert.assertNotNull(requestStatusVersion);

            loop = 0;
            while (loop < max) {
                if (requestHandler.watchExecutor().watcher().watches().get(caseName).getVersion() >= expectedVersion) {
                    isOk = true;
                    break;
                }

                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loop ++;
            }
            Assert.assertTrue(isOk);
        } finally {
            mockSyncExecutor.status = RequestStatus.SYNC_OK;
        }
        return true;
    }

    /**
     * case syncResultTimeOutTest
     * @param caseName
     * @param w
     * @return
     */
    public boolean syncResultTimeOutTest(String caseName, WatchElement w) {
        mockSyncExecutor.status = RequestStatus.DATA_ERROR;
        try {
            boolean ret = requestHandler.register(w);
            Assert.assertTrue(ret);

            try {
                Thread.sleep(5_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            MockSyncExecutor.RequestStatusVersion requestStatusVersion =
                                    mockSyncExecutor.requestStatusHashMap.get(caseName);

            assertBySyncStatus(caseName, requestStatusVersion, RequestStatus.DATA_ERROR);

            mockSyncExecutor.status = RequestStatus.SYNC_OK;

            try {
                Thread.sleep(65_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            requestStatusVersion =
                    mockSyncExecutor.requestStatusHashMap.get(caseName);
            assertBySyncStatus(caseName, requestStatusVersion, RequestStatus.SYNC_OK);
            return true;
        } finally {
            mockSyncExecutor.status = RequestStatus.SYNC_OK;
        }
    }

    private void assertBySyncStatus(String caseName, MockSyncExecutor.RequestStatusVersion requestStatusVersion, RequestStatus requestStatus) {
        Assert.assertNotNull(requestStatusVersion);
        Assert.assertEquals(requestHandler.watchExecutor().watcher().watches().get(caseName).getVersion(),
                requestStatusVersion.getVersion());
        Assert.assertEquals(requestStatus, requestStatusVersion.getRequestStatus());
    }
}
