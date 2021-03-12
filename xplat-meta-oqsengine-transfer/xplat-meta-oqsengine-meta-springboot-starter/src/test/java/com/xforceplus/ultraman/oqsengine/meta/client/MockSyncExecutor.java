package com.xforceplus.ultraman.oqsengine.meta.client;

import com.xforceplus.ultraman.oqsengine.meta.common.constant.RequestStatus;
import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.pojo.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import org.junit.Assert;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.meta.common.utils.EntityClassStorageBuilderUtils.protoToStorageList;

/**
 * desc :
 * name : MockSyncExecutor
 *
 * @author : xujia
 * date : 2021/3/3
 * @since : 1.8
 */
@Component
public class MockSyncExecutor implements SyncExecutor {
    public volatile RequestStatus status = RequestStatus.SYNC_OK;

    public Map<String, RequestStatusVersion> requestStatusHashMap = new HashMap<>();

    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {

        Assert.assertNotNull(entityClassSyncRspProto);

        try {
            if (status.equals(RequestStatus.DATA_ERROR)) {
                throw new MetaSyncClientException("data error.", false);
            } else if (status.equals(RequestStatus.SYNC_OK)) {
                List<EntityClassStorage> entityClassStorageList = protoToStorageList(entityClassSyncRspProto);
                requestStatusHashMap.put(appId, new RequestStatusVersion(status, version));
            }
            return status.equals(RequestStatus.SYNC_OK);
        } catch (Exception e) {
          e.printStackTrace();
          throw e;
        } finally {
            status = RequestStatus.SYNC_OK;
        }
    }

    @Override
    public int version(String appId) {
        return 0;
    }



    public static class RequestStatusVersion {
        private RequestStatus requestStatus;
        private int version;

        public RequestStatusVersion(RequestStatus requestStatus, int version) {
            this.requestStatus = requestStatus;
            this.version = version;
        }

        public RequestStatus getRequestStatus() {
            return requestStatus;
        }

        public void setRequestStatus(RequestStatus requestStatus) {
            this.requestStatus = requestStatus;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }
    }
}
