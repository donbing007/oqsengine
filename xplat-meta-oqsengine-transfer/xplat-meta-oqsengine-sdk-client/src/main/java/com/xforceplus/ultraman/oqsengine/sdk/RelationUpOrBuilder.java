// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface RelationUpOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RelationUp)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1;</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1;</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string relationType = 2;</code>
   * @return The relationType.
   */
  java.lang.String getRelationType();
  /**
   * <code>string relationType = 2;</code>
   * @return The bytes for relationType.
   */
  com.google.protobuf.ByteString
      getRelationTypeBytes();

  /**
   * <code>bool identity = 3;</code>
   * @return The identity.
   */
  boolean getIdentity();

  /**
   * <code>.FieldUp entityField = 4;</code>
   * @return Whether the entityField field is set.
   */
  boolean hasEntityField();
  /**
   * <code>.FieldUp entityField = 4;</code>
   * @return The entityField.
   */
  com.xforceplus.ultraman.oqsengine.sdk.FieldUp getEntityField();
  /**
   * <code>.FieldUp entityField = 4;</code>
   */
  com.xforceplus.ultraman.oqsengine.sdk.FieldUpOrBuilder getEntityFieldOrBuilder();

  /**
   * <code>int64 relatedEntityClassId = 5;</code>
   * @return The relatedEntityClassId.
   */
  long getRelatedEntityClassId();
}
