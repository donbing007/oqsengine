// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

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
   * <code>string displayType = 8;</code>
   */
  java.lang.String getDisplayType();
  /**
   * <code>string displayType = 8;</code>
   */
  com.google.protobuf.ByteString
      getDisplayTypeBytes();

  /**
   * <code>.FieldConfig.MetaFieldSense metaFieldSense = 9;</code>
   */
  int getMetaFieldSenseValue();
  /**
   * <code>.FieldConfig.MetaFieldSense metaFieldSense = 9;</code>
   */
  com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FieldConfig.MetaFieldSense getMetaFieldSense();

  /**
   * <code>int32 fuzzyType = 10;</code>
   */
  int getFuzzyType();

  /**
   * <code>int32 wildcardMinWidth = 11;</code>
   */
  int getWildcardMinWidth();

  /**
   * <code>int32 wildcardMaxWidth = 12;</code>
   */
  int getWildcardMaxWidth();

  /**
   * <code>string uniqueName = 13;</code>
   */
  java.lang.String getUniqueName();
  /**
   * <code>string uniqueName = 13;</code>
   */
  com.google.protobuf.ByteString
      getUniqueNameBytes();

  /**
   * <code>bool crossSearch = 14;</code>
   */
  boolean getCrossSearch();

  /**
   * <code>int32 length = 15;</code>
   */
  int getLength();

  /**
   * <code>int32 valueFloatScale = 16;</code>
   */
  int getValueFloatScale();

  /**
   * <code>int32 jdbcType = 17;</code>
   */
  int getJdbcType();
}
