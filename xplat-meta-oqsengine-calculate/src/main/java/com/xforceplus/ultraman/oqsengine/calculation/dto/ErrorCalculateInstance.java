package com.xforceplus.ultraman.oqsengine.calculation.dto;

import java.util.List;

/**
 * dryRun展示计算值对比信息.
 */
public class ErrorCalculateInstance {
    // 计算错误实例
    Long id;

    // 计算错误字段信息
    List<ErrorFieldUnit> errorFieldUnits;

    public ErrorCalculateInstance(Long id, List<ErrorFieldUnit> errorFieldUnits) {
        this.id = id;
        this.errorFieldUnits = errorFieldUnits;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<ErrorFieldUnit> getErrorFieldUnits() {
        return errorFieldUnits;
    }

    public void setErrorFieldUnits(List<ErrorFieldUnit> errorFieldUnits) {
        this.errorFieldUnits = errorFieldUnits;
    }
}
