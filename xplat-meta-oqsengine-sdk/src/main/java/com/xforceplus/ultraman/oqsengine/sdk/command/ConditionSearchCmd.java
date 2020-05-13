package com.xforceplus.ultraman.oqsengine.sdk.command;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;

/**
 * ConditionSearchCmd
 */
public class ConditionSearchCmd implements MetaDataLikeCmd{

    private String boId;

    private String version;

    private ConditionQueryRequest conditionQueryRequest;

    public ConditionSearchCmd(String boId, ConditionQueryRequest conditionQueryRequest, String version) {
        this.boId = boId;
        this.conditionQueryRequest = conditionQueryRequest;
        this.version = version;
    }

    @Override
    public String getBoId() {
        return boId;
    }

    @Override
    public String version() {
        return version;
    }

    public ConditionQueryRequest getConditionQueryRequest() {
        return conditionQueryRequest;
    }

    @Override
    public void clearVersion() {
        this.version = null;
    }
}
