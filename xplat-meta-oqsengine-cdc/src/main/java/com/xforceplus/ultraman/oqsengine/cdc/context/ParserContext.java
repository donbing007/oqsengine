package com.xforceplus.ultraman.oqsengine.cdc.context;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.MasterStorage;
import io.vavr.Tuple2;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 是否进行commit-id的isReady-check.
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

    /**
     * 中间结果集.
     */
    private Map<Long, Tuple2<Long, List<CanalEntry.Column>>> parseMiddleResult;

    /**
     * entityClassMap.
     */
    private Map<String, IEntityClass> entityClasses;

    /**
     * 当前check-error-pos.
     */
    private int currentCheckPos;

    /**
     * 构造新的实例.
     *
     * @param skipCommitId     需要跳过的提交号.
     * @param checkCommitReady 是否检查提交号状态.
     * @param cdcMetrics       CDC指标器.
     * @param metaManager      元数据管理.
     */
    public ParserContext(long skipCommitId, boolean checkCommitReady, CDCMetrics cdcMetrics, MetaManager metaManager) {
        this.skipCommitId = skipCommitId;
        this.checkCommitReady = checkCommitReady;
        this.cdcMetrics = cdcMetrics;
        this.metaManager = metaManager;
        this.parseMiddleResult = new HashMap<>();
        this.entityClasses = new HashMap<>();
        this.currentCheckPos = 0;
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

    public Map<Long, Tuple2<Long, List<CanalEntry.Column>>> getParseMiddleResult() {
        return parseMiddleResult;
    }

    public int currentCheckPos() {
        return currentCheckPos;
    }

    public void incrementCurrentCheckPos() {
        this.currentCheckPos++;
    }

    public Map<String, IEntityClass> entityClasses() {
        return entityClasses;
    }

    /**
     * 获取entityClassKey.
     * @param entityClassRef 对象参考.
     * @return key.
     */
    public static String entityClassKey(EntityClassRef entityClassRef) {
        return entityClassRef.getId() + "." + entityClassRef.getProfile();
    }
}
