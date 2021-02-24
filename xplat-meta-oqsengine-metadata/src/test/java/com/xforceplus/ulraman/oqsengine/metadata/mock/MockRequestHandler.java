package com.xforceplus.ulraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.*;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;

import javax.annotation.Resource;
import java.util.*;

import static com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder.*;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.NOT_EXIST_VERSION;

/**
 * desc :
 * name : MockRequestHandler
 *
 * @author : xujia
 * date : 2021/2/20
 * @since : 1.8
 */
public class MockRequestHandler implements IRequestHandler {

    private static final long mockResponseTimeDuration = 5_000;

    public static final int EXIST_MIN_VERSION = 0;

    @Resource
    private SyncExecutor syncExecutor;


    @Override
    public boolean register(String appId, int version) {

        try {
            Thread.sleep(mockResponseTimeDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (version == NOT_EXIST_VERSION) {
            version = EXIST_MIN_VERSION;
        }

        accept(entityClassSyncResponseGenerator(appId, version, mockSelfFatherAncestorsGenerate(System.currentTimeMillis())));
        return true;
    }

    @Override
    public boolean register(List<AbstractMap.SimpleEntry<String, Integer>> appIdEntries) {

        try {
            Thread.sleep(mockResponseTimeDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        appIdEntries.forEach(
                a -> {
                    accept(entityClassSyncResponseGenerator(a.getKey(), a.getValue(),
                            mockSelfFatherAncestorsGenerate(System.currentTimeMillis())));
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );

        return true;
    }

    @Override
    public boolean reRegister() {
        return false;
    }

    @Override
    public void accept(EntityClassSyncResponse entityClassSyncResponse) {
        syncExecutor.sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(),
                entityClassSyncResponse.getEntityClassSyncRspProto());
    }
}
