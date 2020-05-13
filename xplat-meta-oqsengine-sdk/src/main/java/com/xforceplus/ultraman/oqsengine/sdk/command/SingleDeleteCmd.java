package com.xforceplus.ultraman.oqsengine.sdk.command;

/**
 * single record delete
 */
public class SingleDeleteCmd implements MetaDataLikeCmd{

    private String boId;
    private String id;
    private String version;


    public SingleDeleteCmd(String boId, String id, String version) {
        this.boId = boId;
        this.id = id;
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

    public String getId() {
        return id;
    }

    @Override
    public void clearVersion() {
        this.version = null;
    }
}
