// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

public interface ProfileInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:ProfileInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string code = 1;</code>
   */
  java.lang.String getCode();
  /**
   * <code>string code = 1;</code>
   */
  com.google.protobuf.ByteString
      getCodeBytes();

  /**
   * <code>repeated .EntityFieldInfo entityFieldInfo = 2;</code>
   */
  java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo> 
      getEntityFieldInfoList();
  /**
   * <code>repeated .EntityFieldInfo entityFieldInfo = 2;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo getEntityFieldInfo(int index);
  /**
   * <code>repeated .EntityFieldInfo entityFieldInfo = 2;</code>
   */
  int getEntityFieldInfoCount();
  /**
   * <code>repeated .EntityFieldInfo entityFieldInfo = 2;</code>
   */
  java.util.List<? extends com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfoOrBuilder> 
      getEntityFieldInfoOrBuilderList();
  /**
   * <code>repeated .EntityFieldInfo entityFieldInfo = 2;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfoOrBuilder getEntityFieldInfoOrBuilder(
      int index);

  /**
   * <code>repeated .RelationInfo relationInfo = 3;</code>
   */
  java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo> 
      getRelationInfoList();
  /**
   * <code>repeated .RelationInfo relationInfo = 3;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfo getRelationInfo(int index);
  /**
   * <code>repeated .RelationInfo relationInfo = 3;</code>
   */
  int getRelationInfoCount();
  /**
   * <code>repeated .RelationInfo relationInfo = 3;</code>
   */
  java.util.List<? extends com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfoOrBuilder> 
      getRelationInfoOrBuilderList();
  /**
   * <code>repeated .RelationInfo relationInfo = 3;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.RelationInfoOrBuilder getRelationInfoOrBuilder(
      int index);
}