package com.xforceplus.ultraman.oqsengine.common.thread;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 03/2022.
 * 轮询的线程执行者.
 *
 * @since 1.8
 */
public class PollingThreadExecutor<T> implements LifeCycledThread<T> {

    private final Logger logger = LoggerFactory.getLogger(PollingThreadExecutor.class);

    private Thread worker;

    private int duration = 1;
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    private int maxStopTimes = 10;

    private volatile boolean tryStop = false;

    /**
     * 构建一个轮询的执行者.
     *
     * @param consumer 传入的消费执行器.
     * @param element  执行器的实际参数.
     * @param duration 每次轮询的间隔.
     * @param timeUnit 每次轮询间隔的时间单位.
     * @param maxWait  执行者关闭时最大等待次数.
     */
    public PollingThreadExecutor(String workName, int duration, TimeUnit timeUnit, int maxWait, Consumer<T> consumer,
                                 T element) {
        if (duration > 0) {
            this.duration = duration;
        }
        if (null != timeUnit) {
            this.timeUnit = timeUnit;
        }
        if (maxWait > 0) {
            this.maxStopTimes = maxWait;
        }

        worker = new Thread(() -> {
            while (!tryStop) {
                consumer.accept(element);

                wakeupAfter(this.duration, this.timeUnit);
            }
        });
        worker.setName(workName);
    }

    @Override
    public void start() {
        logger.info("[pollingThread-{}] start.", worker.getName());
        worker.start();
    }


    @Override
    public void stop() {
        tryStop = true;

        int tryTimes = 0;

        //  等待直到线程结束.
        while (tryTimes < maxStopTimes) {
            if (!worker.isAlive()) {
                logger.info("[pollingThread-{}] stopped.", worker.getName());
                break;
            }
            wakeupAfter(this.duration, this.timeUnit);

            tryTimes++;
        }

        //  线程仍然活跃
        if (worker.isAlive()) {
            logger.warn("[pollingThread-{}] stopped by force.", worker.getName());
            worker.interrupt();
        }
    }

    /**
     * 等待timeSeconds秒后进行重试.
     */
    private void wakeupAfter(long duration, TimeUnit timeUnit) {
        try {
            Thread.sleep(timeUnit.toMillis(duration));
        } catch (InterruptedException e) {
            //  ignore
        }
    }
}
