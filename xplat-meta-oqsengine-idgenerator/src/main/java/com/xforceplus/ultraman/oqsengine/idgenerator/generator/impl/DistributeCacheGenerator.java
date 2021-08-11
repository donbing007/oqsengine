//package com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl;
//
//import com.hazelcast.cp.IAtomicReference;
//import com.hazelcast.cp.lock.FencedLock;
//import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.IDResult;
//import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.ResultCode;
//import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
//import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
//import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
//import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
//import com.xforceplus.ultraman.oqsengine.idgenerator.util.HazelcastUtil;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//
///**
// * Generator Hazelcast 号段缓存实现.
// *
// * @author leo
// * @version 0.1 2021/5/13 11:59
// * @since 1.8
// */
//public class DistributeCacheGenerator implements IDGenerator {
//
//
//    protected String bizType;
//    protected SegmentService segmentService;
//    protected IAtomicReference<SegmentId> current;
//    protected IAtomicReference<SegmentId> next;
//    private IAtomicReference<Boolean> isLoadingNext;
//    private ExecutorService executorService;
//
//    /**
//     * Constructor.
//     *
//     * @param bizType bizType
//     * @param segmentService segmentService
//     * @param executorService executorService
//     */
//    public DistributeCacheGenerator(String bizType, SegmentService segmentService, ExecutorService executorService) {
//        this.bizType = bizType;
//        this.segmentService = segmentService;
//        this.current = HazelcastUtil.getInstance().getCPSubsystem().getAtomicReference("current");
//        this.next = HazelcastUtil.getInstance().getCPSubsystem().getAtomicReference("next");
//        this.isLoadingNext = HazelcastUtil.getInstance().getCPSubsystem().getAtomicReference("isLoadingNext");
//        this.executorService = executorService;
//        loadCurrent();
//    }
//
//    /**
//     * Get the current segment.
//     */
//    public synchronized void loadCurrent() {
//        FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
//        lock.lock();
//        try {
//            if (current == null || current.get() == null || !current.get().useful()) {
//                if (next == null || next.get() == null) {
//                    SegmentId segmentId = querySegmentId();
//                    this.current.set(segmentId);
//                } else {
//                    current.set(next.get());
//                    next.set(null);
//                }
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    private SegmentId querySegmentId() {
//        String message = null;
//        try {
//            SegmentId segmentId = segmentService.getNextSegmentId(bizType);
//            if (segmentId != null) {
//                return segmentId;
//            }
//        } catch (Exception e) {
//            message = e.getMessage();
//            throw new IDGeneratorException(
//                String.format("error query segment: bizType: %s error message :%s ", bizType, message));
//        }
//        throw new IDGeneratorException(
//            String.format("error query segment: bizType: %s error message :%s ", bizType, message));
//    }
//
//    /**
//     * Load the next segment.
//     */
//    public void loadNext() {
//        if (next.get() == null && (isLoadingNext.get() == null || !isLoadingNext.get())) {
//            FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
//            lock.lock();
//            try {
//                if (next.get() == null && (isLoadingNext.get() == null || !isLoadingNext.get())) {
//                    isLoadingNext.set(true);
//                    executorService.submit(new Runnable() {
//                        @Override
//                        public void run() {
//                            try {
//                                next.set(querySegmentId());
//                            } finally {
//                                isLoadingNext.set(false);
//                            }
//                        }
//                    });
//                }
//            } finally {
//                lock.unlock();
//            }
//
//        }
//    }
//
//
//    /**
//     * Reset the segment.
//     *
//     * @param result result of nexId
//     */
//    public synchronized void resetBizType(IDResult result) {
//        FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
//        lock.lock();
//        try {
//            String message = null;
//            if (current != null) {
//                current.set(null);
//            }
//            if (next != null) {
//                next.set(null);
//            }
//            String patternKey = result.getPatternKey();
//            try {
//                segmentService.resetSegment(bizType, patternKey);
//            } catch (Throwable throwable) {
//                message = throwable.getMessage();
//            }
//            if (message != null) {
//                throw new IDGeneratorException("Error reset the segment: " + message);
//            }
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    @Override
//    public String nextId() {
//        while (true) {
//            if (current.get() == null) {
//                loadCurrent();
//                continue;
//            }
//            IDResult result = null;
//            FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
//            lock.lock();
//            try {
//                SegmentId val = current.get();
//                result = val.nextId();
//                current.set(val);
//            } finally {
//                lock.unlock();
//            }
//            if (result.getCode() == ResultCode.OVER) {
//                loadCurrent();
//            } else if (result.getCode() == ResultCode.RESET) {
//                resetBizType(result);
//            } else {
//                if (result.getCode() == ResultCode.LOADING) {
//                    loadNext();
//                }
//                return result.getId();
//            }
//        }
//    }
//
//    @Override
//    public List<String> nextIds(Integer batchSize) {
//        List<String> ids = new ArrayList<>();
//        for (int i = 0; i < batchSize; i++) {
//            String id = nextId();
//            ids.add(id);
//        }
//        return ids;
//    }
//}
