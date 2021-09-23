package com.xforceplus.ultraman.oqsengine.common.timerwheel;

import com.xforceplus.ultraman.oqsengine.common.pool.ExecutorHelper;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 时间轮转算法实现.
 * 所有的时间单位都以毫秒为单位.
 * 默认分隔为512个槽位,每一个槽位时间区间为100毫秒.
 *
 * @param <T> 管理的元素类型.
 * @author dongbin
 * @version 1.0 2020-03-10 11:53:19
 * @since 1.5
 */
public class TimerWheel<T> {

    private static final int DEFAULT_SLOT_NUMBER = 512;
    private static final int DEFAULT_DURATION = 100;

    private final Lock lock = new ReentrantLock();

    /*
     * 时间单位
     */
    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    /*
     * 每个slot间隔时间
     */
    private final long duration;
    private final TimeoutNotification<T> notification;
    private final int slotNumber;
    private final List<Slot> wheel;
    private final ExecutorService worker;
    private int currentSlot;
    /*
     * 删除助手.
     */
    private final Map<T, Integer> removeHelp;

    /**
     * 以默认的512个槽位,和100毫秒为间隔,并失效不通知构造一个新的时间轮.
     */
    public TimerWheel() {
        this(-1, -1, null);
    }

    /**
     * 以默认的512个槽位,和100毫秒为间隔,失效会进行通知.
     *
     * @param notification 失效通知器.
     */
    public TimerWheel(TimeoutNotification<T> notification) {
        this(-1, -1, notification);
    }

    /**
     * 指定槽位数量,指定间隔毫秒数,并且不通知失效.
     *
     * @param slotNumber 槽位数量
     * @param duration   槽位间隔毫秒.
     */
    public TimerWheel(int slotNumber, long duration) {
        this(slotNumber, duration, null);
    }

    /**
     * 指定槽位数量,指定间隔毫秒数,并且通知失效.
     *
     * @param slotNumber   槽位数量.
     * @param duration     槽位间隔毫秒.
     * @param notification 失效通知器.
     */
    public TimerWheel(int slotNumber, long duration, TimeoutNotification<T> notification) {

        if (duration <= 0) {
            this.duration = DEFAULT_DURATION;
        } else {
            this.duration = duration;
        }

        if (slotNumber <= 3) {
            this.slotNumber = DEFAULT_SLOT_NUMBER;
        } else {
            this.slotNumber = slotNumber;
        }

        if (notification == null) {
            this.notification = t -> TimeoutNotification.OVERDUE;
        } else {
            this.notification = notification;
        }

        this.currentSlot = 0;

        wheel = new ArrayList<>(this.slotNumber);

        for (int i = 0; i < this.slotNumber; i++) {
            wheel.add(new Slot());
        }

        this.removeHelp = new ConcurrentHashMap<>(16);

        worker = new ThreadPoolExecutor(
            1,
            1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(1000),
            ExecutorHelper.buildNameThreadFactory("time-wheel", true)
        );

        worker.submit(new PointTask());
    }

    /**
     * 增加一个在指定时间过期的对象.
     *
     * @param target  需要过期的对象.
     * @param timeout 存活的持续时间.
     */
    public void add(T target, long timeout) {
        if (target == null) {
            throw new NullPointerException("Target object is null!");
        }
        if (timeout <= 0) {
            return;
        }
        long specialTimeout = timeout;
        if (specialTimeout < this.duration) {
            specialTimeout = this.duration;
        }
        int virtualSlotIndex;
        int actuallySlotIndex;
        int round;
        lock.lock();
        try {
            virtualSlotIndex = calculateVirtualSlot(specialTimeout);
            actuallySlotIndex = calculateActuallySlot(virtualSlotIndex);
            round = calculateRuound(virtualSlotIndex);

            Slot slot = wheel.get(actuallySlotIndex);
            slot.add(target, round);

        } finally {
            lock.unlock();
        }

        this.removeHelp.put(target, actuallySlotIndex);
    }

    /**
     * 增加一个在指定时间过期的目标.
     *
     * @param target     目标实例.
     * @param expireDate 到期时间.
     */
    public void add(T target, Date expireDate) {
        add(target, expireDate.getTime() - System.currentTimeMillis());
    }

    /**
     * 判断是否存在指定对象.
     *
     * @param target 要检查的目标对象.
     * @return true存在, false不存在.
     */
    public boolean exist(T target) {
        return removeHelp.containsKey(target);
    }

    /**
     * 从环中删除目标.不会触发过期回调.
     *
     * @param target 目标.
     */
    public void remove(T target) {
        if (target == null) {
            return;
        }

        Integer slotIndex = this.removeHelp.remove(target);
        lock.lock();
        try {
            if (slotIndex != null) {
                Slot slot = this.wheel.get(slotIndex);
                slot.remove(target);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 当前环中总共持有过期目标.
     *
     * @return 总共还有多少未过期目标.
     */
    public int size() {
        return removeHelp.size();
    }

    //计算round
    private int calculateRuound(int virtualSlot) {
        return virtualSlot / slotNumber;
    }

    private int calculateActuallySlot(int virtualSlot) {
        return virtualSlot % slotNumber;
    }

    //计算虚似slot.会超过最大slot位.
    private int calculateVirtualSlot(long timeout) {
        return (int) (currentSlot + timeout / duration);
    }

    private class Slot {

        private final List<Element> elements = new LinkedList<>();

        public void add(T obj, int round) {

            Element element = new Element(obj, round);
            elements.add(element);

        }

        public List<T> expire() {
            List<T> expireList = new LinkedList<>();
            Iterator<Element> iter = elements.iterator();
            Element element;
            while (iter.hasNext()) {
                element = iter.next();
                if (element.getRound() <= 0) {
                    expireList.add(element.getTarget());
                    iter.remove();
                } else {
                    element.reduceRound();
                }

            }
            return expireList;
        }

        public void remove(Object target) {
            final int notFound = -1;
            int index = notFound;
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).getTarget().equals(target)) {
                    index = i;
                    break;
                }
            }
            if (index > notFound) {
                elements.remove(index);
            }
        }

        @Override
        public String toString() {
            return "Slot{" + "elements=" + elements + '}';
        }

    }

    private class Element {

        private final T target;
        private int round;

        public Element(T target, int round) {
            this.target = target;
            this.round = round;
        }

        public T getTarget() {
            return target;
        }

        public int reduceRound() {
            return round--;
        }

        public int getRound() {
            return round;
        }

        @Override
        public String toString() {
            return "Element{" + "target=" + target + ", round=" + round + '}';
        }

    }

    private class PointTask implements Runnable {

        @Override
        public void run() {

            List<T> expireList;
            Slot slot;
            do {

                lock.lock();
                try {
                    slot = wheel.get(currentSlot);
                    expireList = slot.expire();

                    currentSlot = (currentSlot + 1) % slotNumber;
                } finally {
                    lock.unlock();
                }

                long resultTime;
                try {
                    for (T target : expireList) {
                        resultTime = notification.notice(target);
                        if (resultTime == TimeoutNotification.OVERDUE) {
                            removeHelp.remove(target);
                        } else {
                            add(target, resultTime);
                        }
                    }
                } catch (Throwable ex) {

                    //不处理任何异常,这里只是为了防止线程意外中止.
                    ex.printStackTrace(System.err);
                }

                try {
                    timeUnit.sleep(duration);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }

            } while (!Thread.interrupted());
        }

    }

}
