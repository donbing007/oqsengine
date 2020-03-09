// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public interface SelectByConditionOrBuilder extends
    // @@protoc_insertion_point(interface_extends:SelectByCondition)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.EntityUp entity = 1;</code>
   */
  boolean hasEntity();
  /**
   * <code>.EntityUp entity = 1;</code>
   */
  EntityUp getEntity();
  /**
   * <code>.EntityUp entity = 1;</code>
   */
  EntityUpOrBuilder getEntityOrBuilder();

  /**
   * <code>int32 pageNo = 2;</code>
   */
  int getPageNo();

  /**
   * <code>int32 pageSize = 3;</code>
   */
  int getPageSize();

  /**
   * <code>.ConditionsUp conditions = 4;</code>
   */
  boolean hasConditions();
  /**
   * <code>.ConditionsUp conditions = 4;</code>
   */
  ConditionsUp getConditions();
  /**
   * <code>.ConditionsUp conditions = 4;</code>
   */
  ConditionsUpOrBuilder getConditionsOrBuilder();

  /**
   * <code>repeated .FieldSortUp sort = 5;</code>
   */
  java.util.List<FieldSortUp>
      getSortList();
  /**
   * <code>repeated .FieldSortUp sort = 5;</code>
   */
  FieldSortUp getSort(int index);
  /**
   * <code>repeated .FieldSortUp sort = 5;</code>
   */
  int getSortCount();
  /**
   * <code>repeated .FieldSortUp sort = 5;</code>
   */
  java.util.List<? extends FieldSortUpOrBuilder>
      getSortOrBuilderList();
  /**
   * <code>repeated .FieldSortUp sort = 5;</code>
   */
  FieldSortUpOrBuilder getSortOrBuilder(
          int index);

  /**
   * <code>repeated .QueryFieldsUp queryFields = 6;</code>
   */
  java.util.List<QueryFieldsUp>
      getQueryFieldsList();
  /**
   * <code>repeated .QueryFieldsUp queryFields = 6;</code>
   */
  QueryFieldsUp getQueryFields(int index);
  /**
   * <code>repeated .QueryFieldsUp queryFields = 6;</code>
   */
  int getQueryFieldsCount();
  /**
   * <code>repeated .QueryFieldsUp queryFields = 6;</code>
   */
  java.util.List<? extends QueryFieldsUpOrBuilder>
      getQueryFieldsOrBuilderList();
  /**
   * <code>repeated .QueryFieldsUp queryFields = 6;</code>
   */
  QueryFieldsUpOrBuilder getQueryFieldsOrBuilder(
          int index);
}
