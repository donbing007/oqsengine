// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto;

public interface FieldConfigOrBuilder extends
    // @@protoc_insertion_point(interface_extends:FieldConfig)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>bool searchable = 1;</code>
   */
  boolean getSearchable();

  /**
   * <code>int64 max = 2;</code>
   */
  long getMax();

  /**
   * <code>int64 min = 3;</code>
   */
  long getMin();

  /**
   * <code>int32 precision = 4;</code>
   */
  int getPrecision();

  /**
   * <code>bool identifier = 5;</code>
   */
  boolean getIdentifier();

  /**
   * <code>bool isRequired = 6;</code>
   */
  boolean getIsRequired();

  /**
   * <code>string validateRegexString = 7;</code>
   */
  java.lang.String getValidateRegexString();
  /**
   * <code>string validateRegexString = 7;</code>
   */
  com.google.protobuf.ByteString
      getValidateRegexStringBytes();

  /**
   * <code>bool isSplittable = 8;</code>
   */
  boolean getIsSplittable();

  /**
   * <code>string delimiter = 9;</code>
   */
  java.lang.String getDelimiter();
  /**
   * <code>string delimiter = 9;</code>
   */
  com.google.protobuf.ByteString
      getDelimiterBytes();

  /**
   * <code>string displayType = 10;</code>
   */
  java.lang.String getDisplayType();
  /**
   * <code>string displayType = 10;</code>
   */
  com.google.protobuf.ByteString
      getDisplayTypeBytes();

  /**
   * <code>.FieldConfig.MetaFieldSense metaFieldSense = 11;</code>
   */
  int getMetaFieldSenseValue();
  /**
   * <code>.FieldConfig.MetaFieldSense metaFieldSense = 11;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.FieldConfig.MetaFieldSense getMetaFieldSense();
}
