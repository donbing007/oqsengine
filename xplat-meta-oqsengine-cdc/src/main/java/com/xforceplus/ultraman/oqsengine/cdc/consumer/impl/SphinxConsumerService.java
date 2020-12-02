package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsService;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetricsRecorder;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.*;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.*;

/**
 * desc :
 * name : SphinxConsumerService
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class SphinxConsumerService implements ConsumerService {

    final Logger logger = LoggerFactory.getLogger(SphinxConsumerService.class);

    @Resource(name = "syncExecutor")
    private SyncExecutor sphinxSyncExecutor;



    @Override
    public CDCMetrics consume(List<CanalEntry.Entry> entries, long batchId, CDCMetricsService cdcMetricsService) throws SQLException {
        //  初始化指标记录器
        CDCMetricsRecorder cdcMetricsRecorder = init(cdcMetricsService.getCdcMetrics().getCdcUnCommitMetrics(), batchId);

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
    private int syncAfterDataFilter(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics, CDCMetricsService cdcMetricsService) throws SQLException {
        int syncCount = ZERO;
        //  需要同步的列表
        Map<Long, RawEntry> rawEntries = new HashMap<>();
        for (CanalEntry.Entry entry : entries) {
            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {
                case TRANSACTIONEND:
                    cleanUnCommit(cdcMetrics);
                    break;
                case ROWDATA:
                    internalDataSync(entry, cdcMetrics, cdcMetricsService, rawEntries);
                    break;
            }
        }

        //  最后一个unCommitId的数据也需要同步一次
        if (!rawEntries.isEmpty()) {
            //  通过执行器执行Sphinx同步
            syncCount += sphinxSyncExecutor.execute(rawEntries.values(), cdcMetrics);
        }

        logCommitId(cdcMetrics);
        return syncCount;
    }

    private void logCommitId(CDCMetrics cdcMetrics) {
        if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size() > EMPTY_BATCH_SIZE) {
            logger.debug("un-commit ids : {}", JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size() > UNEXPECTED_COMMIT_ID_COUNT) {
                logger.warn("one transaction has more than one commitId, ids : {}",
                        JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            }
        }
    }

    private void cleanUnCommit(CDCMetrics cdcMetrics) {
        cdcMetrics.getCdcAckMetrics().getCommitList().addAll(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds());
        //  每次Transaction结束,将unCommitId同步到commitList中
        logCommitId(cdcMetrics);

        //  每个Transaction的结束需要将unCommitEntityValues清空
        cdcMetrics.getCdcUnCommitMetrics().setUnCommitIds(new LinkedHashSet<>());
    }


    private void internalDataSync(CanalEntry.Entry entry, CDCMetrics cdcMetrics, CDCMetricsService cdcMetricsService,
                                  Map<Long, RawEntry> rawEntries) throws SQLException {
        CanalEntry.RowChange rowChange = null;
        try {
            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

        } catch (Exception e) {
            throw new SQLException(String.format("parse entry value failed, [%s], [%s]", entry.getStoreValue(), e));
        }

        CanalEntry.EventType eventType = rowChange.getEventType();
        if (supportEventType(eventType)) {
            //  遍历RowData
            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                //  获取一条完整的Row，只关心变化后的数据
                List<CanalEntry.Column> columns = rowData.getAfterColumnsList();

                //  check need sync
                //  由于主库同步后会在最后commit时再更新一次commit_id，所以对于binlog同步来说，
                //  只需同步commit_id小于Long.MAX_VALUE的row
                if (null == columns || columns.size() == EMPTY_COLUMN_SIZE) {
                    throw new SQLException("columns must not be null");
                }

                Long id = UN_KNOW_ID;
                Long commitId = UN_KNOW_ID;
                try {
                    //  获取ID
                    id = getLongFromColumn(columns, ID);
                    //  获取CommitID
                    commitId = getLongFromColumn(columns, COMMITID);
                } catch (Exception e) {
                    sphinxSyncExecutor.errorRecord(id, commitId, String.format("parse id, commit from columns failed, message : %s", e.getMessage()));
                    continue;
                }

                //  是否MAX_VALUE
                if (commitId != CommitHelper.getUncommitId()) {
//                    if (!cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().contains(commitId)) {
//                        //  阻塞直到成功
//                        cdcMetricsService.isReadyCommit(commitId);
//                    }
                    //  更新
                    cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().add(commitId);
                    rawEntries.put(id, new RawEntry(id, commitId,
                            entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList()));
                }
            }
        }
    }

    /*
        由于OQS主库的删除都是逻辑删除，实际上是进行了UPDATE操作
     */
    private boolean supportEventType(CanalEntry.EventType eventType) {
        return eventType.equals(CanalEntry.EventType.INSERT) ||
            eventType.equals(CanalEntry.EventType.UPDATE);
    }
}
