// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

public interface EntityFieldInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:EntityFieldInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 id = 1;</code>
   */
  long getId();

  /**
   * <code>string name = 2;</code>
   */
  java.lang.String getName();
  /**
   * <code>string name = 2;</code>
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string cname = 3;</code>
   */
  java.lang.String getCname();
  /**
   * <code>string cname = 3;</code>
   */
  com.google.protobuf.ByteString
      getCnameBytes();

  /**
   * <code>.EntityFieldInfo.FieldType fieldType = 4;</code>
   */
  int getFieldTypeValue();
  /**
   * <code>.EntityFieldInfo.FieldType fieldType = 4;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityFieldInfo.FieldType getFieldType();

  /**
   * <code>string dictId = 5;</code>
   */
  java.lang.String getDictId();
  /**
   * <code>string dictId = 5;</code>
   */
  com.google.protobuf.ByteString
      getDictIdBytes();

  /**
   * <code>string defaultValue = 6;</code>
   */
  java.lang.String getDefaultValue();
  /**
   * <code>string defaultValue = 6;</code>
   */
  com.google.protobuf.ByteString
      getDefaultValueBytes();

  /**
   * <code>.FieldConfig fieldConfig = 7;</code>
   */
  boolean hasFieldConfig();
  /**
   * <code>.FieldConfig fieldConfig = 7;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig getFieldConfig();
  /**
   * <code>.FieldConfig fieldConfig = 7;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfigOrBuilder getFieldConfigOrBuilder();

  /**
   * <code>.Calculator calculator = 9;</code>
   */
  boolean hasCalculator();
  /**
   * <code>.Calculator calculator = 9;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Calculator getCalculator();
  /**
   * <code>.Calculator calculator = 9;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.CalculatorOrBuilder getCalculatorOrBuilder();
}
