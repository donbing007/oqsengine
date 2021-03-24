package com.xforceplus.ultraman.oqsengine.meta.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * desc :
 * name : RetryExecutor
 *
 * @author : xujia
 * date : 2021/2/5
 * @since : 1.8
 */
public class RetryExecutor implements IDelayTaskExecutor<RetryExecutor.DelayTask> {

    final Logger logger = LoggerFactory.getLogger(RetryExecutor.class);

    private static DelayQueue<DelayTask> delayTasks = new DelayQueue<DelayTask>();

    private volatile boolean isActive = true;

    public DelayTask take() {
        if (isActive) {
            try {
                return delayTasks.take();
            } catch (InterruptedException e) {
                logger.warn("retryExecutor is interrupted, may stop server...");
            }
        }
        return null;
    }

    public void offer(DelayTask task) {
        if (isActive) {
            try {
                delayTasks.offer(task);
            } catch (Exception e) {
                logger.warn("offer failed, message : {}", e.getMessage());
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


    public static class DelayTask implements Delayed {
        private Element e;
        private long start;
        private long expireTime;

        public DelayTask(long delayInMillis, Element e) {
            this.e = e;
            start = System.currentTimeMillis();
            expireTime = delayInMillis;
        }

        public Element element() {
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

    public static class Element {
        private WatchElement w;
        private String uid;


        public Element(WatchElement w, String uid) {
            this.uid = uid;
            this.w = w;
        }

        public String getUid() {
            return uid;
        }

        public WatchElement getElement() {
            return w;
        }
    }

}
