package com.xforceplus.ultraman.oqsengine.cdc.consumer;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_STOP_WAIT_LOOPS;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.MAX_STOP_WAIT_TIME;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.RECONNECT_WAIT_IN_SECONDS;

import com.xforceplus.ultraman.oqsengine.cdc.connect.AbstractCDCConnector;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.process.BatchProcessor;
import com.xforceplus.ultraman.oqsengine.cdc.context.RunnerContext;
import com.xforceplus.ultraman.oqsengine.meta.common.utils.TimeWaitUtils;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.RunningStatus;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * desc : 处理client端通讯,及外层逻辑.
 * name : CdcConnectorRunner.
 *
 * @author : xujia 2020/11/3
 * @since : 1.8
 */
public class CDCRunner extends Thread {

    final Logger logger = LoggerFactory.getLogger(CDCRunner.class);

    private BatchProcessor batchProcessor;

    private AbstractCDCConnector connector;

    private RunnerContext context;

    public RunnerContext getContext() {
        return context;
    }

    /**
     * 实例化.
     */
    public CDCRunner(BatchProcessor batchProcessor, AbstractCDCConnector connector) {

        this.batchProcessor = batchProcessor;
        this.connector = connector;
        this.context = new RunnerContext();
    }

    /**
     * 优雅停止.
     */
    public void shutdown() {
        //  设置运行状态为尝试停止
        context.setRunningStatus(RunningStatus.TRY_STOP);
        batchProcessor.shutdown();

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
        batchProcessor.init();

        while (true) {
            //  判断当前服务状态是否终止，如果是则关闭.
            if (needTerminate()) {
                break;
            }

            try {
                //  连接
                connector.open();

                //  首先将上次记录完整的信息(batchID)确认到canalServer中
                recover();
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
                continue;
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

        batchProcessor.error(context);
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
    private void consume() throws SQLException {
        while (true) {
            //  服务被终止
            if (context.getRunningStatus().shouldStop()) {
                context.setRunningStatus(RunningStatus.STOP_SUCCESS);
                break;
            }

            batchProcessor.executeOneBatch(connector, context);
        }
    }

    private void recover() throws SQLException {
        //  设置cdc连接状态为连接状态.
        context.getCdcMetrics().connected();

        batchProcessor.recover(connector, context);

        //  确认完毕，需要将当前未提交的数据回滚到当前已确认batchId所对应的初始位置
        connector.rollback();
    }
}
