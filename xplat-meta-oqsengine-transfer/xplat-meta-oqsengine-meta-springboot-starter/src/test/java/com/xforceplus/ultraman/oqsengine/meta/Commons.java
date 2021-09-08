package com.xforceplus.ultraman.oqsengine.meta;

import static com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement.ElementStatus.Register;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

/**
 * desc :.
 * name : Commons
 *
 * @author : xujia
 * @since : 1.8
 */
public class Commons {
    public static final boolean IF_TEST = false;

    public static final Map<String, WatchElement> CASES = new LinkedHashMap<>();

    /**
     * case heartBeat & confirmed.
     */
    public static final String CASE_HEAR_BEAT = "caseHearBeat";
    public static final WatchElement WATCH_ELEMENT_HEART_BEAT =
        new WatchElement(CASE_HEAR_BEAT, "test", 0, Register);

    /**
     * case registerPull then sync-ok.
     */
    public static final String CASE_REGISTER_PULL = "caseRegisterPull";
    public static final WatchElement WATCH_ELEMENT_REGISTER_PULL =
        new WatchElement(CASE_REGISTER_PULL, "test", -1, Register);

    /**
     * case registerPush then sync-failed at once and triggered retry and sync-ok last.
     */
    public static final String CASE_REGISTER_PUSH = "caseRegisterPush";
    public static final WatchElement WATCH_ELEMENT_REGISTER_PUSH =
        new WatchElement(CASE_REGISTER_PUSH, "test", 1, Register);

    /**
     * case sync result from client to server time-out.
     */
    public static final String CASE_SYNC_RESULT_TIME_OUT = "caseSyncResultTimeOut";
    public static final WatchElement WATCH_ELEMENT_SYNC_RESULT_TIME_OUT =
        new WatchElement(CASE_SYNC_RESULT_TIME_OUT, "test", -1, Register);


    /*
     * case register no response.
     * case reconnect.
     * case heartBeat timeout.
     * 手动观察.
     */
    static {
        CASES.put(CASE_HEAR_BEAT, WATCH_ELEMENT_HEART_BEAT);
        CASES.put(CASE_REGISTER_PULL, WATCH_ELEMENT_REGISTER_PULL);
        CASES.put(CASE_REGISTER_PUSH, WATCH_ELEMENT_REGISTER_PUSH);
        CASES.put(CASE_SYNC_RESULT_TIME_OUT, WATCH_ELEMENT_SYNC_RESULT_TIME_OUT);
    }

    /**
     * 断言.
     */
    public static boolean assertWatchElement(String caseName, WatchElement.ElementStatus appStatus, WatchElement w) {
        Assertions.assertEquals(w.getAppId(), Commons.CASES.get(caseName).getAppId());
        Assertions.assertEquals(w.getEnv(), Commons.CASES.get(caseName).getEnv());
        Assertions.assertEquals(w.getVersion(), Commons.CASES.get(caseName).getVersion());
        Assertions.assertEquals(w.getStatus(), appStatus);
        return true;
    }

}
