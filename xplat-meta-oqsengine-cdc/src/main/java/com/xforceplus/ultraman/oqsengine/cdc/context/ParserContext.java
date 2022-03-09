package com.xforceplus.ultraman.oqsengine.cdc.context;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class ParserContext {
    /**
     * 当前跳过的commit-id.
     */
    private long skipCommitId;
    /**
     * 是否进行commit-id的isReady-check
     */
    private boolean checkCommitReady;
    /**
     * metaManager适配器.
     */
    private MetaManager metaManager;
    /**
     * metaManager适配器.
     */
    private MasterStorage masterStorage;

    /**
     * 当前指标对象.
     */
    private CDCMetrics cdcMetrics;

    public ParserContext(long skipCommitId, boolean checkCommitReady, CDCMetrics cdcMetrics, MetaManager metaManager, MasterStorage masterStorage) {
        this.skipCommitId = skipCommitId;
        this.checkCommitReady = checkCommitReady;
        this.cdcMetrics = cdcMetrics;
        this.metaManager = metaManager;
        this.masterStorage = masterStorage;
    }

    public MasterStorage getMasterStorage() {
        return masterStorage;
    }

    public long getSkipCommitId() {
        return skipCommitId;
    }

    public boolean isCheckCommitReady() {
        return checkCommitReady;
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }

    public MetaManager getMetaManager() {
        return metaManager;
    }
}
