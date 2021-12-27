package com.xforceplus.ultraman.oqsengine.calculation.logic.initcalculation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 初始化结果.
 *
 * @version 0.1 2021/12/6 14:51
 * @Auther weikai
 * @since 1.8
 */
public class InitResultInfo implements Serializable {
    // key:entityClassId, value: fieldsIds
    private Map<Long, List<Long>> failedInfo;

    // key:entityClassId, value: fieldsIds
    private Map<Long, List<Long>> successInfo;

    public InitResultInfo() {
        this.failedInfo = new HashMap<>();
        this.successInfo = new HashMap<>();
    }

    public Map<Long, List<Long>> getFailedInfo() {
        return failedInfo;
    }

    public void setFailedInfo(Map<Long, List<Long>> failedInfo) {
        this.failedInfo = failedInfo;
    }

    public Map<Long, List<Long>> getSuccessInfo() {
        return successInfo;
    }

    public void setSuccessInfo(Map<Long, List<Long>> successInfo) {
        this.successInfo = successInfo;
    }

    @Override
    public String toString() {
        return "InitResultInfo{" + "failedList=" + failedInfo + ", successList=" + successInfo + '}';
    }
}
