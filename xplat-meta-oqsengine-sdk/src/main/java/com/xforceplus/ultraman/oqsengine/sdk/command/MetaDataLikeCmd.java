package com.xforceplus.ultraman.oqsengine.sdk.command;

/**
 * MetaDataLikeCmd a cmd related with metadata operation
 */
public interface MetaDataLikeCmd {

    String getBoId();

    String version();

    void clearVersion();
}
