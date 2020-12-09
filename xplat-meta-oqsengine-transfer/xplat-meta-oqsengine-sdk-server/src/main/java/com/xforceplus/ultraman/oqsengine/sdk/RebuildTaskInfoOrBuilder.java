// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: rebuild.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface RebuildTaskInfoOrBuilder extends
    // @@protoc_insertion_point(interface_extends:RebuildTaskInfo)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int64 tid = 1;</code>
   * @return The tid.
   */
  long getTid();

  /**
   * <code>bool isDone = 2;</code>
   * @return The isDone.
   */
  boolean getIsDone();

  /**
   * <code>bool isCancel = 3;</code>
   * @return The isCancel.
   */
  boolean getIsCancel();

  /**
   * <code>int32 percentage = 4;</code>
   * @return The percentage.
   */
  int getPercentage();

  /**
   * <code>string status = 5;</code>
   * @return The status.
   */
  java.lang.String getStatus();
  /**
   * <code>string status = 5;</code>
   * @return The bytes for status.
   */
  com.google.protobuf.ByteString
      getStatusBytes();

  /**
   * <code>int64 entityId = 6;</code>
   * @return The entityId.
   */
  long getEntityId();

  /**
   * <code>int64 starts = 7;</code>
   * @return The starts.
   */
  long getStarts();

  /**
   * <code>int64 ends = 8;</code>
   * @return The ends.
   */
  long getEnds();

  /**
   * <code>int32 batchSize = 9;</code>
   * @return The batchSize.
   */
  int getBatchSize();

  /**
   * <code>int32 finishSize = 10;</code>
   * @return The finishSize.
   */
  int getFinishSize();

  /**
   * <code>string errCode = 11;</code>
   * @return The errCode.
   */
  java.lang.String getErrCode();
  /**
   * <code>string errCode = 11;</code>
   * @return The bytes for errCode.
   */
  com.google.protobuf.ByteString
      getErrCodeBytes();

  /**
   * <code>string message = 12;</code>
   * @return The message.
   */
  java.lang.String getMessage();
  /**
   * <code>string message = 12;</code>
   * @return The bytes for message.
   */
  com.google.protobuf.ByteString
      getMessageBytes();
}
