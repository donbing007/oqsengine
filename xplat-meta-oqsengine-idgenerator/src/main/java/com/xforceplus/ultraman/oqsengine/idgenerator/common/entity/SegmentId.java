package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserUtil;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/7/21 5:33 PM
 */
public class SegmentId implements Serializable {

    private static final long serialVersionUID = -5222792505264340312L;
    private long maxId;
    private long loadingId;
    private AtomicReference<PattenValue> currentId = new AtomicReference<>();
    private String patten;

    String convert(Long id) {
        return PattenParserUtil.getInstance().parse(patten,id);
    }


    public IDResult nextId() {
        PattenValue idValue = currentId.updateAndGet(pattenValue -> {
            PattenValue newValue = new PattenValue(pattenValue.getId()+1,
                    PattenParserUtil.getInstance().parse(patten,pattenValue.getId()+1));
            return newValue;
        });
        if (idValue.getId() > maxId) {
            return new IDResult(ResultCode.OVER, convert(idValue.getId()));
        }
        if (idValue.getId() >= loadingId) {
            return new IDResult(ResultCode.LOADING, convert(idValue.getId()));
        }
        return new IDResult(ResultCode.NORMAL, convert(idValue.getId()));
    }

    public String getPatten() {
        return patten;
    }

    public void setPatten(String patten) {
        this.patten = patten;
    }

    public boolean useful() {
        return currentId.get().getId()  <= maxId;
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

    public PattenValue getCurrentId() {
        return currentId.get();
    }

    public void setCurrentId(PattenValue pattenValue) {
        this.currentId.set(pattenValue);
    }

    @Override
    public String toString() {
        return "[maxId=" + maxId + ",loadingId=" + loadingId + ",currentId=" + currentId  + ",patten="+ patten +"]";
    }
}
