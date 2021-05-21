package com.xforceplus.ultraman.oqsengine.metadata.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.POLL_TIME_OUT_SECONDS;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 元信息淘汰执行器.
 *
 * @author xujia 2021/2/18
 * @since 1.8
 */
public class ExpireExecutor implements IDelayTaskExecutor<ExpireExecutor.DelayCleanEntity> {

    final Logger logger = LoggerFactory.getLogger(ExpireExecutor.class);

    private volatile boolean isActive = true;

    private static DelayQueue<DelayCleanEntity> delayTasks = new DelayQueue<DelayCleanEntity>();

    @Override
    public DelayCleanEntity take() {
        if (isActive) {
            try {
                return delayTasks.poll(POLL_TIME_OUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("expireExecutor is interrupted, may stop server...");
            }
        }
        return null;
    }

    @Override
    public void offer(DelayCleanEntity task) {
        if (isActive) {
            try {
                delayTasks.offer(task);
            } catch (Exception e) {
                logger.warn("offer failed, message : {}", e.getMessage());
                //  ignore
            }
        }
    }

    @Override
    public void stop() {
        isActive = false;
    }

    @Override
    public void start() {
        isActive = true;
    }

    /**
     * 延时任务.
     */
    public static class DelayCleanEntity implements Delayed {
        private Expired expired;
        private long start;
        private long expireTime;

        /**
         * 实例.
         */
        public DelayCleanEntity(long delayInMillis, Expired expired) {
            this.expired = expired;
            start = System.currentTimeMillis();
            expireTime = delayInMillis;
        }

        public Expired element() {
            return expired;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(start + expireTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed o) {
            return (int) (this.getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS));
        }
    }

    /**
     * 过期信息.
     */
    public static class Expired {
        private String appId;
        private int version;

        public Expired(String appId, int version) {
            this.appId = appId;
            this.version = version;
        }

        public String getAppId() {
            return appId;
        }

        public int getVersion() {
            return version;
        }
    }
}
