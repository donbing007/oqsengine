package com.xforceplus.ultraman.oqsengine.changelog.domain;

import java.util.List;

/**
 * json value
 */
public class ChangeValue {

    public enum Op {
        SET,

        ADD,

        DEL
    }

    private Long fieldId;

    private String fieldCode;

    private Op op;

    /**
     * raw data as string
     */
    private String rawValue;

    /**
     * mark this changeValue if is a referenceSet
     */
    private boolean isReferenceSet;

    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public Op getOp() {
        return op;
    }

    public void setOp(Op op) {
        this.op = op;
    }

    public boolean isReferenceSet() {
        return isReferenceSet;
    }

    public void setReferenceSet(boolean referenceSet) {
        isReferenceSet = referenceSet;
    }

    public String getRawValue() {
        return rawValue;
    }

    public void setRawValue(String rawValue) {
        this.rawValue = rawValue;
    }

    @Override
    public String toString() {
        return "ChangeValue{" +
                "fieldId=" + fieldId +
                ", fieldCode='" + fieldCode + '\'' +
                ", op=" + op +
                ", rawValue='" + rawValue + '\'' +
                ", isReferenceSet=" + isReferenceSet +
                '}';
    }
}
