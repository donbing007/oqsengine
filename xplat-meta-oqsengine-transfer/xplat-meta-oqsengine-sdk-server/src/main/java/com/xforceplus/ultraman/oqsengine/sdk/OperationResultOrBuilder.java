// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface OperationResultOrBuilder extends
    // @@protoc_insertion_point(interface_extends:OperationResult)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   *code
   * </pre>
   *
   * <code>.OperationResult.Code code = 1;</code>
   */
  int getCodeValue();
  /**
   * <pre>
   *code
   * </pre>
   *
   * <code>.OperationResult.Code code = 1;</code>
   */
  OperationResult.Code getCode();

  /**
   * <pre>
   *message
   * </pre>
   *
   * <code>string message = 2;</code>
   */
  String getMessage();
  /**
   * <pre>
   *message
   * </pre>
   *
   * <code>string message = 2;</code>
   */
  com.google.protobuf.ByteString
      getMessageBytes();

  /**
   * <code>repeated .EntityUp queryResult = 3;</code>
   */
  java.util.List<EntityUp>
      getQueryResultList();
  /**
   * <code>repeated .EntityUp queryResult = 3;</code>
   */
  EntityUp getQueryResult(int index);
  /**
   * <code>repeated .EntityUp queryResult = 3;</code>
   */
  int getQueryResultCount();
  /**
   * <code>repeated .EntityUp queryResult = 3;</code>
   */
  java.util.List<? extends EntityUpOrBuilder>
      getQueryResultOrBuilderList();
  /**
   * <code>repeated .EntityUp queryResult = 3;</code>
   */
  EntityUpOrBuilder getQueryResultOrBuilder(
          int index);

  /**
   * <code>string transactionResult = 4;</code>
   */
  String getTransactionResult();
  /**
   * <code>string transactionResult = 4;</code>
   */
  com.google.protobuf.ByteString
      getTransactionResultBytes();

  /**
   * <code>int32 affectedRow = 5;</code>
   */
  int getAffectedRow();

  /**
   * <code>repeated int64 ids = 6;</code>
   */
  java.util.List<Long> getIdsList();
  /**
   * <code>repeated int64 ids = 6;</code>
   */
  int getIdsCount();
  /**
   * <code>repeated int64 ids = 6;</code>
   */
  long getIds(int index);

  /**
   * <code>int32 totalRow = 7;</code>
   */
  int getTotalRow();
}