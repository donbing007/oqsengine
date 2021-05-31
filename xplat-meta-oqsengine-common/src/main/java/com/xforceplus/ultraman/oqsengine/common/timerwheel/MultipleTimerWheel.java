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
 * @author weikai 2021/5/21 9:56
 * @version 1.0 2021/5/21 9:56
 * @since 1.5
 */

public class MultipleTimerWheel<T> implements ITimerWheel<T> {
    //DEFAULT_SLOT_NUMBER对应工作轮，槽位数512
    private static final int DEFAULT_SLOT_NUMBER = 512;
    //DEFAULT_SECOND_SLOT_NUMBER对应二级轮，槽位数60
    private static final int DEFAULT_SECOND_SLOT_NUMBER = 60;
    //DEFAULT_THIRD_SLOT_NUMBER对应三级轮，槽位数60
    private static final int DEFAULT_THIRD_SLOT_NUMBER = 60;
    //DEFAULT_FOURTH_SLOT_NUMBER对应四级轮轮，槽位数24
    private static final int DEFAULT_FOURTH_SLOT_NUMBER = 24;

    //工作轮每个Slot默认间隔时间
    private static final int DEFAULT_DURATION = 100;

    private final TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private boolean initTick = true;

    /**
     * 每个slot间隔时间.
     */
    private final long duration;
    private final long secondDuration;
    private final long thirdDuration;
    private final long fourthDuration;

    private final TimeoutNotification<T> notification;
    private final int slotNumber;
    private final List<Slot> workWheel;
    private final List<Slot> secondWheel;
    private final List<Slot> thirdWheel;
    private final List<Slot> fourthWheel;
    private final List<List<Slot>> wheelList;
    private final ExecutorService worker;
    private int currentSlot;
    private int secondCurrentSlot;
    private int thirdCurrentSlot;
    private int fourthCurrentSlot;
    /**
     * 删除助手.
     */
    private final Map<T, int[]> removeHelp;


    /**
     * 以默认的512个槽位,和100毫秒为间隔,失效会进行通知.
     *
     * @param notification 失效通知器.
     */
    public MultipleTimerWheel(TimeoutNotification<T> notification) {
        this(-1, -1, notification);
    }

    /**
     * 初始化时间轮.
     *
     * @param slotNumber 工作轮槽位数.
     * @param duration 间隔时间.
     * @param notification  失效通知器.
     */
    public MultipleTimerWheel(int slotNumber, long duration, TimeoutNotification<T> notification) {

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
            this.notification = new TimeoutNotification<T>() {
                @Override
                public long notice(T t) {
                    return 0;
                }
            };
        } else {
            this.notification = notification;
        }
        //初始化多级时间轮时间间隔
        this.secondDuration = this.duration * this.slotNumber;
        this.thirdDuration = this.secondDuration * DEFAULT_SECOND_SLOT_NUMBER;
        this.fourthDuration = this.thirdDuration * DEFAULT_THIRD_SLOT_NUMBER;

        this.currentSlot = 0;
        this.secondCurrentSlot = 0;
        this.thirdCurrentSlot = 0;
        this.fourthCurrentSlot = 0;

        this.workWheel = new ArrayList<>(this.slotNumber);
        this.secondWheel = new ArrayList<>(DEFAULT_SECOND_SLOT_NUMBER);
        this.thirdWheel = new ArrayList<>(DEFAULT_THIRD_SLOT_NUMBER);
        this.fourthWheel = new ArrayList<>(DEFAULT_FOURTH_SLOT_NUMBER);

        this.wheelList = new ArrayList<>(4);
        this.removeHelp = new ConcurrentHashMap<>(16);

        initWheelList();

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

    private void initWheelList() {

        for (int i = 0; i < this.slotNumber; i++) {
            this.workWheel.add(new Slot());
        }
        for (int i = 0; i < DEFAULT_SECOND_SLOT_NUMBER; i++) {
            this.secondWheel.add(new Slot());
        }
        for (int i = 0; i < DEFAULT_THIRD_SLOT_NUMBER; i++) {
            this.thirdWheel.add(new Slot());
        }
        for (int i = 0; i < DEFAULT_FOURTH_SLOT_NUMBER; i++) {
            this.fourthWheel.add(new Slot());
        }

        this.wheelList.add(this.workWheel);
        this.wheelList.add(this.secondWheel);
        this.wheelList.add(this.thirdWheel);
        this.wheelList.add(this.fourthWheel);
    }

    /**
     * 增加一个在指定时间过期的对象.
     *
     * @param target  需要过期的对象.
     * @param timeout 存活的持续时间.
     */
    @Override
    public void add(T target, long timeout) {
        if (target == null) {
            throw new NullPointerException("Target object is null!");
        }
        if (timeout <= 0) {
            return;
        }
        long specialTimeout = timeout;
        if (specialTimeout <= this.duration) {
            specialTimeout = this.duration * 2;
        }
        int round = -1;
        int current = this.currentSlot;
        int actuallyWheel;
        actuallyWheel = calculateActuallyWheel(specialTimeout, current);
        int actuallySlotIndex;
        actuallySlotIndex = calculateActuallySlot(specialTimeout, actuallyWheel, current);
        if (actuallyWheel < 3) {
            //前三级轮无次轮概念
            round = 0;
        } else if (actuallyWheel == 3) {
            round = (int) (specialTimeout / (fourthDuration * DEFAULT_FOURTH_SLOT_NUMBER));
        }
        long remainingTime = calculateRemainingTime(specialTimeout, current);

        Slot slot = wheelList.get(actuallyWheel).get(actuallySlotIndex);  //获取应该加入的槽位
        slot.addElement(target, round, remainingTime);

        removeHelp.put(target, new int[]{actuallyWheel, actuallySlotIndex});
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
    @Override
    public void remove(T target) {
        if (target == null) {
            return;
        }
        //获取value数组，包含轮位、槽位
        int[] value = removeHelp.remove(target);

        if (value != null) {
            wheelList.get(value[0]).get(value[1]).remove(target);
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


    /**
     * 计算任务添加时应该放到几级时间轮.
     *
     * @param specialTimeout 超时时间
     * @param current        当前工作轮指针位置
     * @return 返回实际轮
     */
    private int calculateActuallyWheel(long specialTimeout, int current) {
        if (specialTimeout < this.secondDuration - current * this.duration) {
            return 0;
        } else if (secondDuration - current * this.duration <= specialTimeout && specialTimeout < thirdDuration - current * this.duration) {
            return 1;
        } else if (thirdDuration - current * this.duration <= specialTimeout && specialTimeout < fourthDuration - current * this.duration) {
            return 2;
        } else if (specialTimeout >= fourthDuration) {
            return 3;
        } else {
            return -1;
        }
    }


    /**
     * 计算实际slot.
     *
     * @param timeout       超时时间
     * @param actuallyWheel 添加的实际轮
     * @param current       工作轮当前指针位置
     * @return 返回实际槽位
     */
    private int calculateActuallySlot(long timeout, int actuallyWheel, int current) {
        int actuallySlot;
        if (actuallyWheel == 0) {
            if (timeout % duration == 0) {
                actuallySlot = (int) ((current + timeout / duration) % this.slotNumber);
            } else {
                actuallySlot = (int) ((current + timeout / duration + 1) % this.slotNumber);
            }
        } else if (actuallyWheel == 1) {
            actuallySlot = (int) ((secondCurrentSlot + (timeout + current * this.duration) / secondDuration) % DEFAULT_SECOND_SLOT_NUMBER);
        } else if (actuallyWheel == 2) {
            actuallySlot = (int) ((thirdCurrentSlot + (timeout + current * this.duration) / thirdDuration) % DEFAULT_THIRD_SLOT_NUMBER);
        } else if (actuallyWheel == 3) {
            actuallySlot = (int) ((fourthCurrentSlot + (timeout + current * this.duration) / fourthDuration) % DEFAULT_FOURTH_SLOT_NUMBER);
        } else {
            actuallySlot = -1;
        }
        return actuallySlot;
    }


    /**
     * 计算2,3,4级时间轮中任务剩余时长.
     *
     * @param specialTimeout 超时时间
     * @param current        工作轮当前指针位置
     * @return 返回剩余时间
     */
    private long calculateRemainingTime(long specialTimeout, int current) {
        long remainingTime = 0;
        if (specialTimeout < this.secondDuration - current * this.duration) {
            remainingTime = 0;
        } else if (secondDuration - current * this.duration < specialTimeout && specialTimeout < thirdDuration - current * this.duration) {
            remainingTime = (specialTimeout + current * this.duration) % secondDuration;
        } else if (thirdDuration - current * this.duration < specialTimeout && specialTimeout < fourthDuration - current * this.duration) {
            remainingTime = (specialTimeout + current * this.duration) % thirdDuration;
        } else if (specialTimeout - current * this.duration > fourthDuration) {
            remainingTime = (specialTimeout + current * this.duration) % fourthDuration;
        }
        return remainingTime;
    }

    /**
     * 拨动指针，若高级轮指针发生拨动则转移高级轮任务至低级轮.
     *
     * @return 返回工作轮拨动后的槽slot
     */
    private Slot getWorkWheelSlotAndTickWheel() {
        Slot slot;
        if (initTick) {
            initTick = false;
            currentSlot = (currentSlot + 1) % slotNumber;
            slot = workWheel.get(0);
        } else if (currentSlot != 0) {
            slot = tickWorkWheel();
        } else if ((++secondCurrentSlot) % DEFAULT_SECOND_SLOT_NUMBER != 0) {
            secondCurrentSlot %= DEFAULT_SECOND_SLOT_NUMBER;
            slot = tickSecondWheel();
        } else if ((++thirdCurrentSlot) % DEFAULT_THIRD_SLOT_NUMBER != 0) {
            thirdCurrentSlot %= DEFAULT_SECOND_SLOT_NUMBER;
            secondCurrentSlot %= DEFAULT_SECOND_SLOT_NUMBER;
            slot = tickThirdWheel();
        } else {
            thirdCurrentSlot %= DEFAULT_SECOND_SLOT_NUMBER;
            secondCurrentSlot %= DEFAULT_SECOND_SLOT_NUMBER;
            slot = tickFourthWheel();
        }
        return slot;
    }

    /**
     * 拨动工作轮.
     *
     * @return 返回工作轮拨动后的槽slot
     */
    private Slot tickWorkWheel() {
        Slot slot = workWheel.get(currentSlot);
        currentSlot = (currentSlot + 1) % slotNumber;
        return slot;
    }

    /**
     * 拨动工作轮、二极轮.
     *
     * @return 返回工作轮拨动后的槽slot
     */
    private Slot tickSecondWheel() {

        Slot secondSlot = secondWheel.get(secondCurrentSlot);
        wheelDeliver(secondSlot, removeHelp);

        Slot slot = workWheel.get(currentSlot);
        currentSlot = (currentSlot + 1) % slotNumber;
        return slot;
    }

    /**
     * 拨动工作轮、二极轮、三级轮.
     *
     * @return 返回工作轮拨动后的槽slot
     */
    private Slot tickThirdWheel() {
        Slot secondSlot = secondWheel.get(secondCurrentSlot);
        wheelDeliver(secondSlot, removeHelp);

        Slot thirdSlot = thirdWheel.get(thirdCurrentSlot);
        wheelDeliver(thirdSlot, removeHelp);

        Slot slot = workWheel.get(currentSlot);
        currentSlot = (currentSlot + 1) % slotNumber;
        return slot;
    }

    /**
     * 拨动工作轮、二极轮、三级轮、四级轮.
     *
     * @return 返回工作轮拨动后的槽slot
     */
    private Slot tickFourthWheel() {
        Slot secondSlot = secondWheel.get(secondCurrentSlot);
        wheelDeliver(secondSlot, removeHelp);

        Slot thirdSlot = thirdWheel.get(thirdCurrentSlot);
        wheelDeliver(thirdSlot, removeHelp);

        fourthCurrentSlot = (fourthCurrentSlot + 1) % DEFAULT_FOURTH_SLOT_NUMBER;
        Slot fourthSlot = fourthWheel.get(fourthCurrentSlot);
        if (fourthSlot.elements.size() > 0) {
            fourthSlot.lock.lock();
            try {
                Iterator<Element> iterator = fourthSlot.elements.iterator();
                Element element;
                while (iterator.hasNext()) {
                    element = iterator.next();
                    if (element.getRound() <= 0) {
                        T target = element.getTarget();
                        long timeout = element.getRemainingTime();
                        removeHelp.remove(target);
                        if (timeout == 0) {
                            timeout = this.duration;
                        }
                        add(target, timeout);
                        iterator.remove();
                    } else {
                        element.reduceRound();
                    }
                }
            } finally {
                fourthSlot.lock.unlock();
            }

        }

        Slot slot = workWheel.get(currentSlot);
        currentSlot = (currentSlot + 1) % slotNumber;
        return slot;
    }

    /**
     * 转移助手.
     *
     * @param slot       待转移槽位
     * @param removeHelp 删除助手
     */
    private void wheelDeliver(Slot slot, Map<T, int[]> removeHelp) {
        if (slot.elements.size() > 0) {
            slot.lock.lock();
            try {
                for (Element element : slot.elements) {
                    removeHelp.remove(element.getTarget());
                    if (element.getRemainingTime() > 0) {
                        add(element.getTarget(), element.getRemainingTime());
                    } else {
                        add(element.getTarget(), this.duration);
                    }
                }
                slot.elements.clear();
            } finally {
                slot.lock.unlock();
            }
        }
    }

    private class Element {

        private final T target;
        private int round;
        private long remainingTime;

        public Element(T target, int round, long remainingTime) {
            this.target = target;
            this.round = round;
            this.remainingTime = remainingTime;
        }

        public long getRemainingTime() {
            return remainingTime;
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

    private class Slot {
        private final List<Element> elements = new LinkedList<>();
        private final Lock lock = new ReentrantLock();

        public void addElement(T obj, int round, long remainingTime) {
            Element element = new Element(obj, round, remainingTime);
            lock.lock();
            try {
                elements.add(element);
            } finally {
                lock.unlock();
            }
        }

        /**
         * 淘汰过期任务，添加任务不会加入工作轮当前槽位，不加锁.
         */
        public void expire() {
            long resultTime;
            for (Element element : elements) {
                resultTime = notification.notice(element.getTarget());
                if (resultTime > 0) {
                    add(element.getTarget(), resultTime);
                } else {
                    removeHelp.remove(element.getTarget());
                }
            }
            elements.clear();
        }

        public void remove(Object target) {
            final int notFound = -1;
            int index = notFound;
            lock.lock();
            try {
                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i).getTarget().equals(target)) {
                        index = i;
                        break;
                    }
                }
                if (index > notFound) {
                    elements.remove(index);
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String toString() {
            return "Slot{" + "elements=" + elements + '}';
        }
    }

    private class PointTask implements Runnable {

        @Override
        public void run() {
            Slot slot;
            long startTime;
            long endTime;
            do {
                startTime = System.currentTimeMillis();
                slot = getWorkWheelSlotAndTickWheel();
                slot.expire();
                try {
                    endTime = System.currentTimeMillis();
                    long sleepTime = duration - (endTime - startTime);
                    if (sleepTime > 0) {
                        timeUnit.sleep(sleepTime);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } while (!Thread.interrupted());
        }
    }
}

