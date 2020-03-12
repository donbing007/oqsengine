package com.xforceplus.ultraman.oqsengine.sdk.command;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;

public class ConditionSearchCmd implements MetaDataLikeCmd{

    private String boId;

    private ConditionQueryRequest conditionQueryRequest;

    public ConditionSearchCmd(String boId, ConditionQueryRequest conditionQueryRequest) {
        this.boId = boId;
        this.conditionQueryRequest = conditionQueryRequest;
    }

    public String getBoId() {
        return boId;
    }

    public ConditionQueryRequest getConditionQueryRequest() {
        return conditionQueryRequest;
    }
}
