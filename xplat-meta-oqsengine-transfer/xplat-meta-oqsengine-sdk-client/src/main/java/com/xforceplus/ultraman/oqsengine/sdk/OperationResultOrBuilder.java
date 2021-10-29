// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface OperationResultOrBuilder extends
    // @@protoc_insertion_point(interface_extends:OperationResult)
    com.google.protobuf.MessageOrBuilder {

    /**
     * <pre>
     * code
     * </pre>
     *
     * <code>.OperationResult.Code code = 1;</code>
     *
     * @return The enum numeric value on the wire for code.
     */
    int getCodeValue();

    /**
     * <pre>
     * code
     * </pre>
     *
     * <code>.OperationResult.Code code = 1;</code>
     *
     * @return The code.
     */
    com.xforceplus.ultraman.oqsengine.sdk.OperationResult.Code getCode();

    /**
     * <pre>
     * message
     * </pre>
     *
     * <code>string message = 2;</code>
     *
     * @return The message.
     */
    java.lang.String getMessage();

    /**
     * <pre>
     * message
     * </pre>
     *
     * <code>string message = 2;</code>
     *
     * @return The bytes for message.
     */
    com.google.protobuf.ByteString
    getMessageBytes();

    /**
     * <code>repeated .EntityUp queryResult = 3;</code>
     */
    java.util.List<com.xforceplus.ultraman.oqsengine.sdk.EntityUp>
    getQueryResultList();

    /**
     * <code>repeated .EntityUp queryResult = 3;</code>
     */
    com.xforceplus.ultraman.oqsengine.sdk.EntityUp getQueryResult(int index);

    /**
     * <code>repeated .EntityUp queryResult = 3;</code>
     */
    int getQueryResultCount();

    /**
     * <code>repeated .EntityUp queryResult = 3;</code>
     */
    java.util.List<? extends com.xforceplus.ultraman.oqsengine.sdk.EntityUpOrBuilder>
    getQueryResultOrBuilderList();

    /**
     * <code>repeated .EntityUp queryResult = 3;</code>
     */
    com.xforceplus.ultraman.oqsengine.sdk.EntityUpOrBuilder getQueryResultOrBuilder(
        int index);

    /**
     * <code>string transactionResult = 4;</code>
     *
     * @return The transactionResult.
     */
    java.lang.String getTransactionResult();

    /**
     * <code>string transactionResult = 4;</code>
     *
     * @return The bytes for transactionResult.
     */
    com.google.protobuf.ByteString
    getTransactionResultBytes();

    /**
     * <code>int32 affectedRow = 5;</code>
     *
     * @return The affectedRow.
     */
    int getAffectedRow();

    /**
     * <code>repeated int64 ids = 6;</code>
     *
     * @return A list containing the ids.
     */
    java.util.List<java.lang.Long> getIdsList();

    /**
     * <code>repeated int64 ids = 6;</code>
     *
     * @return The count of ids.
     */
    int getIdsCount();

    /**
     * <code>repeated int64 ids = 6;</code>
     *
     * @param index The index of the element to return.
     * @return The ids at the given index.
     */
    long getIds(int index);

    /**
     * <code>int32 totalRow = 7;</code>
     *
     * @return The totalRow.
     */
    int getTotalRow();
}
