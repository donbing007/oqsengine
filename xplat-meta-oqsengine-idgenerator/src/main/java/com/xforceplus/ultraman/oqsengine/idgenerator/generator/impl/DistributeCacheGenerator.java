package com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl;

import com.hazelcast.cp.IAtomicReference;
import com.hazelcast.cp.lock.FencedLock;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.NamedThreadFactory;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.IDResult;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.ResultCode;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.util.HazelcastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/8/21 10:01 PM
 */
public class DistributeCacheGenerator implements IDGenerator {


    protected String bizType;
    protected SegmentService segmentService;
    protected IAtomicReference<SegmentId> current;
    protected IAtomicReference<SegmentId> next;
    private IAtomicReference<Boolean>  isLoadingNext;
    private ExecutorService executorService;

    public DistributeCacheGenerator(String bizType, SegmentService segmentService) {
        this.bizType = bizType;
        this.segmentService = segmentService;
        this.current = HazelcastUtil.getInstance().getCPSubsystem().getAtomicReference("current");
        this.next = HazelcastUtil.getInstance().getCPSubsystem().getAtomicReference("next");
        this.isLoadingNext =  HazelcastUtil.getInstance().getCPSubsystem().getAtomicReference("isLoadingNext");
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("oqs-id-generator"));
        loadCurrent();
    }

    public synchronized void loadCurrent() {
        FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
        lock.lock();
        try {
            if (current == null || current.get() == null || !current.get().useful()) {
                if (next == null || next.get() == null) {
                    SegmentId segmentId = querySegmentId();
                    this.current.set(segmentId);
                } else {
                    current.set(next.get());
                    next.set(null);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private SegmentId querySegmentId() {
        String message = null;
        try {
            SegmentId segmentId = segmentService.getNextSegmentId(bizType);
            if (segmentId != null) {
                return segmentId;
            }
        } catch (Exception e) {
            message = e.getMessage();
        }
        throw new IDGeneratorException("error query segment: " + message);
    }

    public void loadNext() {
        if (next.get() == null && (isLoadingNext.get() == null || !isLoadingNext.get())) {
            FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
            lock.lock();
            try {
                if (next.get() == null && (isLoadingNext.get() == null || !isLoadingNext.get())) {
                    isLoadingNext.set(true);
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                next.set(querySegmentId());
                            } finally {
                                isLoadingNext.set(false);
                            }
                        }
                    });
                }
            }
            finally {
                lock.unlock();
            }

        }
    }

    @Override
    public String nextId() {
        while (true) {
            if (current.get() == null) {
                loadCurrent();
                continue;
            }
            IDResult result = null;
            FencedLock lock = HazelcastUtil.getInstance().getCPSubsystem().getLock(this.bizType);
            lock.lock();
            try {
                SegmentId val = current.get();
                result = val.nextId();
                current.set(val);
            }
            finally {
                lock.unlock();
            }
            if (result.getCode() == ResultCode.OVER) {
                loadCurrent();
            } else {
                if (result.getCode() == ResultCode.LOADING) {
                    loadNext();
                }
                return result.getId();
            }
        }
    }

    @Override
    public List<String> nextIds(Integer batchSize) {
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            String id = nextId();
            ids.add(id);
        }
        return ids;
    }
}
