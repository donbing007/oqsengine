package com.xforceplus.ultraman.oqsengine.meta;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;

/**
 * desc :
 * name : Commons
 *
 * @author : xujia
 * date : 2021/3/5
 * @since : 1.8
 */
public class Commons {
    public static final boolean IF_TEST = false;

    public static final Map<String, WatchElement> cases = new LinkedHashMap<>();

    /**
     * case heartBeat & confirmed
     */
    public static final String caseHeartBeat = "caseHearBeat";
    public static final WatchElement watchElementHeartBeat = new WatchElement(caseHeartBeat, "test", 0, Register);

    /**
     * case registerPull then sync-ok
     */
    public static final String caseRegisterPull = "caseRegisterPull";
    public static final WatchElement watchElementRegisterPull = new WatchElement(caseRegisterPull, "test", -1, Register);

    /**
     * case registerPush then sync-failed at once and triggered retry and sync-ok last
     */
    public static final String caseRegisterPush = "caseRegisterPush";
    public static final WatchElement watchElementRegisterPush = new WatchElement(caseRegisterPush, "test", 1, Register);

    /**
     * case sync result from client to server time-out
     */
    public static final String caseSyncResultTimeOut = "caseSyncResultTimeOut";
    public static final WatchElement watchElementSyncResultTimeOut = new WatchElement(caseSyncResultTimeOut, "test", -1, Register);


    /**
     * case register no response
     * case reconnect
     * case heartBeat timeout
     * 手动观察
     */
    static {
        cases.put(caseHeartBeat, watchElementHeartBeat);
        cases.put(caseRegisterPull, watchElementRegisterPull);
        cases.put(caseRegisterPush, watchElementRegisterPush);
        cases.put(caseSyncResultTimeOut, watchElementSyncResultTimeOut);
    }

    public static boolean assertWatchElement(String caseName, WatchElement.ElementStatus appStatus, WatchElement w) {
        Assertions.assertEquals(w.getAppId(), Commons.cases.get(caseName).getAppId());
        Assertions.assertEquals(w.getEnv(), Commons.cases.get(caseName).getEnv());
        Assertions.assertEquals(w.getVersion(), Commons.cases.get(caseName).getVersion());
        Assertions.assertEquals(w.getStatus(), appStatus);
        return true;
    }

}
