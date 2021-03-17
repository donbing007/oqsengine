package com.xforceplus.ulraman.oqsengine.metadata.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.*;
import com.xforceplus.ultraman.oqsengine.meta.executor.IRequestWatchExecutor;
import com.xforceplus.ultraman.oqsengine.meta.handler.IRequestHandler;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;

import javax.annotation.Resource;
import java.util.*;

import static com.xforceplus.ulraman.oqsengine.metadata.utils.EntityClassStorageBuilder.*;
import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.NOT_EXIST_VERSION;

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
    public boolean register(WatchElement watchElement) {

        try {
            Thread.sleep(mockResponseTimeDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (watchElement.getVersion() == NOT_EXIST_VERSION) {
            watchElement.setVersion(EXIST_MIN_VERSION);
        }

        onNext(entityClassSyncResponseGenerator(watchElement.getAppId(), watchElement.getVersion(),
                                                        mockSelfFatherAncestorsGenerate(System.currentTimeMillis())), null);
        return true;
    }


    @Override
    public boolean register(List<WatchElement> appIdEntries) {

        try {
            Thread.sleep(mockResponseTimeDuration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        appIdEntries.forEach(
                a -> {
                    onNext(entityClassSyncResponseGenerator(a.getAppId(), a.getVersion(),
                            mockSelfFatherAncestorsGenerate(System.currentTimeMillis())), null);
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
    public IRequestWatchExecutor watchExecutor() {
        return null;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void onNext(EntityClassSyncResponse entityClassSyncResponse, Void aVoid) {
        syncExecutor.sync(entityClassSyncResponse.getAppId(), entityClassSyncResponse.getVersion(),
                entityClassSyncResponse.getEntityClassSyncRspProto());
    }

    @Override
    public boolean isShutDown() {
        return false;
    }
}
