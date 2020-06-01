package com.xforceplus.ultraman.oqsengine.sdk.command;

import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;

/**
 * condition export command
 */
public class ConditionExportCmd implements MetaDataLikeCmd, ContextAwareCmd {

    private String boId;

    private String version;

    private String exportType;

    private ConditionQueryRequest conditionQueryRequest;

    public ConditionExportCmd(String boId, ConditionQueryRequest conditionQueryRequest, String version, String exportType) {
        this.boId = boId;
        this.version = version;
        this.conditionQueryRequest = conditionQueryRequest;
        this.exportType = exportType;
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

    public String getExportType(){
        return this.exportType;
    }

    @Override
    public void clearVersion() {
        this.version = null;
    }
}
