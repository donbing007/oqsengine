package com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.IDResult;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.ResultCode;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/8/21 10:01 PM
 */
public class LocalCacheGenerator implements IDGenerator {


    protected String bizType;
    protected SegmentService segmentService;
    protected volatile SegmentId current;
    protected volatile SegmentId next;
    private volatile boolean isLoadingNext;
    private Object lock = new Object();
    private ExecutorService executorService;

    public LocalCacheGenerator(String bizType, SegmentService segmentService,ExecutorService executorService) {
        this.bizType = bizType;
        this.segmentService = segmentService;
        this.executorService = executorService;
        loadCurrent();
    }

    public synchronized void loadCurrent() {
        if (current == null || !current.useful()) {
            if (next == null) {
                SegmentId segmentId = querySegmentId();
                this.current = segmentId;
            } else {
                current = next;
                next = null;
            }
        }
    }

    public synchronized void resetBizType(IDResult result) {
        String message = null;
        if(current != null) {
            current = null;
        }
        if(next != null) {
            next = null;
        }
        String patternKey = result.getPatternKey();
        try {
            segmentService.resetSegment(bizType, patternKey);
        }
        catch (Throwable throwable) {
            message = throwable.getMessage();
        }
        if(message != null) {
            throw new IDGeneratorException("Error reset the segment: "+ message);
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
        if (next == null && !isLoadingNext) {
            synchronized (lock) {
                if (next == null && !isLoadingNext) {
                    isLoadingNext = true;
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 无论获取下个segment成功与否，都要将isLoadingNext赋值为false
                                next = querySegmentId();
                            } finally {
                                isLoadingNext = false;
                            }
                        }
                    });
                }
            }
        }
    }

    @Override
    public String nextId() {
        while (true) {
            if (current == null) {
                loadCurrent();
                continue;
            }
            IDResult result = current.nextId();
            if (result.getCode() == ResultCode.OVER) {
                loadCurrent();
            }
            else if(result.getCode() == ResultCode.RESET) {
                resetBizType(result);
            }
            else {
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
