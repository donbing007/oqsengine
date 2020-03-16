package com.xforceplus.ultraman.oqsengine.sdk.command;

public class SingleDeleteCmd implements MetaDataLikeCmd{

    private String boId;
    private  String id;


    public SingleDeleteCmd(String boId, String id) {
        this.boId = boId;
        this.id = id;
    }

    public String getBoId() {
        return boId;
    }

    public String getId() {
        return id;
    }
}
