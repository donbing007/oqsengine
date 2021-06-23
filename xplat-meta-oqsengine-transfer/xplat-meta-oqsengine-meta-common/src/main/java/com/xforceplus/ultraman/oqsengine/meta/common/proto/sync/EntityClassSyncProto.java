// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

public final class EntityClassSyncProto {
  private EntityClassSyncProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_EntityClassSyncRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_EntityClassSyncRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_EntityClassSyncResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_EntityClassSyncResponse_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_EntityClassSyncRspProto_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_EntityClassSyncRspProto_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_EntityClassInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_EntityClassInfo_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_EntityFieldInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_EntityFieldInfo_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_FieldConfig_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_FieldConfig_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_Calculator_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_Calculator_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_ProfileInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_ProfileInfo_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_RelationInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_RelationInfo_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\nsync.proto\032\031google/protobuf/any.proto\"" +
      "q\n\026EntityClassSyncRequest\022\013\n\003uid\030\001 \001(\t\022\r" +
      "\n\005appId\030\002 \001(\t\022\017\n\007version\030\003 \001(\005\022\016\n\006status" +
      "\030\004 \001(\005\022\013\n\003env\030\005 \001(\t\022\r\n\005force\030\006 \001(\010\"\272\001\n\027E" +
      "ntityClassSyncResponse\022\r\n\005appId\030\001 \001(\t\022\017\n" +
      "\007version\030\002 \001(\005\022\013\n\003uid\030\003 \001(\t\022\016\n\006status\030\004 " +
      "\001(\005\022\013\n\003env\030\005 \001(\t\022\013\n\003md5\030\006 \001(\t\0229\n\027entityC" +
      "lassSyncRspProto\030\007 \001(\0132\030.EntityClassSync" +
      "RspProto\022\r\n\005force\030\010 \001(\010\"B\n\027EntityClassSy" +
          "ncRspProto\022'\n\rentityClasses\030\001 \003(\0132\020.Enti" +
      "tyClassInfo\"\323\001\n\017EntityClassInfo\022\014\n\004code\030" +
      "\001 \001(\t\022\n\n\002id\030\002 \001(\003\022\014\n\004name\030\003 \001(\t\022\016\n\006fathe" +
      "r\030\004 \001(\003\022\r\n\005level\030\005 \001(\005\022\017\n\007version\030\006 \001(\005\022" +
      "&\n\014entityFields\030\007 \003(\0132\020.EntityFieldInfo\022" +
      " \n\trelations\030\010 \003(\0132\r.RelationInfo\022\036\n\010pro" +
      "files\030\t \003(\0132\014.ProfileInfo\"\302\002\n\017EntityFiel" +
      "dInfo\022\n\n\002id\030\001 \001(\003\022\014\n\004name\030\002 \001(\t\022\r\n\005cname" +
      "\030\003 \001(\t\022-\n\tfieldType\030\004 \001(\0162\032.EntityFieldI" +
      "nfo.FieldType\022\016\n\006dictId\030\005 \001(\t\022\024\n\014default" +
      "Value\030\006 \001(\t\022!\n\013fieldConfig\030\007 \001(\0132\014.Field" +
      "Config\022\037\n\ncalculator\030\t \001(\0132\013.Calculator\"" +
      "m\n\tFieldType\022\013\n\007UNKNOWN\020\000\022\013\n\007BOOLEAN\020\001\022\010" +
      "\n\004ENUM\020\002\022\014\n\010DATETIME\020\003\022\010\n\004LONG\020\004\022\n\n\006STRI" +
      "NG\020\005\022\013\n\007STRINGS\020\006\022\013\n\007DECIMAL\020\007\"\260\004\n\013Field" +
      "Config\022\022\n\nsearchable\030\001 \001(\010\022\013\n\003max\030\002 \001(\003\022" +
      "\013\n\003min\030\003 \001(\003\022\021\n\tprecision\030\004 \001(\005\022\022\n\nident" +
      "ifier\030\005 \001(\010\022\022\n\nisRequired\030\006 \001(\010\022\033\n\023valid" +
      "ateRegexString\030\007 \001(\t\022\023\n\013displayType\030\010 \001(" +
      "\t\0223\n\016metaFieldSense\030\t \001(\0162\033.FieldConfig." +
      "MetaFieldSense\022\021\n\tfuzzyType\030\n \001(\005\022\030\n\020wil" +
      "dcardMinWidth\030\013 \001(\005\022\030\n\020wildcardMaxWidth\030" +
      "\014 \001(\005\022\022\n\nuniqueName\030\r \001(\t\022\023\n\013crossSearch" +
      "\030\016 \001(\010\022\016\n\006length\030\017 \001(\005\"\320\001\n\016MetaFieldSens" +
      "e\022\013\n\007UNKNOWN\020\000\022\n\n\006NORMAL\020\001\022\r\n\tTENANT_ID\020" +
      "\002\022\017\n\013TENANT_CODE\020\003\022\017\n\013CREATE_TIME\020\004\022\017\n\013U" +
      "PDATE_TIME\020\005\022\022\n\016CREATE_USER_ID\020\006\022\022\n\016UPDA" +
      "TE_USER_ID\020\007\022\024\n\020CREATE_USER_NAME\020\010\022\024\n\020UP" +
      "DATE_USER_NAME\020\t\022\017\n\013DELETE_FLAG\020\n\"\245\002\n\nCa" +
      "lculator\022\025\n\rcalculateType\030\001 \001(\005\022\022\n\nexpre" +
      "ssion\030\002 \001(\t\022\021\n\tvalidator\030\003 \001(\t\022\013\n\003min\030\004 " +
      "\001(\t\022\013\n\003max\030\005 \001(\t\022\021\n\tcondition\030\006 \001(\t\022\032\n\022e" +
      "mptyValueTransfer\030\007 \001(\t\022\016\n\006patten\030\010 \001(\t\022" +
      "\r\n\005model\030\t \001(\t\022\014\n\004step\030\n \001(\005\022\r\n\005level\030\013 " +
      "\001(\005\022\014\n\004args\030\014 \003(\t\022\024\n\014failedPolicy\030\r \001(\005\022" +
      "0\n\022failedDefaultValue\030\016 \001(\0132\024.google.pro" +
      "tobuf.Any\"k\n\013ProfileInfo\022\014\n\004code\030\001 \001(\t\022)" +
      "\n\017entityFieldInfo\030\002 \003(\0132\020.EntityFieldInf" +
      "o\022#\n\014relationInfo\030\003 \003(\0132\r.RelationInfo\"\362" +
      "\001\n\014RelationInfo\022\n\n\002id\030\001 \001(\003\022\014\n\004code\030\002 \001(" +
      "\t\022\032\n\022rightEntityClassId\030\003 \001(\003\022\031\n\021leftEnt" +
      "ityClassId\030\004 \001(\003\022\033\n\023leftEntityClassCode\030" +
      "\005 \001(\t\022\024\n\014relationType\030\006 \001(\005\022\020\n\010identity\030" +
      "\007 \001(\010\022%\n\013entityField\030\010 \001(\0132\020.EntityField" +
      "Info\022\025\n\rbelongToOwner\030\t \001(\010\022\016\n\006strong\030\n " +
      "\001(\0102V\n\017EntityClassSync\022C\n\010register\022\027.Ent" +
      "ityClassSyncRequest\032\030.EntityClassSyncRes" +
      "ponse\"\000(\0010\001BR\n8com.xforceplus.ultraman.o" +
      "qsengine.meta.common.proto.syncB\024EntityC" +
      "lassSyncProtoP\001b\006proto3"
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
          com.google.protobuf.AnyProto.getDescriptor(),
        }, assigner);
    internal_static_EntityClassSyncRequest_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_EntityClassSyncRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_EntityClassSyncRequest_descriptor,
        new java.lang.String[] { "Uid", "AppId", "Version", "Status", "Env", "Force", });
    internal_static_EntityClassSyncResponse_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_EntityClassSyncResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_EntityClassSyncResponse_descriptor,
        new java.lang.String[] { "AppId", "Version", "Uid", "Status", "Env", "Md5", "EntityClassSyncRspProto", "Force", });
    internal_static_EntityClassSyncRspProto_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_EntityClassSyncRspProto_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_EntityClassSyncRspProto_descriptor,
        new java.lang.String[] { "EntityClasses", });
    internal_static_EntityClassInfo_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_EntityClassInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_EntityClassInfo_descriptor,
        new java.lang.String[] { "Code", "Id", "Name", "Father", "Level", "Version", "EntityFields", "Relations", "Profiles", });
    internal_static_EntityFieldInfo_descriptor =
      getDescriptor().getMessageTypes().get(4);
    internal_static_EntityFieldInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_EntityFieldInfo_descriptor,
        new java.lang.String[] { "Id", "Name", "Cname", "FieldType", "DictId", "DefaultValue", "FieldConfig", "Calculator", });
    internal_static_FieldConfig_descriptor =
      getDescriptor().getMessageTypes().get(5);
    internal_static_FieldConfig_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_FieldConfig_descriptor,
        new java.lang.String[] { "Searchable", "Max", "Min", "Precision", "Identifier", "IsRequired", "ValidateRegexString", "DisplayType", "MetaFieldSense", "FuzzyType", "WildcardMinWidth", "WildcardMaxWidth", "UniqueName", "CrossSearch", "Length", });
    internal_static_Calculator_descriptor =
      getDescriptor().getMessageTypes().get(6);
    internal_static_Calculator_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_Calculator_descriptor,
        new java.lang.String[] { "CalculateType", "Expression", "Validator", "Min", "Max", "Condition", "EmptyValueTransfer", "Patten", "Model", "Step", "Level", "Args", "FailedPolicy", "FailedDefaultValue", });
    internal_static_ProfileInfo_descriptor =
      getDescriptor().getMessageTypes().get(7);
    internal_static_ProfileInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_ProfileInfo_descriptor,
        new java.lang.String[] { "Code", "EntityFieldInfo", "RelationInfo", });
    internal_static_RelationInfo_descriptor =
      getDescriptor().getMessageTypes().get(8);
    internal_static_RelationInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_RelationInfo_descriptor,
        new java.lang.String[] { "Id", "Code", "RightEntityClassId", "LeftEntityClassId", "LeftEntityClassCode", "RelationType", "Identity", "EntityField", "BelongToOwner", "Strong", });
    com.google.protobuf.AnyProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
