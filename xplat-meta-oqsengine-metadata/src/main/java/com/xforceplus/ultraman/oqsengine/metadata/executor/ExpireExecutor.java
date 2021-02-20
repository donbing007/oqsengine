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

    private static DelayQueue<DelayCleanEntity> delayTasks = new DelayQueue<DelayCleanEntity>();

    public DelayCleanEntity take() {
        try {
            return delayTasks.take();
        } catch (InterruptedException e) {
            logger.warn("expireExecutor is interrupted, may stop server...");
            //  ignore
        }
        return null;
    }

    public void offer(DelayCleanEntity task) {
        try {
            delayTasks.offer(task);
        } catch (Exception e) {
            logger.warn("offer failed, message : {}", e.getMessage());
            //  ignore
        }
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
