package com.xforceplus.ultraman.oqsengine.cdc.consumer.service;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_COLUMN_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EXPECTED_COMMIT_ID_COUNT;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.INIT_ID;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.CDCConstant;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.error.ErrorRecorder;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.factory.BinLogParserFactory;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.CommonUtils;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetricsRecorder;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCUnCommitMetrics;
import com.xforceplus.ultraman.oqsengine.storage.index.IndexStorage;
import io.micrometer.core.annotation.Timed;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 索引消费服务.
 *
 * @author xujia 2020/11/3
 * @since : 1.8
 */
public class DefaultConsumerService implements ConsumerService {

    final Logger logger = LoggerFactory.getLogger(DefaultConsumerService.class);

    @Resource
    private CDCMetricsHandler cdcMetricsHandler;

    @Resource(name = "indexStorage")
    private IndexStorage sphinxQLIndexStorage;

    @Resource
    private MetaManager metaManager;

    @Resource
    private ErrorRecorder errorRecorder;

    private long skipCommitId = INIT_ID;

    private boolean checkCommitReady = true;

    protected ParseResult parseResult = new ParseResult();

    public void setSkipCommitId(long skipCommitId) {
        this.skipCommitId = skipCommitId;
    }

    public void setCheckCommitReady(boolean checkCommitReady) {
        this.checkCommitReady = checkCommitReady;
    }

    public ParseResult printParseResult() {
        return parseResult;
    }

    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "cdc", "action", "cdc-consume"}
    )
    @Override
    public CDCMetrics consumeOneBatch(List<CanalEntry.Entry> entries, long batchId, CDCMetrics cdcMetrics)
        throws SQLException {
        try {
            //  初始化指标记录器
            CDCMetricsRecorder cdcMetricsRecorder =
                init(cdcMetrics.getCdcUnCommitMetrics(), batchId);

            //  同步逻辑.
            int syncs = parseCanalEntries(entries, cdcMetricsRecorder.getCdcMetrics());

            //  完成指标记录器.
            return cdcMetricsRecorder.finishRecord(syncs).getCdcMetrics();
        } finally {
            //  错误记录.
            if (parseResult.getErrors().size() > 0) {
                errorRecorder.record(batchId, parseResult.getErrors());
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

    /**
     * 初始化指标记录器, 将上一个批次unCommit数据写回当前指标.
     *
     * @param cdcUnCommitMetrics 上一个批次unCommit指标数据.
     * @param batchId            批次号.
     * @return 指标记录器.
     */
    private CDCMetricsRecorder init(CDCUnCommitMetrics cdcUnCommitMetrics, long batchId) {
        CDCMetricsRecorder cdcMetricsRecorder = new CDCMetricsRecorder();
        return cdcMetricsRecorder.startRecord(cdcUnCommitMetrics, batchId);
    }

    /**
     * 解析入口函数,对一个批次进行解析.
     *
     * @param entries    完整的批次信息.
     * @param cdcMetrics 指标数据.
     * @return 成功条数.
     */
    private int parseCanalEntries(List<CanalEntry.Entry> entries, CDCMetrics cdcMetrics) throws SQLException {
        //  初始化上下文
        ParserContext parserContext =
            new ParserContext(skipCommitId, checkCommitReady, cdcMetrics, metaManager);

        for (CanalEntry.Entry entry : entries) {

            //  不是TransactionEnd/RowData类型数据, 将被过滤
            switch (entry.getEntryType()) {
                case TRANSACTIONEND: {
                    transactionEnd(parserContext);
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

        //  等待isReady
        if (!parseResult.isReadyCommitIds().isEmpty()) {
            cdcMetricsHandler.isReady(new ArrayList<>(parseResult.isReadyCommitIds()));
        }

        //  批次数据整理完毕，开始执行index写操作。
        if (!parseResult.getFinishEntries().isEmpty()) {
            try {
                //  通过执行器执行Sphinx同步
                sphinxQLIndexStorage.saveOrDeleteOriginalEntities(parseResult.getFinishEntries().values());
            } catch (Exception e) {
                String message = String.format(
                    "write sphinx-batch error, commitIds : %s, startId : %s, message : %s",
                            parseResult.isReadyCommitIds(), parseResult.getStartId(), e.getMessage());

                //  加入错误列表.
                parseResult.addError(parseResult.getStartId(), parseResult.getFinishEntries().get(parseResult.getStartId()).getCommitid(),
                    CDCConstant.BATCH_WRITE_ERROR_POS,
                    CommonUtils.toErrorCommitIdStr(parseResult.isReadyCommitIds(), parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds()),
                    String.format("batch : %d consumer columns failed, %s", parserContext.getCdcMetrics().getBatchId(), message));

                throw new SQLException(message);
            }
        }

        batchLogged(cdcMetrics);

        return parseResult.getFinishEntries().size();
    }

    /**
     * 打印批次指标数据.
     *
     * @param cdcMetrics 指标数据.
     */
    private void batchLogged(CDCMetrics cdcMetrics) {
        if (cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds().size() > EMPTY_BATCH_SIZE) {
            if (logger.isDebugEnabled()) {
                logger.debug("[cdc-consumer] batch : {} end with un-commit ids : {}",
                    cdcMetrics.getBatchId(), JSON.toJSON(cdcMetrics.getCdcUnCommitMetrics().getUnCommitIds()));
            }
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


    private boolean hasUnCommitIds(ParserContext parserContext) {
        return parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size() > EXPECTED_COMMIT_ID_COUNT;
    }

    /**
     * TE时需要处理的逻辑.
     * 1.转移uncommitIds到ackList.
     * 2.清空uncommitIds.
     *
     * @param parserContext 上下文.
     */
    private void transactionEnd(ParserContext parserContext) {
        if (hasUnCommitIds(parserContext)) {
            if (logger.isWarnEnabled()) {
                logger.warn(
                    "[cdc-consumer] transaction end, batch : {}, one transaction has more than one commitId, ids : {}",
                    parserContext.getCdcMetrics().getBatchId(),
                    JSON.toJSON(parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds()));
            }
        }

        parserContext.getCdcMetrics().getCdcAckMetrics().getCommitList().addAll(
            parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds());

        if (logger.isDebugEnabled()) {
            logger.debug("[cdc-consumer] transaction end, batchId : {}, add new commitIds : {}",
                parserContext.getCdcMetrics().getBatchId(),
                JSON.toJSON(parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds()));
        }

        //  每个Transaction的结束需要将unCommitEntityValues清空
        parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().clear();
    }

    /**
     * 对rowData进行解析，rowData为对一张表的CUD操作，记录条数1～N.
     *
     * @param entry         canal对象同步实例.
     * @param parserContext 上下文.
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

                //  消费columns
                BinLogParserFactory.getInstance().dynamicParser().parse(columns, parserContext, parseResult);
            }
        }
    }

    /**
     * 只支持逻辑删除，实际上是进行了UPDATE操作.
     *
     * @param eventType 操作类型
     */
    private boolean supportEventType(CanalEntry.EventType eventType) {
        return eventType.equals(CanalEntry.EventType.INSERT)
            || eventType.equals(CanalEntry.EventType.UPDATE);
    }
}
