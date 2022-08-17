package com.xforceplus.ultraman.oqsengine.calculation.dto;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;

/**
 * dryRun信息对比最小字段单元.
 */
public class ErrorFieldUnit {
    // 字段
    private IEntityField field;

    // 当前计算值
    private IValue now;

    // 期待值
    private IValue expect;


    /**
     * 默认构造器.
     *
     * @param field 字段
     * @param now 当前值
     * @param expect 期待值
     */
    public ErrorFieldUnit(IEntityField field, IValue now, IValue expect) {
        this.field = field;
        this.now = now;
        this.expect = expect;
    }

    public IEntityField getField() {
        return field;
    }

    public void setField(IEntityField field) {
        this.field = field;
    }

    public IValue getNow() {
        return now;
    }

    public void setNow(IValue now) {
        this.now = now;
    }

    public IValue getExpect() {
        return expect;
    }

    public void setExpect(IValue expect) {
        this.expect = expect;
    }


}
