// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface RelationUpOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RelationUp)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string relationType = 2;</code>
   */
  java.lang.String getRelationType();
  /**
   * <code>string relationType = 2;</code>
   */
  com.google.protobuf.ByteString
      getRelationTypeBytes();

  /**
   * <code>bool identity = 3;</code>
   */
  boolean getIdentity();

  /**
   * <code>.FieldUp entityField = 4;</code>
   */
  boolean hasEntityField();
  /**
   * <code>.FieldUp entityField = 4;</code>
   */
  com.xforceplus.ultraman.oqsengine.sdk.FieldUp getEntityField();
  /**
   * <code>.FieldUp entityField = 4;</code>
   */
  com.xforceplus.ultraman.oqsengine.sdk.FieldUpOrBuilder getEntityFieldOrBuilder();
}
