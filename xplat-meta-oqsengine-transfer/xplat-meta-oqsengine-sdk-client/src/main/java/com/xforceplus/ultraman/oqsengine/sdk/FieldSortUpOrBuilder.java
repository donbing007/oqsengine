// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface FieldSortUpOrBuilder extends
    // @@protoc_insertion_point(interface_extends:FieldSortUp)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string code = 1;</code>
   */
  String getCode();
  /**
   * <code>string code = 1;</code>
   */
  com.google.protobuf.ByteString
      getCodeBytes();

  /**
   * <code>.FieldSortUp.Order order = 2;</code>
   */
  int getOrderValue();
  /**
   * <code>.FieldSortUp.Order order = 2;</code>
   */
  FieldSortUp.Order getOrder();

  /**
   * <code>.FieldUp field = 3;</code>
   */
  boolean hasField();
  /**
   * <code>.FieldUp field = 3;</code>
   */
  FieldUp getField();
  /**
   * <code>.FieldUp field = 3;</code>
   */
  FieldUpOrBuilder getFieldOrBuilder();
}