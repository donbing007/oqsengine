// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

public interface DomainConditionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:DomainCondition)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 entityId = 1;</code>
   */
  long getEntityId();

  /**
   * <code>string entityCode = 2;</code>
   */
  java.lang.String getEntityCode();
  /**
   * <code>string entityCode = 2;</code>
   */
  com.google.protobuf.ByteString
      getEntityCodeBytes();

  /**
   * <code>string profile = 3;</code>
   */
  java.lang.String getProfile();
  /**
   * <code>string profile = 3;</code>
   */
  com.google.protobuf.ByteString
      getProfileBytes();

  /**
   * <code>int64 entityFieldId = 4;</code>
   */
  long getEntityFieldId();

  /**
   * <code>string entityFieldCode = 5;</code>
   */
  java.lang.String getEntityFieldCode();
  /**
   * <code>string entityFieldCode = 5;</code>
   */
  com.google.protobuf.ByteString
      getEntityFieldCodeBytes();

  /**
   * <code>.DomainCondition.FieldType fieldType = 6;</code>
   */
  int getFieldTypeValue();
  /**
   * <code>.DomainCondition.FieldType fieldType = 6;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.DomainCondition.FieldType getFieldType();

  /**
   * <code>.DomainCondition.Operator operator = 7;</code>
   */
  int getOperatorValue();
  /**
   * <code>.DomainCondition.Operator operator = 7;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.DomainCondition.Operator getOperator();

  /**
   * <code>string values = 8;</code>
   */
  java.lang.String getValues();
  /**
   * <code>string values = 8;</code>
   */
  com.google.protobuf.ByteString
      getValuesBytes();
}
