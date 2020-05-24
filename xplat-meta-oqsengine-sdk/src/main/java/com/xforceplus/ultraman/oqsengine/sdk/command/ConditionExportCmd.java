package com.xforceplus.ultraman.oqsengine.sdk.command;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;

/**
 * condition export command
 */
public class ConditionExportCmd implements MetaDataLikeCmd, ContextAwareCmd {

    private String boId;

    private String version;

    private ConditionQueryRequest conditionQueryRequest;

    public ConditionExportCmd(String boId, ConditionQueryRequest conditionQueryRequest, String version) {
        this.boId = boId;
        this.version = version;
        this.conditionQueryRequest = conditionQueryRequest;
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
