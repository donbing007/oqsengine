// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

public interface EntityClassSyncResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:EntityClassSyncResponse)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string appId = 1;</code>
     */
    java.lang.String getAppId();

    /**
     * <code>string appId = 1;</code>
     */
    com.google.protobuf.ByteString
    getAppIdBytes();

    /**
     * <code>int32 version = 2;</code>
     */
    int getVersion();

    /**
     * <code>string uid = 3;</code>
     */
    java.lang.String getUid();

    /**
     * <code>string uid = 3;</code>
     */
    com.google.protobuf.ByteString
    getUidBytes();

    /**
     * <code>int32 status = 4;</code>
     */
    int getStatus();

    /**
     * <code>string env = 5;</code>
     */
    java.lang.String getEnv();

    /**
     * <code>string env = 5;</code>
     */
    com.google.protobuf.ByteString
    getEnvBytes();

    /**
     * <code>string md5 = 6;</code>
     */
    java.lang.String getMd5();

    /**
     * <code>string md5 = 6;</code>
     */
    com.google.protobuf.ByteString
    getMd5Bytes();

    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    boolean hasEntityClassSyncRspProto();

    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto getEntityClassSyncRspProto();

    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder getEntityClassSyncRspProtoOrBuilder();

    /**
     * <code>bool force = 8;</code>
     */
    boolean getForce();
}
