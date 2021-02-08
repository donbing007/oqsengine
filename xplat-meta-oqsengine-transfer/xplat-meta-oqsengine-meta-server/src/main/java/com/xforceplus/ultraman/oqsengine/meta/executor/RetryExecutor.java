package com.xforceplus.ultraman.oqsengine.meta.executor;

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
public class RetryExecutor implements IRetryExecutor {

    private static DelayQueue<DelayTask> delayTasks = new DelayQueue<DelayTask>();

    public DelayTask take() {
        try {
            return delayTasks.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void offer(DelayTask task) {
        delayTasks.offer(task);
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
        private String uid;
        private String appId;
        private int version;

        public Element(String appId, int version, String uid) {
            this.uid = uid;
            this.appId = appId;
            this.version = version;
        }

        public String getUid() {
            return uid;
        }

        public String getAppId() {
            return appId;
        }

        public int getVersion() {
            return version;
        }
    }

}
