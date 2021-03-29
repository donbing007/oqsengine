package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : ExpireExecutor
 *
 * @author : xujia
 * date : 2021/2/18
 * @since : 1.8
 */
public class ExpireExecutor implements IDelayTaskExecutor<ExpireExecutor.DelayCleanEntity>  {

    final Logger logger = LoggerFactory.getLogger(ExpireExecutor.class);

    private volatile boolean isActive = true;

    private static DelayQueue<DelayCleanEntity> delayTasks = new DelayQueue<DelayCleanEntity>();

    @Override
    public DelayCleanEntity take() {
        if (isActive) {
            try {
                return delayTasks.take();
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

    public static class DelayCleanEntity implements Delayed {
        private Expired e;
        private long start;
        private long expireTime;

        public DelayCleanEntity(long delayInMillis, Expired e) {
            this.e = e;
            start = System.currentTimeMillis();
            expireTime = delayInMillis;
        }

        public Expired element() {
            return e;
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
     *
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
