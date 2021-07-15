package com.xforceplus.ultraman.oqsengine.metadata.integeration.recover.server.mock;

import com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.dto.ServerSyncEvent;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.EntityClassGenerator;


/**
 * desc :.
 * name : EntityClassGenerator
 *
 * @author : xujia 2021/3/1
 * @since : 1.8
 */
public class MockEntityClassGenerator implements EntityClassGenerator {

    private int version;
    private long entityId;

    public void reset(int version, long entityId) {
        this.version = version;
        this.entityId = entityId;
    }

    @Override
    public ServerSyncEvent pull(String appId, String env) {
        return new MockSyncEvent(appId, env, version,
            MockEntityClassSyncRspProtoBuilder.entityClassSyncRspProtoGenerator(entityId));
    }

    public class MockSyncEvent implements ServerSyncEvent {

        private final String appId;
        private final String env;
        private final int version;

        /**
         * standard.
         */
        private final EntityClassSyncRspProto entityClassSyncRspProto;

        /**
         * extend.
         */
        //  todo not implements now


        public MockSyncEvent(String appId, String env, int version, EntityClassSyncRspProto entityClassSyncRspProto) {
            this.appId = appId;
            this.env = env;
            this.version = version;
            this.entityClassSyncRspProto = entityClassSyncRspProto;
        }

        @Override
        public EntityClassSyncRspProto entityClassSyncRspProto() {
            return entityClassSyncRspProto;
        }

        @Override
        public String appId() {
            return appId;
        }

        @Override
        public String env() {
            return env;
        }

        @Override
        public int version() {
            return version;
        }
    }
}
