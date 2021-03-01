// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto;

/**
 * Protobuf type {@code RelationInfo}
 */
public  final class RelationInfo extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:RelationInfo)
    RelationInfoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RelationInfo.newBuilder() to construct.
  private RelationInfo(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RelationInfo() {
    id_ = 0L;
    name_ = "";
    entityClassId_ = 0L;
    relOwnerClassId_ = 0L;
    relOwnerClassName_ = "";
    relationType_ = "";
    identity_ = false;
    entityFieldCode_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private RelationInfo(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    int mutable_bitField0_ = 0;
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownFieldProto3(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 8: {

            id_ = input.readInt64();
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            name_ = s;
            break;
          }
          case 24: {

            entityClassId_ = input.readInt64();
            break;
          }
          case 32: {

            relOwnerClassId_ = input.readInt64();
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();

            relOwnerClassName_ = s;
            break;
          }
          case 50: {
            java.lang.String s = input.readStringRequireUtf8();

            relationType_ = s;
            break;
          }
          case 56: {

            identity_ = input.readBool();
            break;
          }
          case 66: {
            java.lang.String s = input.readStringRequireUtf8();

            entityFieldCode_ = s;
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncProto.internal_static_RelationInfo_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncProto.internal_static_RelationInfo_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.Builder.class);
  }

  public static final int ID_FIELD_NUMBER = 1;
  private long id_;
  /**
   * <code>int64 id = 1;</code>
   */
  public long getId() {
    return id_;
  }

  public static final int NAME_FIELD_NUMBER = 2;
  private volatile java.lang.Object name_;
  /**
   * <code>string name = 2;</code>
   */
  public java.lang.String getName() {
    java.lang.Object ref = name_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      name_ = s;
      return s;
    }
  }
  /**
   * <code>string name = 2;</code>
   */
  public com.google.protobuf.ByteString
      getNameBytes() {
    java.lang.Object ref = name_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      name_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ENTITYCLASSID_FIELD_NUMBER = 3;
  private long entityClassId_;
  /**
   * <code>int64 entityClassId = 3;</code>
   */
  public long getEntityClassId() {
    return entityClassId_;
  }

  public static final int RELOWNERCLASSID_FIELD_NUMBER = 4;
  private long relOwnerClassId_;
  /**
   * <code>int64 relOwnerClassId = 4;</code>
   */
  public long getRelOwnerClassId() {
    return relOwnerClassId_;
  }

  public static final int RELOWNERCLASSNAME_FIELD_NUMBER = 5;
  private volatile java.lang.Object relOwnerClassName_;
  /**
   * <code>string relOwnerClassName = 5;</code>
   */
  public java.lang.String getRelOwnerClassName() {
    java.lang.Object ref = relOwnerClassName_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      relOwnerClassName_ = s;
      return s;
    }
  }
  /**
   * <code>string relOwnerClassName = 5;</code>
   */
  public com.google.protobuf.ByteString
      getRelOwnerClassNameBytes() {
    java.lang.Object ref = relOwnerClassName_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      relOwnerClassName_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int RELATIONTYPE_FIELD_NUMBER = 6;
  private volatile java.lang.Object relationType_;
  /**
   * <code>string relationType = 6;</code>
   */
  public java.lang.String getRelationType() {
    java.lang.Object ref = relationType_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      relationType_ = s;
      return s;
    }
  }
  /**
   * <code>string relationType = 6;</code>
   */
  public com.google.protobuf.ByteString
      getRelationTypeBytes() {
    java.lang.Object ref = relationType_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      relationType_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int IDENTITY_FIELD_NUMBER = 7;
  private boolean identity_;
  /**
   * <code>bool identity = 7;</code>
   */
  public boolean getIdentity() {
    return identity_;
  }

  public static final int ENTITYFIELDCODE_FIELD_NUMBER = 8;
  private volatile java.lang.Object entityFieldCode_;
  /**
   * <code>string entityFieldCode = 8;</code>
   */
  public java.lang.String getEntityFieldCode() {
    java.lang.Object ref = entityFieldCode_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      entityFieldCode_ = s;
      return s;
    }
  }
  /**
   * <code>string entityFieldCode = 8;</code>
   */
  public com.google.protobuf.ByteString
      getEntityFieldCodeBytes() {
    java.lang.Object ref = entityFieldCode_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      entityFieldCode_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (id_ != 0L) {
      output.writeInt64(1, id_);
    }
    if (!getNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, name_);
    }
    if (entityClassId_ != 0L) {
      output.writeInt64(3, entityClassId_);
    }
    if (relOwnerClassId_ != 0L) {
      output.writeInt64(4, relOwnerClassId_);
    }
    if (!getRelOwnerClassNameBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, relOwnerClassName_);
    }
    if (!getRelationTypeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, relationType_);
    }
    if (identity_ != false) {
      output.writeBool(7, identity_);
    }
    if (!getEntityFieldCodeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 8, entityFieldCode_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (id_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(1, id_);
    }
    if (!getNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, name_);
    }
    if (entityClassId_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(3, entityClassId_);
    }
    if (relOwnerClassId_ != 0L) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt64Size(4, relOwnerClassId_);
    }
    if (!getRelOwnerClassNameBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, relOwnerClassName_);
    }
    if (!getRelationTypeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, relationType_);
    }
    if (identity_ != false) {
      size += com.google.protobuf.CodedOutputStream
        .computeBoolSize(7, identity_);
    }
    if (!getEntityFieldCodeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(8, entityFieldCode_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo other = (com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo) obj;

    boolean result = true;
    result = result && (getId()
        == other.getId());
    result = result && getName()
        .equals(other.getName());
    result = result && (getEntityClassId()
        == other.getEntityClassId());
    result = result && (getRelOwnerClassId()
        == other.getRelOwnerClassId());
    result = result && getRelOwnerClassName()
        .equals(other.getRelOwnerClassName());
    result = result && getRelationType()
        .equals(other.getRelationType());
    result = result && (getIdentity()
        == other.getIdentity());
    result = result && getEntityFieldCode()
        .equals(other.getEntityFieldCode());
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + ID_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getId());
    hash = (37 * hash) + NAME_FIELD_NUMBER;
    hash = (53 * hash) + getName().hashCode();
    hash = (37 * hash) + ENTITYCLASSID_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getEntityClassId());
    hash = (37 * hash) + RELOWNERCLASSID_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashLong(
        getRelOwnerClassId());
    hash = (37 * hash) + RELOWNERCLASSNAME_FIELD_NUMBER;
    hash = (53 * hash) + getRelOwnerClassName().hashCode();
    hash = (37 * hash) + RELATIONTYPE_FIELD_NUMBER;
    hash = (53 * hash) + getRelationType().hashCode();
    hash = (37 * hash) + IDENTITY_FIELD_NUMBER;
    hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
        getIdentity());
    hash = (37 * hash) + ENTITYFIELDCODE_FIELD_NUMBER;
    hash = (53 * hash) + getEntityFieldCode().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code RelationInfo}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:RelationInfo)
      com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncProto.internal_static_RelationInfo_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncProto.internal_static_RelationInfo_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    public Builder clear() {
      super.clear();
      id_ = 0L;

      name_ = "";

      entityClassId_ = 0L;

      relOwnerClassId_ = 0L;

      relOwnerClassName_ = "";

      relationType_ = "";

      identity_ = false;

      entityFieldCode_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncProto.internal_static_RelationInfo_descriptor;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.getDefaultInstance();
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo build() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo buildPartial() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo result = new com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo(this);
      result.id_ = id_;
      result.name_ = name_;
      result.entityClassId_ = entityClassId_;
      result.relOwnerClassId_ = relOwnerClassId_;
      result.relOwnerClassName_ = relOwnerClassName_;
      result.relationType_ = relationType_;
      result.identity_ = identity_;
      result.entityFieldCode_ = entityFieldCode_;
      onBuilt();
      return result;
    }

    public Builder clone() {
      return (Builder) super.clone();
    }
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.setField(field, value);
    }
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return (Builder) super.clearField(field);
    }
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return (Builder) super.clearOneof(oneof);
    }
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo other) {
      if (other == com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo.getDefaultInstance()) return this;
      if (other.getId() != 0L) {
        setId(other.getId());
      }
      if (!other.getName().isEmpty()) {
        name_ = other.name_;
        onChanged();
      }
      if (other.getEntityClassId() != 0L) {
        setEntityClassId(other.getEntityClassId());
      }
      if (other.getRelOwnerClassId() != 0L) {
        setRelOwnerClassId(other.getRelOwnerClassId());
      }
      if (!other.getRelOwnerClassName().isEmpty()) {
        relOwnerClassName_ = other.relOwnerClassName_;
        onChanged();
      }
      if (!other.getRelationType().isEmpty()) {
        relationType_ = other.relationType_;
        onChanged();
      }
      if (other.getIdentity() != false) {
        setIdentity(other.getIdentity());
      }
      if (!other.getEntityFieldCode().isEmpty()) {
        entityFieldCode_ = other.entityFieldCode_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    public final boolean isInitialized() {
      return true;
    }

    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private long id_ ;
    /**
     * <code>int64 id = 1;</code>
     */
    public long getId() {
      return id_;
    }
    /**
     * <code>int64 id = 1;</code>
     */
    public Builder setId(long value) {
      
      id_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 id = 1;</code>
     */
    public Builder clearId() {
      
      id_ = 0L;
      onChanged();
      return this;
    }

    private java.lang.Object name_ = "";
    /**
     * <code>string name = 2;</code>
     */
    public java.lang.String getName() {
      java.lang.Object ref = name_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        name_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string name = 2;</code>
     */
    public com.google.protobuf.ByteString
        getNameBytes() {
      java.lang.Object ref = name_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        name_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string name = 2;</code>
     */
    public Builder setName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      name_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string name = 2;</code>
     */
    public Builder clearName() {
      
      name_ = getDefaultInstance().getName();
      onChanged();
      return this;
    }
    /**
     * <code>string name = 2;</code>
     */
    public Builder setNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      name_ = value;
      onChanged();
      return this;
    }

    private long entityClassId_ ;
    /**
     * <code>int64 entityClassId = 3;</code>
     */
    public long getEntityClassId() {
      return entityClassId_;
    }
    /**
     * <code>int64 entityClassId = 3;</code>
     */
    public Builder setEntityClassId(long value) {
      
      entityClassId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 entityClassId = 3;</code>
     */
    public Builder clearEntityClassId() {
      
      entityClassId_ = 0L;
      onChanged();
      return this;
    }

    private long relOwnerClassId_ ;
    /**
     * <code>int64 relOwnerClassId = 4;</code>
     */
    public long getRelOwnerClassId() {
      return relOwnerClassId_;
    }
    /**
     * <code>int64 relOwnerClassId = 4;</code>
     */
    public Builder setRelOwnerClassId(long value) {
      
      relOwnerClassId_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int64 relOwnerClassId = 4;</code>
     */
    public Builder clearRelOwnerClassId() {
      
      relOwnerClassId_ = 0L;
      onChanged();
      return this;
    }

    private java.lang.Object relOwnerClassName_ = "";
    /**
     * <code>string relOwnerClassName = 5;</code>
     */
    public java.lang.String getRelOwnerClassName() {
      java.lang.Object ref = relOwnerClassName_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        relOwnerClassName_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string relOwnerClassName = 5;</code>
     */
    public com.google.protobuf.ByteString
        getRelOwnerClassNameBytes() {
      java.lang.Object ref = relOwnerClassName_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        relOwnerClassName_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string relOwnerClassName = 5;</code>
     */
    public Builder setRelOwnerClassName(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      relOwnerClassName_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string relOwnerClassName = 5;</code>
     */
    public Builder clearRelOwnerClassName() {
      
      relOwnerClassName_ = getDefaultInstance().getRelOwnerClassName();
      onChanged();
      return this;
    }
    /**
     * <code>string relOwnerClassName = 5;</code>
     */
    public Builder setRelOwnerClassNameBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      relOwnerClassName_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object relationType_ = "";
    /**
     * <code>string relationType = 6;</code>
     */
    public java.lang.String getRelationType() {
      java.lang.Object ref = relationType_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        relationType_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string relationType = 6;</code>
     */
    public com.google.protobuf.ByteString
        getRelationTypeBytes() {
      java.lang.Object ref = relationType_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        relationType_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string relationType = 6;</code>
     */
    public Builder setRelationType(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      relationType_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string relationType = 6;</code>
     */
    public Builder clearRelationType() {
      
      relationType_ = getDefaultInstance().getRelationType();
      onChanged();
      return this;
    }
    /**
     * <code>string relationType = 6;</code>
     */
    public Builder setRelationTypeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      relationType_ = value;
      onChanged();
      return this;
    }

    private boolean identity_ ;
    /**
     * <code>bool identity = 7;</code>
     */
    public boolean getIdentity() {
      return identity_;
    }
    /**
     * <code>bool identity = 7;</code>
     */
    public Builder setIdentity(boolean value) {
      
      identity_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bool identity = 7;</code>
     */
    public Builder clearIdentity() {
      
      identity_ = false;
      onChanged();
      return this;
    }

    private java.lang.Object entityFieldCode_ = "";
    /**
     * <code>string entityFieldCode = 8;</code>
     */
    public java.lang.String getEntityFieldCode() {
      java.lang.Object ref = entityFieldCode_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        entityFieldCode_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string entityFieldCode = 8;</code>
     */
    public com.google.protobuf.ByteString
        getEntityFieldCodeBytes() {
      java.lang.Object ref = entityFieldCode_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        entityFieldCode_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string entityFieldCode = 8;</code>
     */
    public Builder setEntityFieldCode(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      entityFieldCode_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string entityFieldCode = 8;</code>
     */
    public Builder clearEntityFieldCode() {
      
      entityFieldCode_ = getDefaultInstance().getEntityFieldCode();
      onChanged();
      return this;
    }
    /**
     * <code>string entityFieldCode = 8;</code>
     */
    public Builder setEntityFieldCodeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      entityFieldCode_ = value;
      onChanged();
      return this;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:RelationInfo)
  }

  // @@protoc_insertion_point(class_scope:RelationInfo)
  private static final com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo();
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RelationInfo>
      PARSER = new com.google.protobuf.AbstractParser<RelationInfo>() {
    public RelationInfo parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new RelationInfo(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<RelationInfo> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RelationInfo> getParserForType() {
    return PARSER;
  }

  public com.xforceplus.ultraman.oqsengine.meta.common.proto.RelationInfo getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

