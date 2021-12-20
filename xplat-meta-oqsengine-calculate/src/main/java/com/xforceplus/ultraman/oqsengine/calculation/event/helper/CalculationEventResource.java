package com.xforceplus.ultraman.oqsengine.calculation.event.helper;

import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public class CalculationEventResource {

    private SegmentStorage segmentStorage;

    private MetaManager metaManager;

    public SegmentStorage getSegmentStorage() {
        return segmentStorage;
    }

    public MetaManager getMetaManager() {
        return metaManager;
    }

    public static class Builder {
        private SegmentStorage segmentStorage;

        private MetaManager metaManager;


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

        /**
         * 构造一个FeedBackContext 实例.
         *
         * @return 实例.
         */
        public CalculationEventResource build() {
            CalculationEventResource resource = new CalculationEventResource();
            resource.metaManager = this.metaManager;
            resource.segmentStorage = this.segmentStorage;
            return resource;
        }
    }

}
