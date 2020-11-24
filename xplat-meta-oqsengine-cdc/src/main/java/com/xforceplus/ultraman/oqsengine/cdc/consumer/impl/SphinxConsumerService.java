package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetricsRecorder;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.devops.cdc.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.storage.transaction.commit.CommitHelper;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;
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

    @Resource(name = "sphinxSyncExecutor")
    private SphinxSyncExecutor sphinxSyncExecutor;

    @Override
    public CDCMetrics consume(List<CanalEntry.Entry> entries, long batchId, CDCUnCommitMetrics cdcUnCommitMetrics) throws SQLException {
        //  初始化指标记录器
        CDCMetricsRecorder cdcMetricsRecorder = init(cdcUnCommitMetrics, batchId);

        //  同步逻辑
        int syncs = syncAfterDataFilter(entries, cdcMetricsRecorder.getCdcMetrics());

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
    private int syncAfterDataFilter(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        int syncCount = ZERO;
        //  需要同步的列表
        List<RawEntry> rawEntries = new ArrayList<>();
        for (CanalEntry.Entry entry : entries) {
            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {
                case TRANSACTIONEND:
                    //  同步rawEntries到Sphinx
                    if (rawEntries.size() > 0) {
                        //  通过执行器执行Sphinx同步
                        syncCount += sphinxSyncExecutor.sync(rawEntries, cdcMetrics);

                        //  每个Transaction的结束需要将rawEntries清空
                        rawEntries.clear();
                    }
                    //  到达TransactionEnd时的清理与同步
                    cleanUnCommit(cdcMetrics);
                    break;
                case ROWDATA:
                    rawEntries.addAll(internalDataSync(entry, cdcMetrics));
                    break;
            }
        }

        //  最后一个unCommitId的数据也需要同步一次
        if (!rawEntries.isEmpty()) {
            //  通过执行器执行Sphinx同步
            syncCount += sphinxSyncExecutor.sync(rawEntries, cdcMetrics);
        }

        return syncCount;
    }

    private void cleanUnCommit(CDCMetrics cdcMetrics) {
        //  每次Transaction结束,将unCommitId同步到commitList中
        if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitId() > INIT_ID) {
            cdcMetrics.getCdcAckMetrics().getCommitList().add(cdcMetrics.getCdcUnCommitMetrics().getUnCommitId());
            cdcMetrics.getCdcUnCommitMetrics().setUnCommitId(INIT_ID);
        }

        //  每个Transaction的结束需要将unCommitEntityValues清空
        cdcMetrics.getCdcUnCommitMetrics().setUnCommitEntityValues(new ConcurrentHashMap<>());
    }


    private List<RawEntry> internalDataSync(CanalEntry.Entry entry, CDCMetrics cdcMetrics) throws SQLException {
        List<RawEntry> rawEntries = new ArrayList<>();
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
                    sphinxSyncExecutor.errorHandle(id, commitId, String.format("parse id, commit from columns failed, message : %s", e.getMessage()));
                    continue;
                }

                //  是否MAX_VALUE
                if (commitId != CommitHelper.getUncommitId()) {
                    //  更新
                    cdcMetrics.getCdcUnCommitMetrics().setUnCommitId(commitId);
                    rawEntries.add(new RawEntry(id, commitId,
                            entry.getHeader().getExecuteTime(), eventType, rowData.getAfterColumnsList()));
                } else {
                    //  优化父子类
                    addPrefEntityValue(columns, id, cdcMetrics);
                }
            }
        }

        return rawEntries;
    }


    /*
        当存在子类时,将父类信息缓存在蓄水池中，等待子类进行合并
        蓄水池在每一次事务结束时进行判断，必须为空(代表一个事务中的父子类已全部同步完毕)
        父类会扔自己的EntityValue进去,子类会取出自己父类的EntityValue进行合并
    */
    private void addPrefEntityValue(List<CanalEntry.Column> columns, Long id, CDCMetrics cdcMetrics) throws SQLException {
        try {
            //  有子类, 将父类的EntityValue存入的relationMap中
            if (getLongFromColumn(columns, CREF) > ZERO) {
                cdcMetrics.getCdcUnCommitMetrics().getUnCommitEntityValues()
                        .put(id,
                                new RawEntityValue(getStringFromColumn(columns, ATTRIBUTE), getStringFromColumn(columns, META)));
            }
        } catch (Exception e) {
            logger.warn("convert pref entityValue failed, pref: {}, ignore.", id);
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
