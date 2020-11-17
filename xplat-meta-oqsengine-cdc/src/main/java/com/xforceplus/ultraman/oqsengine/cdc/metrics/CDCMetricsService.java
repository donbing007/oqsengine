package com.xforceplus.ultraman.oqsengine.cdc.metrics;

import com.xforceplus.ultraman.oqsengine.cdc.consumer.callback.CDCMetricsCallback;
import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.CDCStatus;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCAckMetrics;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCMetricsConstant.*;


/**
 * desc :
 * name : CDCMetricsService
 *
 * @author : xujia
 * date : 2020/11/7
 * @since : 1.8
 */
public class CDCMetricsService {

    final Logger logger = LoggerFactory.getLogger(CDCMetricsService.class);

    @Resource
    private CDCMetricsCallback cdcMetricsCallback;

    //  sync pool
    private ExecutorService cdcSyncPool;

    private CDCMetrics cdcMetrics;

    public CDCMetricsService() {
        initCdcSyncPool();

        cdcMetrics = new CDCMetrics();
    }

    private void initCdcSyncPool() {
        //  启动一个线程数大小为1线程池进行CDC指标的同步
        cdcSyncPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(MAX_QUEUE_SIZE),
            ExecutorHelper.buildNameThreadFactory(POOL_NAME, true),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }

    public CDCMetrics getCdcMetrics() {
        return cdcMetrics;
    }

    public void callBackSuccess(CDCAckMetrics temp) {
        this.getCdcMetrics().consumeSuccess(temp);
        callback();
    }

    public void callBackError(CDCStatus cdcStatus) {
        this.getCdcMetrics().getCdcAckMetrics().setCdcConsumerStatus(cdcStatus);
        callback();
    }

    public void backup(CDCMetrics cdcMetrics) {
        cdcSyncPool.submit(() -> {
            try {
                cdcMetricsCallback.cdcSaveLastUnCommit(cdcMetrics);
            } catch (Exception e) {
                logger.error("back up unCommitMetrics to redis error, unCommitMetrics : {}", cdcMetrics.toString());
                e.printStackTrace();
            }
        });
    }

    public CDCMetrics query() throws SQLException {
        try {
            return cdcMetricsCallback.queryLastUnCommit();
        } catch (Exception e) {
            e.printStackTrace();
            throw new SQLException("query unCommitMetrics from redis error.");
        }
    }

    public void callback() {
        //  设置本次callback的时间
        cdcMetrics.getCdcAckMetrics().setLastUpdateTime(System.currentTimeMillis());
        //  异步执行回调
        cdcSyncPool.submit(() -> {
            try {
                cdcMetricsCallback.cdcAck(cdcMetrics.getCdcAckMetrics());
            } catch (Exception e) {
                logger.error("callback error, metrics : {}", cdcMetrics.getCdcAckMetrics().toString());
                e.printStackTrace();
            }
        });
    }
}
