package com.xforceplus.ultraman.oqsengine.cdc.consumer.service.mock;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getLongFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_COLUMN_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.COMMITID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ID;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.error.ErrorRecorder;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetricsRecorder;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import io.micrometer.core.annotation.Timed;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ErrorConsumerService implements ConsumerService {

    private ErrorRecorder errorRecorder;
    private CDCMetricsHandler cdcMetricsHandler;

    private Map<String, ParseResult.Error> errors = new HashMap<>();

    protected ParseResult parseResult = new ParseResult();

    private int testCount = 0;
    private int maxRetry = 3;

    private volatile boolean isFinishTest = false;

    public ErrorRecorder errorRecorder() {
        return errorRecorder;
    }

    @Timed(value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS, extraTags = {"initiator", "cdc", "action", "cdc-consume"})
    @Override
    public CDCMetrics consumeOneBatch(List<CanalEntry.Entry> entries, long batchId, CDCMetrics cdcMetrics)
        throws SQLException {
        try {
            //  初始化指标记录器
            CDCMetricsRecorder cdcMetricsRecorder =
                init(cdcMetrics.getCdcUnCommitMetrics(), batchId);

            //  同步逻辑.
            int syncs = parseCanalEntries(entries, cdcMetricsRecorder.getCdcMetrics());

            if (testCount < maxRetry) {
                testCount++;
                throw new SQLException("mock error consumer exception.");
            }

            //  完成指标记录器.
            return cdcMetricsRecorder.finishRecord(syncs).getCdcMetrics();
        } finally {
            //  错误记录.
            if (parseResult.getErrors().size() > 0) {
                errorRecorder.record(batchId, parseResult.getErrors());

                errors.putAll(parseResult.getErrors());
            }

            if (testCount == maxRetry) {
                isFinishTest = true;
            }

            //  结果集对象不删除只清空
            //  跨批次的情况(静态的最后一条业务数据没有到达时)operationEntries保留最后一条数据到下次消费.
            parseResult.clean();
        }
    }

    @Override
    public CDCMetricsHandler metricsHandler() {
        return cdcMetricsHandler;
    }

    public boolean isFinishTest() {
        return isFinishTest;
    }

    /**
     * 初始化指标记录器, 将上一个批次unCommit数据写回当前指标.
     *
     * @param cdcUnCommitMetrics 上一个批次unCommit指标数据.
     * @param batchId 批次号.
     * @return 指标记录器.
     */
    private CDCMetricsRecorder init(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        CDCMetricsRecorder cdcMetricsRecorder = new CDCMetricsRecorder();
        return cdcMetricsRecorder.startRecord(cdcUnCommitMetrics, batchId);
    }

    private int parseCanalEntries(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        //  初始化上下文
        ParserContext parserContext =
            new ParserContext(-1, false, cdcMetrics, null);

        for (CanalEntry.Entry entry : entries) {

            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {
                case TRANSACTIONEND: {
                    break;
                }
                case ROWDATA: {
                    rowDataParse(entry, parserContext);
                    break;
                }
                default: {
                }
            }
        }

        return 0;
    }

    /**
     * 对rowData进行解析，rowData为对一张表的CUD操作，记录条数1～N.
     *
     * @param entry canal对象同步实例.
     * @param parserContext 上下文.
     * @throws SQLException
     */
    private void rowDataParse(CanalEntry.Entry entry, ParserContext parserContext) throws SQLException {

        CanalEntry.RowChange rowChange = null;
        try {
            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (Exception e) {
            throw new SQLException(String.format("batch : %d, parse entry value failed, [%s], [%s]",
                parserContext.getCdcMetrics().getBatchId(), entry.getStoreValue(), e));
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
                if (columns.size() == EMPTY_COLUMN_SIZE) {
                    throw new SQLException(
                        String.format("batch : %d, columns must not be null",
                            parserContext.getCdcMetrics().getBatchId()));
                }

                //  获取CommitID
                long commitId = getLongFromColumn(columns, COMMITID);

                //  获取ID
                long id = getLongFromColumn(columns, ID);

                //  加入错误列表.
                parseResult.addError(id, commitId,
                    String.format("batch : %d, pos : %d, parse columns failed, message : %s",
                        parserContext.getCdcMetrics().getBatchId(), parseResult.getPos(), "mock test error"));

                parseResult.finishOne();
            }
        }
    }

    /**
     * 只支持逻辑删除，实际上是进行了UPDATE操作.
     *
     * @param eventType 操作类型
     * @return
     */
    private boolean supportEventType(CanalEntry.EventType eventType) {
        return eventType.equals(CanalEntry.EventType.INSERT)
            || eventType.equals(CanalEntry.EventType.UPDATE);
    }

    public void init(ErrorRecorder errorRecorder, CDCMetricsHandler cdcMetricsHandler) {
        this.errorRecorder = errorRecorder;
        this.cdcMetricsHandler = cdcMetricsHandler;
    }

    public Map<String, ParseResult.Error> getErrors() {
        return errors;
    }
}
