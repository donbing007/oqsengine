package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_COLUMN_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EXPECTED_COMMIT_ID_COUNT;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.NO_TRANSACTION_COMMIT_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.COMMITID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ID;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetricsRecorder;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 索引消费服务.
 *
 * @author xujia 2020/11/3
 * @since : 1.8
 */
public class SphinxConsumerService implements ConsumerService {

    final Logger logger = LoggerFactory.getLogger(SphinxConsumerService.class);

    @Resource(name = "syncExecutor")
    private SyncExecutor sphinxSyncExecutor;

    private long skipCommitId = INIT_ID;

    private boolean checkCommitReady = true;

    public void setSkipCommitId(long skipCommitId) {
        this.skipCommitId = skipCommitId;
    }

    public void setCheckCommitReady(boolean checkCommitReady) {
        this.checkCommitReady = checkCommitReady;
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "cdc", "action", "cdc-consume"})
    @Override
    public CDCMetrics consume(List<CanalEntry.Entry> entries, long batchId, CDCMetricsService cdcMetricsService)
        throws SQLException {
        //  初始化指标记录器
        CDCMetricsRecorder cdcMetricsRecorder =
            init(cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics(), batchId);

        //  同步逻辑
        int syncs = syncAfterDataFilter(entries, cdcMetricsRecorder.getCdcMetrics(), cdcMetricsService);

        //  完成指标记录器
        return cdcMetricsRecorder.finishRecord(syncs).getCdcMetrics();
    }

    //  初始化指标记录器, 将上一个批次unCommit数据写回当前指标
    private CDCMetricsRecorder init(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        CDCMetricsRecorder cdcMetricsRecorder = new CDCMetricsRecorder();
        return cdcMetricsRecorder.startRecord(cdcUnCommitMetrics, batchId);
    }

    /*
        数据清洗、同步
    * */
    private int syncAfterDataFilter(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics,
                                    CDCMetricsService cdcMetricsService) throws SQLException {
        int syncCount = ZERO;
        //  需要同步的列表
        Map<Long, RawEntry> rawEntries = new LinkedHashMap<>();

        List<Long> commitIDs = new ArrayList<>();

        Timer.Sample sample = Timer.start(Metrics.globalRegistry);

        for (CanalEntry.Entry entry : entries) {

            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {
                case TRANSACTIONEND: {
                    cleanUnCommit(cdcMetrics);
                    break;
                }
                case ROWDATA: {
                    internalDataSync(entry, cdcMetrics, commitIDs, rawEntries);
                    break;
                }
                default: {
                    continue;
                }
            }
        }

        //  等待isReady
        if (!commitIDs.isEmpty()) {
            cdcMetricsService.isReadyCommit(commitIDs);
        }

        sample.stop(Timer.builder(MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS)
            .tags(
                "initiator", "cdc",
                "action", "init",
                "exception", "none"
            )
            .publishPercentileHistogram(false)
            .publishPercentiles(null)
            .register(Metrics.globalRegistry));

        //  批次数据整理完毕，开始执行index写操作。
        if (!rawEntries.isEmpty()) {
            //  通过执行器执行Sphinx同步
            syncCount += sphinxSyncExecutor.execute(rawEntries.values(), cdcMetrics);
        }

        batchLogged(cdcMetrics);
        return syncCount;
    }

    private void batchLogged(CDCMetrics cdcMetrics) {
        if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size() > EMPTY_BATCH_SIZE) {
            logger.info("[cdc-consumer] batch : {} end with un-commit ids : {}",
                cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size() > EXPECTED_COMMIT_ID_COUNT) {
                logger.warn("[cdc-consumer] batch : {}, one transaction has more than one commitId, ids : {}",
                    cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[cdc-consumer] batch end, batchId : {}, commitIds : {}, un-commitIds : {}",
                cdcMetrics.getBatchId(),
                JSON.toJSON(cdcMetrics.getCdcAckMetrics().getCommitList()),
                JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
        }
    }

    private void cleanUnCommit(CDCMetrics cdcMetrics) {
        if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size() > EXPECTED_COMMIT_ID_COUNT) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                    "[cdc-consumer] transaction end, batch : {}, one transaction has more than one commitId, ids : {}",
                    cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            }
        }

        cdcMetrics.getCdcAckMetrics().getCommitList().addAll(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds());

        if (logger.isDebugEnabled()) {
            logger.debug("[cdc-consumer] transaction end, batchId : {}, add new commitIds : {}",
                cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
        }

        //  每个Transaction的结束需要将unCommitEntityValues清空
        cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().clear();
    }

    private void internalDataSync(CanalEntry.Entry entry,
                                  CDCMetrics cdcMetrics,
                                  List<Long> commitIDs, Map<Long, RawEntry> rawEntries) throws SQLException {
        CanalEntry.RowChange rowChange = null;

        String uniKeyPrefixOffset = entry.getHeader().getLogfileName() + "-" + entry.getHeader().getLogfileOffset();

        try {
            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

        } catch (Exception e) {
            throw new SQLException(String.format("batch : %d, parse entry value failed, [%s], [%s]",
                cdcMetrics.getBatchId(), entry.getStoreValue(), e));
        }

        CanalEntry.EventType eventType = rowChange.getEventType();
        if (supportEventType(eventType)) {
            //  遍历RowData
            int pos = 1;
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                //  获取一条完整的Row，只关心变化后的数据
                List<CanalEntry.Column> columns = rowData.getAfterColumnsList();

                //  check need sync
                //  由于主库同步后会在最后commit时再更新一次commit_id，所以对于binlog同步来说，
                //  只需同步commit_id小于Long.MAX_VALUE的row
                if (null == columns || columns.size() == EMPTY_COLUMN_SIZE) {
                    throw new SQLException(
                        String.format("batch : %d, columns must not be null", cdcMetrics.getBatchId()));
                }

                long id;
                long commitId = UN_KNOW_ID;
                try {
                    //  获取CommitID
                    commitId = getLongFromColumn(columns, COMMITID);

                    //  获取ID
                    id = getLongFromColumn(columns, ID);
                } catch (Exception e) {
                    if (commitId != CommitHelper.getUncommitId()) {
                        if (commitId != UN_KNOW_ID) {
                            cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().add(commitId);
                        }
                        sphinxSyncExecutor.formatErrorHandle(columns, uniKeyPrefixOffset, pos, cdcMetrics.getBatchId(),
                            String.format("batch : %d, parse id, commitId from columns failed, message : %s",
                                cdcMetrics.getBatchId(), e.getMessage()));
                    }

                    continue;
                }

                //  是否MAX_VALUE
                if (commitId != CommitHelper.getUncommitId()) {
                    /*
                     *  检查是否为跳过不处理的commitId满足commitId > skipCommitId || (commitId == 0 && skipCommitId != 0)
                     * 否则跳过.
                     */
                    if (commitId > skipCommitId || (commitId == NO_TRANSACTION_COMMIT_ID
                        && skipCommitId != NO_TRANSACTION_COMMIT_ID)) {

                        if (checkCommitReady && !cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().contains(commitId)) {
                            commitIDs.add(commitId);
                        }

                        //  更新
                        cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().add(commitId);
                        rawEntries.put(id, new RawEntry(uniKeyPrefixOffset, pos, id, commitId,
                            entry.getHeader().getExecuteTime(), rowData.getAfterColumnsList()));
                    } else {
                        logger.warn(
                            "[cdc-consumer] batch : {}, ignore commitId less than skipCommitId, current id : {}, commitId : {}, skipCommitId : {}",
                            cdcMetrics.getBatchId(), id, commitId, skipCommitId);
                    }
                }

                pos++;
            }
        }
    }



    /*
        由于OQS主库的删除都是逻辑删除，实际上是进行了UPDATE操作
     */
    private boolean supportEventType(CanalEntry.EventType eventType) {
        return eventType.equals(CanalEntry.EventType.INSERT)
            || eventType.equals(CanalEntry.EventType.UPDATE);
    }
}
