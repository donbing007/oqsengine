// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

public final class EntityResourceProto {
  private EntityResourceProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_SelectByCondition_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_SelectByCondition_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ConditionsUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ConditionsUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FieldSortUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FieldSortUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FieldConditionUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FieldConditionUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_OperationResult_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_OperationResult_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_TransactionUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_TransactionUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_EntityUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_EntityUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_RelationUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_RelationUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ValueUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ValueUp_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FieldUp_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FieldUp_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016transfer.proto\"\217\001\n\021SelectByCondition\022\031" +
      "\n\006entity\030\001 \001(\0132\t.EntityUp\022\016\n\006pageNo\030\002 \001(" +
      "\005\022\020\n\010pageSize\030\003 \001(\005\022!\n\nconditions\030\004 \001(\0132" +
      "\r.ConditionsUp\022\032\n\004sort\030\005 \003(\0132\014.FieldSort" +
      "Up\"1\n\014ConditionsUp\022!\n\006fields\030\001 \003(\0132\021.Fie" +
      "ldConditionUp\"s\n\013FieldSortUp\022\014\n\004code\030\001 \001" +
      "(\t\022!\n\005order\030\002 \001(\0162\022.FieldSortUp.Order\022\027\n" +
      "\005field\030\003 \001(\0132\010.FieldUp\"\032\n\005Order\022\007\n\003asc\020\000" +
      "\022\010\n\004desc\020\001\"\336\001\n\020FieldConditionUp\022\014\n\004code\030" +
      "\001 \001(\t\022\'\n\toperation\030\002 \001(\0162\024.FieldConditio" +
      "nUp.Op\022\016\n\006values\030\003 \003(\t\022\027\n\005field\030\004 \001(\0132\010." +
      "FieldUp\"j\n\002Op\022\006\n\002eq\020\000\022\010\n\004like\020\001\022\006\n\002in\020\002\022" +
      "\t\n\005ge_le\020\003\022\t\n\005ge_lt\020\004\022\t\n\005gt_le\020\005\022\t\n\005gt_l" +
      "t\020\006\022\006\n\002gt\020\007\022\006\n\002ge\020\010\022\006\n\002lt\020\t\022\006\n\002le\020\n\"\375\001\n\017" +
      "OperationResult\022#\n\004code\030\001 \001(\0162\025.Operatio" +
      "nResult.Code\022\017\n\007message\030\002 \001(\t\022\036\n\013queryRe" +
      "sult\030\003 \003(\0132\t.EntityUp\022\031\n\021transactionResu" +
      "lt\030\004 \001(\t\022\023\n\013affectedRow\030\005 \001(\005\022\013\n\003ids\030\006 \003" +
      "(\003\022\020\n\010totalRow\030\007 \001(\005\"E\n\004Code\022\006\n\002OK\020\000\022\n\n\006" +
      "FAILED\020\001\022\r\n\tEXCEPTION\020\002\022\017\n\013NETWORK_ERR\020\003" +
      "\022\t\n\005OTHER\020\004\">\n\rTransactionUp\022\n\n\002id\030\001 \001(\t" +
      "\022\017\n\007service\030\002 \001(\t\022\020\n\010tansType\030\003 \001(\t\"\316\001\n\010" +
      "EntityUp\022\n\n\002id\030\001 \001(\003\022\035\n\010relation\030\002 \003(\0132\013" +
      ".RelationUp\022 \n\rentityClasses\030\003 \003(\0132\t.Ent" +
      "ityUp\022$\n\021extendEntityClass\030\004 \001(\0132\t.Entit" +
      "yUp\022\030\n\006fields\030\005 \003(\0132\010.FieldUp\022\030\n\006values\030" +
      "\006 \003(\0132\010.ValueUp\022\r\n\005objId\030\007 \001(\003\022\014\n\004code\030\010" +
      " \001(\t\"\177\n\nRelationUp\022\014\n\004name\030\001 \001(\t\022\024\n\014rela" +
      "tionType\030\002 \001(\t\022\020\n\010identity\030\003 \001(\010\022\035\n\013enti" +
      "tyField\030\004 \001(\0132\010.FieldUp\022\034\n\024relatedEntity" +
      "ClassId\030\005 \001(\003\"J\n\007ValueUp\022\r\n\005value\030\001 \001(\t\022" +
      "\014\n\004name\030\002 \001(\t\022\021\n\tfieldType\030\003 \001(\t\022\017\n\007fiel" +
      "dId\030\004 \001(\003\"\266\001\n\007FieldUp\022\n\n\002id\030\001 \001(\003\022\014\n\004nam" +
      "e\030\002 \001(\t\022\021\n\tfieldType\030\003 \001(\t\022\014\n\004code\030\004 \001(\t" +
      "\022\023\n\013displayType\030\005 \001(\t\022\020\n\010editable\030\006 \001(\t\022" +
      "\020\n\010enumCode\030\007 \001(\t\022\021\n\tmaxLength\030\010 \001(\t\022\020\n\010" +
      "required\030\t \001(\t\022\022\n\nsearchable\030\n \001(\t2\357\002\n\rE" +
      "ntityService\022)\n\005begin\022\016.TransactionUp\032\020." +
      "OperationResult\022$\n\005build\022\t.EntityUp\032\020.Op" +
      "erationResult\022&\n\007replace\022\t.EntityUp\032\020.Op" +
      "erationResult\022%\n\006remove\022\t.EntityUp\032\020.Ope" +
      "rationResult\022(\n\tselectOne\022\t.EntityUp\032\020.O" +
      "perationResult\022:\n\022selectByConditions\022\022.S" +
      "electByCondition\032\020.OperationResult\022*\n\006co" +
      "mmit\022\016.TransactionUp\032\020.OperationResult\022," +
      "\n\010rollBack\022\016.TransactionUp\032\020.OperationRe" +
      "sultB>\n%com.xforceplus.ultraman.oqsengin" +
      "e.sdkB\023EntityResourceProtoP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_SelectByCondition_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_SelectByCondition_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_SelectByCondition_descriptor,
        new java.lang.String[] { "Entity", "PageNo", "PageSize", "Conditions", "Sort", });
    internal_static_ConditionsUp_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_ConditionsUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ConditionsUp_descriptor,
        new java.lang.String[] { "Fields", });
    internal_static_FieldSortUp_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_FieldSortUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FieldSortUp_descriptor,
        new java.lang.String[] { "Code", "Order", "Field", });
    internal_static_FieldConditionUp_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_FieldConditionUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FieldConditionUp_descriptor,
        new java.lang.String[] { "Code", "Operation", "Values", "Field", });
    internal_static_OperationResult_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_OperationResult_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_OperationResult_descriptor,
        new java.lang.String[] { "Code", "Message", "QueryResult", "TransactionResult", "AffectedRow", "Ids", "TotalRow", });
    internal_static_TransactionUp_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_TransactionUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_TransactionUp_descriptor,
        new java.lang.String[] { "Id", "Service", "TansType", });
    internal_static_EntityUp_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_EntityUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_EntityUp_descriptor,
        new java.lang.String[] { "Id", "Relation", "EntityClasses", "ExtendEntityClass", "Fields", "Values", "ObjId", "Code", });
    internal_static_RelationUp_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_RelationUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_RelationUp_descriptor,
        new java.lang.String[] { "Name", "RelationType", "Identity", "EntityField", "RelatedEntityClassId", });
    internal_static_ValueUp_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_ValueUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ValueUp_descriptor,
        new java.lang.String[] { "Value", "Name", "FieldType", "FieldId", });
    internal_static_FieldUp_descriptor =
      getDescriptor().getMessageTypes().get(9);
    internal_static_FieldUp_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FieldUp_descriptor,
        new java.lang.String[] { "Id", "Name", "FieldType", "Code", "DisplayType", "Editable", "EnumCode", "MaxLength", "Required", "Searchable", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
