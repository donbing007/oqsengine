// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

public interface CalculatorOrBuilder extends
    // @@protoc_insertion_point(interface_extends:Calculator)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 calculateType = 1;</code>
   */
  int getCalculateType();

  /**
   * <code>string expression = 2;</code>
   */
  java.lang.String getExpression();
  /**
   * <code>string expression = 2;</code>
   */
  com.google.protobuf.ByteString
      getExpressionBytes();

  /**
   * <code>string validator = 3;</code>
   */
  java.lang.String getValidator();
  /**
   * <code>string validator = 3;</code>
   */
  com.google.protobuf.ByteString
      getValidatorBytes();

  /**
   * <code>string min = 4;</code>
   */
  java.lang.String getMin();
  /**
   * <code>string min = 4;</code>
   */
  com.google.protobuf.ByteString
      getMinBytes();

  /**
   * <code>string max = 5;</code>
   */
  java.lang.String getMax();
  /**
   * <code>string max = 5;</code>
   */
  com.google.protobuf.ByteString
      getMaxBytes();

  /**
   * <code>string condition = 6;</code>
   */
  java.lang.String getCondition();
  /**
   * <code>string condition = 6;</code>
   */
  com.google.protobuf.ByteString
      getConditionBytes();

  /**
   * <code>string emptyValueTransfer = 7;</code>
   */
  java.lang.String getEmptyValueTransfer();
  /**
   * <code>string emptyValueTransfer = 7;</code>
   */
  com.google.protobuf.ByteString
      getEmptyValueTransferBytes();

  /**
   * <code>string patten = 8;</code>
   */
  java.lang.String getPatten();
  /**
   * <code>string patten = 8;</code>
   */
  com.google.protobuf.ByteString
      getPattenBytes();

  /**
   * <code>string model = 9;</code>
   */
  java.lang.String getModel();
  /**
   * <code>string model = 9;</code>
   */
  com.google.protobuf.ByteString
      getModelBytes();

  /**
   * <code>int32 step = 10;</code>
   */
  int getStep();

  /**
   * <code>int32 level = 11;</code>
   */
  int getLevel();

  /**
   * <code>repeated string args = 12;</code>
   */
  java.util.List<java.lang.String>
      getArgsList();
  /**
   * <code>repeated string args = 12;</code>
   */
  int getArgsCount();
  /**
   * <code>repeated string args = 12;</code>
   */
  java.lang.String getArgs(int index);
  /**
   * <code>repeated string args = 12;</code>
   */
  com.google.protobuf.ByteString
      getArgsBytes(int index);

  /**
   * <code>int32 failedPolicy = 13;</code>
   */
  int getFailedPolicy();

  /**
   * <code>.google.protobuf.Any failedDefaultValue = 14;</code>
   */
  boolean hasFailedDefaultValue();
  /**
   * <code>.google.protobuf.Any failedDefaultValue = 14;</code>
   */
  com.google.protobuf.Any getFailedDefaultValue();
  /**
   * <code>.google.protobuf.Any failedDefaultValue = 14;</code>
   */
  com.google.protobuf.AnyOrBuilder getFailedDefaultValueOrBuilder();

  /**
   * <code>int64 lookupEntityClassId = 15;</code>
   */
  long getLookupEntityClassId();

  /**
   * <code>int64 lookupEntityFieldId = 16;</code>
   */
  long getLookupEntityFieldId();

  /**
   * <code>int32 resetType = 17;</code>
   */
  int getResetType();

  /**
   * <code>int32 domainNoSenior = 18;</code>
   */
  int getDomainNoSenior();

  /**
   * <code>int32 aggregationBoId = 19;</code>
   */
  int getAggregationBoId();

  /**
   * <code>int32 aggregationFieldId = 20;</code>
   */
  int getAggregationFieldId();

  /**
   * <code>int32 aggregationType = 21;</code>
   */
  int getAggregationType();

  /**
   * <code>int32 aggregationRelationId = 22;</code>
   */
  int getAggregationRelationId();

  /**
   * <code>string domainCondition = 23;</code>
   */
  java.lang.String getDomainCondition();
  /**
   * <code>string domainCondition = 23;</code>
   */
  com.google.protobuf.ByteString
      getDomainConditionBytes();

  /**
   * <code>map&lt;int64, int64&gt; aggregationByFields = 24;</code>
   */
  int getAggregationByFieldsCount();
  /**
   * <code>map&lt;int64, int64&gt; aggregationByFields = 24;</code>
   */
  boolean containsAggregationByFields(
      long key);
  /**
   * Use {@link #getAggregationByFieldsMap()} instead.
   */
  @java.lang.Deprecated
  java.util.Map<java.lang.Long, java.lang.Long>
  getAggregationByFields();
  /**
   * <code>map&lt;int64, int64&gt; aggregationByFields = 24;</code>
   */
  java.util.Map<java.lang.Long, java.lang.Long>
  getAggregationByFieldsMap();
  /**
   * <code>map&lt;int64, int64&gt; aggregationByFields = 24;</code>
   */

  long getAggregationByFieldsOrDefault(
      long key,
      long defaultValue);
  /**
   * <code>map&lt;int64, int64&gt; aggregationByFields = 24;</code>
   */

  long getAggregationByFieldsOrThrow(
      long key);
}
