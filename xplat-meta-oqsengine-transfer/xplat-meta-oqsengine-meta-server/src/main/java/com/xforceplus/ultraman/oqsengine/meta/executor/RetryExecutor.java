package com.xforceplus.ultraman.oqsengine.meta.executor;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.POLL_TIME_OUT_SECONDS;

import com.xforceplus.ultraman.oqsengine.meta.common.dto.WatchElement;
import com.xforceplus.ultraman.oqsengine.meta.common.executor.IDelayTaskExecutor;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RetryExecutor.
 *
 * @author xujia
 * @since 1.8
 */
public class RetryExecutor implements IDelayTaskExecutor<RetryExecutor.DelayTask> {

    final Logger logger = LoggerFactory.getLogger(RetryExecutor.class);

    private static final DelayQueue<DelayTask> delayTasks = new DelayQueue<DelayTask>();

    private volatile boolean isActive = true;

    /**
     * 获取.
     */
    public DelayTask take() {
        if (isActive) {
            try {
                return delayTasks.poll(POLL_TIME_OUT_SECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.warn("retryExecutor is interrupted, may stop server...");
            }
        }
        return null;
    }

    /**
     * 放入.
     */
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

    /**
     * delayTask.
     */
    public static class DelayTask implements Delayed {
        private final Element element;
        private final long start;
        private final long expireTime;

        /**
         * delayTask定义.
         */
        public DelayTask(long delayInMillis, Element e) {
            this.element = e;
            start = System.currentTimeMillis();
            expireTime = delayInMillis;
        }

        public Element element() {
            return element;
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
     * element.
     */
    public static class Element {
        private final WatchElement watch;
        private final String uid;
        private final String clientId;


        public Element(WatchElement w, String uid, String clientId) {
            this.uid = uid;
            this.watch = w;
            this.clientId = clientId;
        }

        public String getUid() {
            return uid;
        }

        public WatchElement getElement() {
            return watch;
        }

        public String getClientId() {
            return clientId;
        }
    }

}
