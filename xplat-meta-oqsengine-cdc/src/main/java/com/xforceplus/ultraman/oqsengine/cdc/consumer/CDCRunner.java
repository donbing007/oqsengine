package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.EMPTY_BATCH_SIZE;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.FREE_MESSAGE_WAIT_IN_MS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.IS_BACK_UP_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_STOP_WAIT_LOOPS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_STOP_WAIT_TIME;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MESSAGE_GET_WARM_INTERVAL;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.RECONNECT_WAIT_IN_SECONDS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.ZERO;

import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.service.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.cdc.metrics.CDCMetricsHandler;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.RebuildIndexExecutor;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc : 处理client端通讯,及外层逻辑.
 *
 * name : CdcConnectorRunner.
 *
 * @author : xujia 2020/11/3
 * @since : 1.8
 */
public class CDCRunner extends Thread {

    final Logger logger = LoggerFactory.getLogger(CDCRunner.class);

    private ConsumerService consumerService;

    private CDCMetricsHandler metricsHandler;

    private AbstractCDCConnector connector;

    private RebuildIndexExecutor rebuildIndexExecutor;

    private RunnerContext context;

    /**
     * 实例化.
     */
    public CDCRunner(ConsumerService consumerService,
                          CDCMetricsHandler metricsHandler,
                          AbstractCDCConnector connector,
                          RebuildIndexExecutor rebuildIndexExecutor) {

        this.consumerService = consumerService;
        this.metricsHandler = metricsHandler;
        this.connector = connector;
        this.rebuildIndexExecutor = rebuildIndexExecutor;

        context = new RunnerContext();
    }

    /**
     * 优雅停止.
     */
    public void shutdown() {
        //  设置运行状态为尝试停止
        context.setRunningStatus(RunningStatus.TRY_STOP);
        metricsHandler.shutdown();

        int useTime = 0;
        while (useTime < MAX_STOP_WAIT_LOOPS) {
            TimeWaitUtils.wakeupAfter(MAX_STOP_WAIT_TIME, TimeUnit.SECONDS);

            //  判断当前状态是否已停止
            if (context.getRunningStatus().isStop()) {
                logger.info("[cdc-runner] consumer success stop.");
                break;
            }

            useTime++;
        }

        if (useTime >= MAX_STOP_WAIT_LOOPS) {
            logger.warn("[cdc-runner] force stop after {} seconds.", useTime * MAX_STOP_WAIT_TIME);
        }
    }

    /**
     * 开始运行.
     */
    public void run() {
        //  初始化指标处理器
        metricsHandler.init();

        while (true) {
            //  判断当前服务状态是否终止，如果是则关闭.
            if (needTerminate()) {
                break;
            }

            try {
                //  连接
                connector.open();

                //  首先将上次记录完整的信息(batchID)确认到canalServer中
                metricsRecover();
            } catch (Exception e) {

                //  当前连续失败数自增
                context.incrementContinuesConnectFails();

                //  达到最大重试次数
                //  这个步骤是当出现了maxRetry次重连都失败的情况，会放弃当前的链接，重新创建一个新的链接。
                //  该做法是为了解决当canal-server重启后当前链接失效的情况下还在继续链接.
                if (connector.isMaxRetry(context.getContinuesConnectFails())) {
                    //  再次确认当前链接已释放
                    connector.close();

                    //  重新创建canalConnector
                    connector.init();

                    context.resetContinuesConnectFails();
                } else {
                    closeAndCallBackError(CDCStatus.DIS_CONNECTED,
                        String.format("[cdc-runner] canal-server connection error, %s", e.getMessage()));

                }
                return;
            }

            //  连接成功，重置标志位
            context.resetContinuesConnectFails();

            try {
                //  消费
                consume();
            } catch (Exception e) {
                closeAndCallBackError(CDCStatus.CONSUME_FAILED,
                    String.format("[cdc-runner] canal-client consume error, %s", e.getMessage()));
            }
        }
    }

    private void closeAndCallBackError(CDCStatus cdcStatus, String errorMessage) {
        context.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(cdcStatus);

        connector.close();
        logger.error(errorMessage);

        //  这里将进行睡眠->同步错误信息->进入下次循环
        TimeWaitUtils.wakeupAfter(RECONNECT_WAIT_IN_SECONDS, TimeUnit.SECONDS);

        metricsHandler.callBackError(context.getCdcMetrics());
    }

    private boolean needTerminate() {
        if (context.getRunningStatus().shouldStop()) {
            connector.shutdown();
            context.setRunningStatus(RunningStatus.STOP_SUCCESS);
            return true;
        }
        return false;
    }

    /**
     * 消费.
     */
    public void consume() throws SQLException {
        while (true) {
            //  服务被终止
            if (context.getRunningStatus().shouldStop()) {
                context.setRunningStatus(RunningStatus.STOP_SUCCESS);
                break;
            }

            executeBatch();
        }
    }

    private void executeBatch() throws SQLException {
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
                    "[cdc-runner] get message from canal server use too much times, use timeMs : {}, batchId : {}",
                    duration, message.getId());
            }
        } catch (Exception e) {
            //  未获取到数据,回滚
            connector.rollback();
            String error = String.format("get message from canal server error, %s", e.getMessage());
            logger.error("[cdc-runner] read message exception, {}", error);
            throw new SQLException(error);
        }

        //  当synced标志位设置为True时，表示后续的操作必须通过最终一致性操作保持成功
        boolean synced = false;
        try {
            CDCMetrics cdcMetrics = null;
            if (batchId != EMPTY_BATCH_ID || message.getEntries().size() != EMPTY_BATCH_SIZE) {

                //  消费binlog
                cdcMetrics =
                    consumerService.consume(message.getEntries(), batchId, context.getCdcMetrics());

                //  binlog处理，同步指标到cdcMetrics中
                synced = saveMetrics(context.getCdcMetrics());

                //  canal状态确认、指标同步
                finishAck(context.getCdcMetrics());

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

                emptyAck(batchId);
            }
        } catch (Exception e) {
            String error = "";
            if (!synced) {
                //  当未执行到最终必须成功时,需进行rollback
                connector.rollback();
                error = "consume message error";
            } else {
                error = "ack finish status error";
            }
            logger.error("[cdc-runner] consume batch error, connection will reset..., message : {}, {}", error, e.getMessage());
            throw new SQLException(error);
        }
    }

    //  首先保存本次消费完时未提交的数据
    private boolean saveMetrics(CDCMetrics cdcMetrics) {
        metricsHandler.backup(cdcMetrics);

        return true;
    }

    private void metricsRecover() throws SQLException {
        //  设置cdc连接状态为连接状态.
        context.getCdcMetrics().connected();

        //  如果是服务重启，则需要对齐canal ack信息及redis中的ackMetrics指标
        CDCMetrics cdcMetrics = metricsHandler.query();
        long originBatchId = EMPTY_BATCH_ID;
        if (null != cdcMetrics) {
            //  当前的BatchId != -1时，表示需要进行Canal batchId ACK操作
            originBatchId = cdcMetrics.getBatchId();
            if (originBatchId != IS_BACK_UP_ID && originBatchId != EMPTY_BATCH_ID) {
                // ack确认， 回写uncommit信息
                backAfterAck(originBatchId, cdcMetrics);
            }

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, true);

            logger.info("[cdc-runner] recover from last ackMetrics position success..., originBatchId : {}",
                originBatchId);
        } else {
            //  回调告知当前为一个新的开始
            metricsHandler.newConnectCallBack(context.getCdcMetrics());
            logger.info("[cdc-runner] new connect callBack success, originBatchId : {}", originBatchId);
        }

        //  确认完毕，需要将当前未提交的数据回滚到当前已确认batchId所对应的初始位置
        connector.rollback();
    }

    //  同步指标数据
    private void finishAck(CDCMetrics cdcMetrics) throws SQLException {
        if (null != cdcMetrics) {
            long originBatchId = cdcMetrics.getBatchId();
            // ack确认， 回写unCommit信息
            backAfterAck(originBatchId, cdcMetrics);

            //  回调告知当前成功信息
            callBackSuccess(originBatchId, cdcMetrics, false);
        }
    }

    //  同步一个空的batchId
    private void emptyAck(long batchId) throws SQLException {
        //  同步状态
        connector.ack(batchId);

        //  没有新的同步信息，睡眠5ms进入下次轮训
        TimeWaitUtils.wakeupAfter(FREE_MESSAGE_WAIT_IN_MS, TimeUnit.MILLISECONDS);
    }

    /*
        由于采用2阶段prepare -> confirm模式，当进入backAfterAck的逻辑时,必须保证一致性（成功）
        所以需要在ack成功后标记batchId为-Long.MAX_VALUE，并覆盖unCommitMetrics。
        启动时重复该步骤
     */
    private void backAfterAck(long originBatchId, CDCMetrics cdcMetrics) throws SQLException {
        //  1.确认ack batchId
        connector.ack(originBatchId);
        logger.debug("ack batch success, batchId : {}", originBatchId);
        //  2.设置当前batchId为-LONG.MAX_VALUE
        cdcMetrics.setBatchId(IS_BACK_UP_ID);
        //  3.重置redis unCommit数据
        metricsHandler.backup(cdcMetrics);
        logger.debug("rest cdcMetrics with buckUpId success, origin batchId : {}", originBatchId);
    }

    private void callBackSuccess(long originBatchId, CDCMetrics temp, boolean isConnectSync) {
        context.getCdcMetrics().setCdcUnCommitMetrics(temp.getCdcUnCommitMetrics());
        context.getCdcMetrics().consumeSuccess(originBatchId, temp, isConnectSync);

        metricsHandler.callBackSuccess(context.getCdcMetrics());
    }
}
