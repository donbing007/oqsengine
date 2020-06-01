package com.xforceplus.ultraman.oqsengine.sdk.command;

/**
 * download template
 */
public class GetImportTemplateCmd implements MetaDataLikeCmd {

    private String version;

    private String boId;

    public GetImportTemplateCmd(String boId, String version) {
        this.version = version;
        this.boId = boId;
    }

    @Override
    public String getBoId() {
        return boId;
    }

    @Override
    public String version() {
        return version;
    }

    @Override
    public void clearVersion() {
        this.version = null;
    }
}
