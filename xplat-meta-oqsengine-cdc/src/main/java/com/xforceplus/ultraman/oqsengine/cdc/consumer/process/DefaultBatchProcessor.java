package com.xforceplus.ultraman.oqsengine.cdc.consumer.process;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.FREE_MESSAGE_WAIT_IN_MS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.IS_BACK_UP_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MESSAGE_GET_WARM_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class DefaultBatchProcessor implements BatchProcessor {

    final Logger logger = LoggerFactory.getLogger(DefaultBatchProcessor.class);

    @Resource
    private ConsumerService consumerService;

    @Resource
    private RebuildIndexExecutor rebuildIndexExecutor;

    @Override
    public void executeOneBatch(AbstractCDCConnector connector, RunnerContext context) throws SQLException {
        Message message = null;
        long batchId;
        try {
            long start = System.currentTimeMillis();

            //获取指定数量的数据
            message = connector.getMessageWithoutAck();
            long duration = System.currentTimeMillis() - start;

            batchId = message.getId();

            if (duration > MESSAGE_GET_WARM_INTERVAL && batchId != EMPTY_BATCH_ID) {
                logger.info(
                    "[batchProcess] read message from canal server use too much times, use timeMs : {}, batchId : {}",
                    duration, message.getId());
            }
        } catch (Exception e) {
            //  未获取到数据,回滚
            connector.rollback();
            String error = String.format("read message from canal server error, %s", e.getMessage());
            logger.error("[batchProcess] {}", error);
            throw new SQLException(error);
        }

        //  当synced标志位设置为True时，表示后续的操作必须通过最终一致性操作保持成功
        boolean synced = false;
        try {
            CDCMetrics cdcMetrics = null;
            if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                //  消费binlog
                cdcMetrics =
                    consumerService.consumeOneBatch(message.getEntries(), batchId, context.getCdcMetrics());

                //  binlog处理，同步指标到cdcMetrics中
                synced = saveMetrics(cdcMetrics);

                //  canal状态确认、指标同步
                finishBatch(cdcMetrics, connector, context);

                context.setCdcMetrics(cdcMetrics);

                //  同步维护指标.
                if (!context.getCdcMetrics().getDevOpsMetrics().isEmpty()) {
                    rebuildIndexExecutor.sync(context.getCdcMetrics().getDevOpsMetrics());
                }
            } else {
                //  当前没有任务需要消费
                cdcMetrics = new CDCMetrics(batchId, context.getCdcMetrics().getCdcAckMetrics(),
                    context.getCdcMetrics().getCdcUnCommitMetrics());

                cdcMetrics.getCdcAckMetrics().setExecuteRows(ZERO);

                synced = saveMetrics(cdcMetrics);

                emptyBatch(batchId, connector);
            }
        } catch (Exception e) {
            String error = "";
            if (synced) {
                error = "ack finish status error";
            } else {
                //  当未执行到最终必须成功时,需进行rollback
                connector.rollback();
                error = "consume message error";
            }

            logger.error("[batchProcess] consume batch error, connection will reset..., message : {}, {}", error,
                e.getMessage());
            throw new SQLException(error);
        }
    }

    @Override
    public void recover(AbstractCDCConnector connector, RunnerContext runnerContext) throws SQLException {
        //  如果是服务重启，则需要对齐canal ack信息及redis中的ackMetrics指标
        CDCMetrics cdcMetrics = consumerService.metricsHandler().query();
        long originBatchId = EMPTY_BATCH_ID;
        if (null != cdcMetrics) {
            //  当前的BatchId != -1时，表示需要进行Canal batchId ACK操作
            originBatchId = cdcMetrics.getBatchId();
            if (originBatchId != IS_BACK_UP_ID && originBatchId != EMPTY_BATCH_ID) {
                // ack确认， 回写uncommit信息
                backup(originBatchId, connector, cdcMetrics);
            }

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, runnerContext, true);

            logger.info("[cdc-runner] recover from last ackMetrics position success..., originBatchId : {}",
                originBatchId);
        } else {
            //  回调告知当前为一个新的开始
            consumerService.metricsHandler().renewConnect(runnerContext.getCdcMetrics());
            logger.info("[cdc-runner] renew connect success, originBatchId : {}", originBatchId);
        }
    }

    @Override
    public void init() {
        consumerService.metricsHandler().init();
    }

    @Override
    public void shutdown() {
        consumerService.metricsHandler().shutdown();
    }

    @Override
    public void error(RunnerContext context) {
        consumerService.metricsHandler().callBackError(context.getCdcMetrics());
    }

    /**
     * 保存未提交的指标数据.
     *
     * @param cdcMetrics 指标数据.
     * @return true/false.
     */
    private boolean saveMetrics(CDCMetrics cdcMetrics) {
        consumerService.metricsHandler().backup(cdcMetrics);

        return true;
    }

    /**
     * 完成时的确认.
     *
     * @param cdcMetrics 指标数据.
     * @param connector  连接器.
     * @param context    上下文.
     */
    private void finishBatch(CDCMetrics cdcMetrics, AbstractCDCConnector connector, RunnerContext context)
        throws SQLException {
        if (null != cdcMetrics) {
            long originBatchId = cdcMetrics.getBatchId();

            // ack确认， 回写unCommit信息
            backup(originBatchId, connector, cdcMetrics);

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, context, false);
        }
    }

    /**
     * 同步一个空的batchId.
     *
     * @param batchId   批次id.
     * @param connector 连接器.
     */
    private void emptyBatch(long batchId, AbstractCDCConnector connector) throws SQLException {
        //  同步状态
        connector.ack(batchId);

        //  没有新的同步信息，睡眠5ms进入下次轮训
        TimeWaitUtils.wakeupAfter(FREE_MESSAGE_WAIT_IN_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * 备份.
     * 由于采用2阶段prepare -> confirm模式，当进入backAfterAck的逻辑时,必须保证一致性（成功）.
     * 所以需要在ack成功后标记batchId为-Long.MAX_VALUE，并覆盖unCommitMetrics.
     * 启动时重复该步骤.
     *
     * @param originBatchId 批次ID.
     * @param connector     连接器.
     * @param cdcMetrics    指标.
     */
    private void backup(long originBatchId, AbstractCDCConnector connector, CDCMetrics cdcMetrics) throws SQLException {
        //  1.确认ack batchId
        connector.ack(originBatchId);

        logger.debug("ack batch success, batchId : {}", originBatchId);
        //  2.设置当前batchId为-LONG.MAX_VALUE
        cdcMetrics.setBatchId(IS_BACK_UP_ID);
        //  3.重置redis unCommit数据
        consumerService.metricsHandler().backup(cdcMetrics);
        logger.debug("rest cdcMetrics with buckUpId success, origin batchId : {}", originBatchId);
    }

    /**
     * 回调(成功).
     *
     * @param originBatchId 批次ID.
     * @param temp          临时的指标数据.
     * @param context       上下文.
     * @param isConnectSync 是否成功消费标志.
     */
    private void callBackSuccess(long originBatchId, CDCMetrics temp, RunnerContext context, boolean isConnectSync) {
        context.getCdcMetrics().setCdcUnCommitMetrics(temp.getCdcUnCommitMetrics());
        context.getCdcMetrics().consumeSuccess(originBatchId, temp, isConnectSync);

        consumerService.metricsHandler().callBackSuccess(context.getCdcMetrics());
    }
}
