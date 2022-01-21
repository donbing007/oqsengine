package com.xforceplus.ultraman.oqsengine.calculation.event.helper;

import com.xforceplus.ultraman.oqsengine.common.serializable.SerializeStrategy;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.storage.KeyValueStorage;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculationEventResource {

    private SegmentStorage segmentStorage;

    private MetaManager metaManager;

    private KeyValueStorage keyValueStorage;

    private SerializeStrategy serializeStrategy;

    public SegmentStorage getSegmentStorage() {
        return segmentStorage;
    }

    public MetaManager getMetaManager() {
        return metaManager;
    }

    public KeyValueStorage getKeyValueStorage() {
        return keyValueStorage;
    }

    public SerializeStrategy getSerializeStrategy() {
        return serializeStrategy;
    }

    /**
     * builder.
     */
    public static class Builder {
        private SegmentStorage segmentStorage;

        private MetaManager metaManager;

        private KeyValueStorage keyValueStorage;

        private SerializeStrategy serializeStrategy;

        private Builder() {
        }

        public static CalculationEventResource.Builder anCalculationEventContext() {
            return new CalculationEventResource.Builder();
        }


        public CalculationEventResource.Builder withSegmentStorage(SegmentStorage segmentStorage) {
            this.segmentStorage = segmentStorage;
            return this;
        }

        public CalculationEventResource.Builder withMeta(MetaManager metaManager) {
            this.metaManager = metaManager;
            return this;
        }

        public CalculationEventResource.Builder withKeyValueStorage(KeyValueStorage keyValueStorage) {
            this.keyValueStorage = keyValueStorage;
            return this;
        }

        public CalculationEventResource.Builder withSerializeStrategy(SerializeStrategy serializeStrategy) {
            this.serializeStrategy = serializeStrategy;
            return this;
        }

        /**
         * 构造一个FeedBackContext 实例.
         *
         * @return 实例.
         */
        public CalculationEventResource build() {
            CalculationEventResource resource = new CalculationEventResource();
            resource.metaManager = this.metaManager;
            resource.segmentStorage = this.segmentStorage;
            resource.keyValueStorage = this.keyValueStorage;
            return resource;
        }
    }

}
