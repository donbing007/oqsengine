package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

import static com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil.getPatternKey;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.ResetModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/7/21 5:33 PM
 */
public class SegmentId implements Serializable, Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentId.class);
    private static final long serialVersionUID = -5222792505264340312L;
    private long maxId;
    private long loadingId;
    private AtomicReference<PatternValue> currentId = new AtomicReference<>();
    private String pattern;
    private int resetable;

    @Override
    public SegmentId clone() {
        SegmentId cloneObj = null;
        try {
            cloneObj = (SegmentId) super.clone();
            cloneObj.currentId = new AtomicReference<>();
            cloneObj.currentId.set((PatternValue) this.currentId.get().clone());
            return cloneObj;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException("Clone failed!");
        }
    }


    String convert(Long id) {
        return PatternParserUtil.getInstance().parse(pattern, id);
    }


    /**
     * Get next id.
     *
     * @return IDResult
     */
    public IDResult nextId() {
        PatternValue currentValue;
        PatternValue nextValue;
        do {
            currentValue = currentId.get();
            nextValue = new PatternValue(currentValue.getId() + 1,
                PatternParserUtil.getInstance().parse(pattern, currentValue.getId() + 1));
        } while (!currentId.compareAndSet(currentValue, nextValue));
        if (nextValue.getId() > maxId) {
            return new IDResult(ResultCode.OVER, convert(nextValue.getId()));
        }
        if (nextValue.getId() >= loadingId) {
            return new IDResult(ResultCode.LOADING, convert(nextValue.getId()));
        }
        if (PatternParserUtil.needReset(pattern, currentValue, nextValue)
            && ResetModel.fromValue(resetable).equals(ResetModel.RESETABLE)) {
            LOGGER.info("Need reset currentValue  : {}, nextValue : {}", currentValue, nextValue);
            return new IDResult((ResultCode.RESET), convert(nextValue.getId()), getPatternKey(pattern, nextValue));
        } else {
            LOGGER.info("Resetable : {} currentValue : {} , nextValue :{}", resetable, currentValue, nextValue);
        }
        return new IDResult(ResultCode.NORMAL, convert(nextValue.getId()));
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public boolean useful() {
        return currentId.get().getId() <= maxId;
    }

    public long getMaxId() {
        return maxId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public long getLoadingId() {
        return loadingId;
    }

    public void setLoadingId(long loadingId) {
        this.loadingId = loadingId;
    }

    public PatternValue getCurrentId() {
        return currentId.get();
    }

    public void setCurrentId(PatternValue pattenValue) {
        this.currentId.set(pattenValue);
    }

    public int getResetable() {
        return resetable;
    }

    public void setResetable(int resetable) {
        this.resetable = resetable;
    }

    @Override
    public String toString() {
        return "[maxId="
            + maxId + ",loadingId=" + loadingId + ",currentId=" + currentId.toString() + ",patten="
            + pattern + "]";
    }
}
