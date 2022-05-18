// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

/**
 * Protobuf type {@code EntityClassSyncRspProto}
 */
public  final class EntityClassSyncRspProto extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:EntityClassSyncRspProto)
    EntityClassSyncRspProtoOrBuilder {
private static final long serialVersionUID = 0L;
  // Use EntityClassSyncRspProto.newBuilder() to construct.
  private EntityClassSyncRspProto(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private EntityClassSyncRspProto() {
    entityClasses_ = java.util.Collections.emptyList();
    appCode_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private EntityClassSyncRspProto(
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
          case 10: {
            if (!((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
              entityClasses_ = new java.util.ArrayList<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo>();
              mutable_bitField0_ |= 0x00000001;
            }
            entityClasses_.add(
                input.readMessage(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.parser(), extensionRegistry));
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            appCode_ = s;
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
      if (((mutable_bitField0_ & 0x00000001) == 0x00000001)) {
        entityClasses_ = java.util.Collections.unmodifiableList(entityClasses_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncRspProto_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncRspProto_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder.class);
  }

  private int bitField0_;
  public static final int ENTITYCLASSES_FIELD_NUMBER = 1;
  private java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo> entityClasses_;
  /**
   * <code>repeated .EntityClassInfo entityClasses = 1;</code>
   */
  public java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo> getEntityClassesList() {
    return entityClasses_;
  }
  /**
   * <code>repeated .EntityClassInfo entityClasses = 1;</code>
   */
  public java.util.List<? extends com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder> 
      getEntityClassesOrBuilderList() {
    return entityClasses_;
  }
  /**
   * <code>repeated .EntityClassInfo entityClasses = 1;</code>
   */
  public int getEntityClassesCount() {
    return entityClasses_.size();
  }
  /**
   * <code>repeated .EntityClassInfo entityClasses = 1;</code>
   */
  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo getEntityClasses(int index) {
    return entityClasses_.get(index);
  }
  /**
   * <code>repeated .EntityClassInfo entityClasses = 1;</code>
   */
  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder getEntityClassesOrBuilder(
      int index) {
    return entityClasses_.get(index);
  }

  public static final int APPCODE_FIELD_NUMBER = 2;
  private volatile java.lang.Object appCode_;
  /**
   * <code>string appCode = 2;</code>
   */
  public java.lang.String getAppCode() {
    java.lang.Object ref = appCode_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      appCode_ = s;
      return s;
    }
  }
  /**
   * <code>string appCode = 2;</code>
   */
  public com.google.protobuf.ByteString
      getAppCodeBytes() {
    java.lang.Object ref = appCode_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      appCode_ = b;
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
    for (int i = 0; i < entityClasses_.size(); i++) {
      output.writeMessage(1, entityClasses_.get(i));
    }
    if (!getAppCodeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, appCode_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    for (int i = 0; i < entityClasses_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(1, entityClasses_.get(i));
    }
    if (!getAppCodeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, appCode_);
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
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto other = (com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto) obj;

    boolean result = true;
    result = result && getEntityClassesList()
        .equals(other.getEntityClassesList());
    result = result && getAppCode()
        .equals(other.getAppCode());
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
    if (getEntityClassesCount() > 0) {
      hash = (37 * hash) + ENTITYCLASSES_FIELD_NUMBER;
      hash = (53 * hash) + getEntityClassesList().hashCode();
    }
    hash = (37 * hash) + APPCODE_FIELD_NUMBER;
    hash = (53 * hash) + getAppCode().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parseFrom(
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
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto prototype) {
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
   * Protobuf type {@code EntityClassSyncRspProto}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:EntityClassSyncRspProto)
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncRspProto_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncRspProto_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.newBuilder()
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
        getEntityClassesFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      if (entityClassesBuilder_ == null) {
        entityClasses_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
      } else {
        entityClassesBuilder_.clear();
      }
      appCode_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncRspProto_descriptor;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.getDefaultInstance();
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto build() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto buildPartial() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto result = new com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (entityClassesBuilder_ == null) {
        if (((bitField0_ & 0x00000001) == 0x00000001)) {
          entityClasses_ = java.util.Collections.unmodifiableList(entityClasses_);
          bitField0_ = (bitField0_ & ~0x00000001);
        }
        result.entityClasses_ = entityClasses_;
      } else {
        result.entityClasses_ = entityClassesBuilder_.build();
      }
      result.appCode_ = appCode_;
      result.bitField0_ = to_bitField0_;
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
      if (other instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto other) {
      if (other == com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.getDefaultInstance()) return this;
      if (entityClassesBuilder_ == null) {
        if (!other.entityClasses_.isEmpty()) {
          if (entityClasses_.isEmpty()) {
            entityClasses_ = other.entityClasses_;
            bitField0_ = (bitField0_ & ~0x00000001);
          } else {
            ensureEntityClassesIsMutable();
            entityClasses_.addAll(other.entityClasses_);
          }
          onChanged();
        }
      } else {
        if (!other.entityClasses_.isEmpty()) {
          if (entityClassesBuilder_.isEmpty()) {
            entityClassesBuilder_.dispose();
            entityClassesBuilder_ = null;
            entityClasses_ = other.entityClasses_;
            bitField0_ = (bitField0_ & ~0x00000001);
            entityClassesBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getEntityClassesFieldBuilder() : null;
          } else {
            entityClassesBuilder_.addAllMessages(other.entityClasses_);
          }
        }
      }
      if (!other.getAppCode().isEmpty()) {
        appCode_ = other.appCode_;
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
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo> entityClasses_ =
      java.util.Collections.emptyList();
    private void ensureEntityClassesIsMutable() {
      if (!((bitField0_ & 0x00000001) == 0x00000001)) {
        entityClasses_ = new java.util.ArrayList<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo>(entityClasses_);
        bitField0_ |= 0x00000001;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder> entityClassesBuilder_;

    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo> getEntityClassesList() {
      if (entityClassesBuilder_ == null) {
        return java.util.Collections.unmodifiableList(entityClasses_);
      } else {
        return entityClassesBuilder_.getMessageList();
      }
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public int getEntityClassesCount() {
      if (entityClassesBuilder_ == null) {
        return entityClasses_.size();
      } else {
        return entityClassesBuilder_.getCount();
      }
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo getEntityClasses(int index) {
      if (entityClassesBuilder_ == null) {
        return entityClasses_.get(index);
      } else {
        return entityClassesBuilder_.getMessage(index);
      }
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder setEntityClasses(
        int index, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo value) {
      if (entityClassesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureEntityClassesIsMutable();
        entityClasses_.set(index, value);
        onChanged();
      } else {
        entityClassesBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder setEntityClasses(
        int index, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder builderForValue) {
      if (entityClassesBuilder_ == null) {
        ensureEntityClassesIsMutable();
        entityClasses_.set(index, builderForValue.build());
        onChanged();
      } else {
        entityClassesBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder addEntityClasses(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo value) {
      if (entityClassesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureEntityClassesIsMutable();
        entityClasses_.add(value);
        onChanged();
      } else {
        entityClassesBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder addEntityClasses(
        int index, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo value) {
      if (entityClassesBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureEntityClassesIsMutable();
        entityClasses_.add(index, value);
        onChanged();
      } else {
        entityClassesBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder addEntityClasses(
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder builderForValue) {
      if (entityClassesBuilder_ == null) {
        ensureEntityClassesIsMutable();
        entityClasses_.add(builderForValue.build());
        onChanged();
      } else {
        entityClassesBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder addEntityClasses(
        int index, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder builderForValue) {
      if (entityClassesBuilder_ == null) {
        ensureEntityClassesIsMutable();
        entityClasses_.add(index, builderForValue.build());
        onChanged();
      } else {
        entityClassesBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder addAllEntityClasses(
        java.lang.Iterable<? extends com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo> values) {
      if (entityClassesBuilder_ == null) {
        ensureEntityClassesIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, entityClasses_);
        onChanged();
      } else {
        entityClassesBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder clearEntityClasses() {
      if (entityClassesBuilder_ == null) {
        entityClasses_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000001);
        onChanged();
      } else {
        entityClassesBuilder_.clear();
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public Builder removeEntityClasses(int index) {
      if (entityClassesBuilder_ == null) {
        ensureEntityClassesIsMutable();
        entityClasses_.remove(index);
        onChanged();
      } else {
        entityClassesBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder getEntityClassesBuilder(
        int index) {
      return getEntityClassesFieldBuilder().getBuilder(index);
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder getEntityClassesOrBuilder(
        int index) {
      if (entityClassesBuilder_ == null) {
        return entityClasses_.get(index);  } else {
        return entityClassesBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public java.util.List<? extends com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder> 
         getEntityClassesOrBuilderList() {
      if (entityClassesBuilder_ != null) {
        return entityClassesBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(entityClasses_);
      }
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder addEntityClassesBuilder() {
      return getEntityClassesFieldBuilder().addBuilder(
          com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.getDefaultInstance());
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder addEntityClassesBuilder(
        int index) {
      return getEntityClassesFieldBuilder().addBuilder(
          index, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.getDefaultInstance());
    }
    /**
     * <code>repeated .EntityClassInfo entityClasses = 1;</code>
     */
    public java.util.List<com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder> 
         getEntityClassesBuilderList() {
      return getEntityClassesFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder> 
        getEntityClassesFieldBuilder() {
      if (entityClassesBuilder_ == null) {
        entityClassesBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfo.Builder, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassInfoOrBuilder>(
                entityClasses_,
                ((bitField0_ & 0x00000001) == 0x00000001),
                getParentForChildren(),
                isClean());
        entityClasses_ = null;
      }
      return entityClassesBuilder_;
    }

    private java.lang.Object appCode_ = "";
    /**
     * <code>string appCode = 2;</code>
     */
    public java.lang.String getAppCode() {
      java.lang.Object ref = appCode_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        appCode_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string appCode = 2;</code>
     */
    public com.google.protobuf.ByteString
        getAppCodeBytes() {
      java.lang.Object ref = appCode_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        appCode_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string appCode = 2;</code>
     */
    public Builder setAppCode(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      appCode_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string appCode = 2;</code>
     */
    public Builder clearAppCode() {
      
      appCode_ = getDefaultInstance().getAppCode();
      onChanged();
      return this;
    }
    /**
     * <code>string appCode = 2;</code>
     */
    public Builder setAppCodeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      appCode_ = value;
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


    // @@protoc_insertion_point(builder_scope:EntityClassSyncRspProto)
  }

  // @@protoc_insertion_point(class_scope:EntityClassSyncRspProto)
  private static final com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto();
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<EntityClassSyncRspProto>
      PARSER = new com.google.protobuf.AbstractParser<EntityClassSyncRspProto>() {
    public EntityClassSyncRspProto parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new EntityClassSyncRspProto(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<EntityClassSyncRspProto> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<EntityClassSyncRspProto> getParserForType() {
    return PARSER;
  }

  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

