// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ForBo.proto

package com.xforceplus.ultraman.oqsengine.meta.proto.test;

/**
 * Protobuf type {@code ModuleUp}
 */
public  final class ModuleUp extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:ModuleUp)
    ModuleUpOrBuilder {
private static final long serialVersionUID = 0L;
  // Use ModuleUp.newBuilder() to construct.
  private ModuleUp(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private ModuleUp() {
    moduleId_ = "";
    moduleVersion_ = "";
    authorization_ = java.util.Collections.emptyList();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private ModuleUp(
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
            java.lang.String s = input.readStringRequireUtf8();

            moduleId_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            moduleVersion_ = s;
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              authorization_ = new java.util.ArrayList<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization>();
              mutable_bitField0_ |= 0x00000004;
            }
            authorization_.add(
                input.readMessage(com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.parser(), extensionRegistry));
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
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        authorization_ = java.util.Collections.unmodifiableList(authorization_);
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_ModuleUp_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_ModuleUp_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.class, com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.Builder.class);
  }

  private int bitField0_;
  public static final int MODULEID_FIELD_NUMBER = 1;
  private volatile java.lang.Object moduleId_;
  /**
   * <pre>
   * 传入的模块id.
   * </pre>
   *
   * <code>string moduleId = 1;</code>
   */
  public java.lang.String getModuleId() {
    java.lang.Object ref = moduleId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      moduleId_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * 传入的模块id.
   * </pre>
   *
   * <code>string moduleId = 1;</code>
   */
  public com.google.protobuf.ByteString
      getModuleIdBytes() {
    java.lang.Object ref = moduleId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      moduleId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MODULEVERSION_FIELD_NUMBER = 2;
  private volatile java.lang.Object moduleVersion_;
  /**
   * <pre>
   * 模块版本.
   * </pre>
   *
   * <code>string moduleVersion = 2;</code>
   */
  public java.lang.String getModuleVersion() {
    java.lang.Object ref = moduleVersion_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      moduleVersion_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * 模块版本.
   * </pre>
   *
   * <code>string moduleVersion = 2;</code>
   */
  public com.google.protobuf.ByteString
      getModuleVersionBytes() {
    java.lang.Object ref = moduleVersion_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      moduleVersion_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int AUTHORIZATION_FIELD_NUMBER = 3;
  private java.util.List<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization> authorization_;
  /**
   * <pre>
   * 当前授权信息.
   * </pre>
   *
   * <code>repeated .Authorization authorization = 3;</code>
   */
  public java.util.List<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization> getAuthorizationList() {
    return authorization_;
  }
  /**
   * <pre>
   * 当前授权信息.
   * </pre>
   *
   * <code>repeated .Authorization authorization = 3;</code>
   */
  public java.util.List<? extends com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder> 
      getAuthorizationOrBuilderList() {
    return authorization_;
  }
  /**
   * <pre>
   * 当前授权信息.
   * </pre>
   *
   * <code>repeated .Authorization authorization = 3;</code>
   */
  public int getAuthorizationCount() {
    return authorization_.size();
  }
  /**
   * <pre>
   * 当前授权信息.
   * </pre>
   *
   * <code>repeated .Authorization authorization = 3;</code>
   */
  public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization getAuthorization(int index) {
    return authorization_.get(index);
  }
  /**
   * <pre>
   * 当前授权信息.
   * </pre>
   *
   * <code>repeated .Authorization authorization = 3;</code>
   */
  public com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder getAuthorizationOrBuilder(
      int index) {
    return authorization_.get(index);
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
    if (!getModuleIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, moduleId_);
    }
    if (!getModuleVersionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, moduleVersion_);
    }
    for (int i = 0; i < authorization_.size(); i++) {
      output.writeMessage(3, authorization_.get(i));
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getModuleIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, moduleId_);
    }
    if (!getModuleVersionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, moduleVersion_);
    }
    for (int i = 0; i < authorization_.size(); i++) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, authorization_.get(i));
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
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp other = (com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp) obj;

    boolean result = true;
    result = result && getModuleId()
        .equals(other.getModuleId());
    result = result && getModuleVersion()
        .equals(other.getModuleVersion());
    result = result && getAuthorizationList()
        .equals(other.getAuthorizationList());
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
    hash = (37 * hash) + MODULEID_FIELD_NUMBER;
    hash = (53 * hash) + getModuleId().hashCode();
    hash = (37 * hash) + MODULEVERSION_FIELD_NUMBER;
    hash = (53 * hash) + getModuleVersion().hashCode();
    if (getAuthorizationCount() > 0) {
      hash = (37 * hash) + AUTHORIZATION_FIELD_NUMBER;
      hash = (53 * hash) + getAuthorizationList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parseFrom(
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
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp prototype) {
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
   * Protobuf type {@code ModuleUp}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:ModuleUp)
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUpOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_ModuleUp_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_ModuleUp_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.class, com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.newBuilder()
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
        getAuthorizationFieldBuilder();
      }
    }
    public Builder clear() {
      super.clear();
      moduleId_ = "";

      moduleVersion_ = "";

      if (authorizationBuilder_ == null) {
        authorization_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
      } else {
        authorizationBuilder_.clear();
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_ModuleUp_descriptor;
    }

    public com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.getDefaultInstance();
    }

    public com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp build() {
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp buildPartial() {
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp result = new com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.moduleId_ = moduleId_;
      result.moduleVersion_ = moduleVersion_;
      if (authorizationBuilder_ == null) {
        if (((bitField0_ & 0x00000004) == 0x00000004)) {
          authorization_ = java.util.Collections.unmodifiableList(authorization_);
          bitField0_ = (bitField0_ & ~0x00000004);
        }
        result.authorization_ = authorization_;
      } else {
        result.authorization_ = authorizationBuilder_.build();
      }
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
      if (other instanceof com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp other) {
      if (other == com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp.getDefaultInstance()) return this;
      if (!other.getModuleId().isEmpty()) {
        moduleId_ = other.moduleId_;
        onChanged();
      }
      if (!other.getModuleVersion().isEmpty()) {
        moduleVersion_ = other.moduleVersion_;
        onChanged();
      }
      if (authorizationBuilder_ == null) {
        if (!other.authorization_.isEmpty()) {
          if (authorization_.isEmpty()) {
            authorization_ = other.authorization_;
            bitField0_ = (bitField0_ & ~0x00000004);
          } else {
            ensureAuthorizationIsMutable();
            authorization_.addAll(other.authorization_);
          }
          onChanged();
        }
      } else {
        if (!other.authorization_.isEmpty()) {
          if (authorizationBuilder_.isEmpty()) {
            authorizationBuilder_.dispose();
            authorizationBuilder_ = null;
            authorization_ = other.authorization_;
            bitField0_ = (bitField0_ & ~0x00000004);
            authorizationBuilder_ = 
              com.google.protobuf.GeneratedMessageV3.alwaysUseFieldBuilders ?
                 getAuthorizationFieldBuilder() : null;
          } else {
            authorizationBuilder_.addAllMessages(other.authorization_);
          }
        }
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
      com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private java.lang.Object moduleId_ = "";
    /**
     * <pre>
     * 传入的模块id.
     * </pre>
     *
     * <code>string moduleId = 1;</code>
     */
    public java.lang.String getModuleId() {
      java.lang.Object ref = moduleId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        moduleId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * 传入的模块id.
     * </pre>
     *
     * <code>string moduleId = 1;</code>
     */
    public com.google.protobuf.ByteString
        getModuleIdBytes() {
      java.lang.Object ref = moduleId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        moduleId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * 传入的模块id.
     * </pre>
     *
     * <code>string moduleId = 1;</code>
     */
    public Builder setModuleId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      moduleId_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 传入的模块id.
     * </pre>
     *
     * <code>string moduleId = 1;</code>
     */
    public Builder clearModuleId() {
      
      moduleId_ = getDefaultInstance().getModuleId();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 传入的模块id.
     * </pre>
     *
     * <code>string moduleId = 1;</code>
     */
    public Builder setModuleIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      moduleId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object moduleVersion_ = "";
    /**
     * <pre>
     * 模块版本.
     * </pre>
     *
     * <code>string moduleVersion = 2;</code>
     */
    public java.lang.String getModuleVersion() {
      java.lang.Object ref = moduleVersion_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        moduleVersion_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * 模块版本.
     * </pre>
     *
     * <code>string moduleVersion = 2;</code>
     */
    public com.google.protobuf.ByteString
        getModuleVersionBytes() {
      java.lang.Object ref = moduleVersion_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        moduleVersion_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * 模块版本.
     * </pre>
     *
     * <code>string moduleVersion = 2;</code>
     */
    public Builder setModuleVersion(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      moduleVersion_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 模块版本.
     * </pre>
     *
     * <code>string moduleVersion = 2;</code>
     */
    public Builder clearModuleVersion() {
      
      moduleVersion_ = getDefaultInstance().getModuleVersion();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 模块版本.
     * </pre>
     *
     * <code>string moduleVersion = 2;</code>
     */
    public Builder setModuleVersionBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      moduleVersion_ = value;
      onChanged();
      return this;
    }

    private java.util.List<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization> authorization_ =
      java.util.Collections.emptyList();
    private void ensureAuthorizationIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        authorization_ = new java.util.ArrayList<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization>(authorization_);
        bitField0_ |= 0x00000004;
       }
    }

    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder, com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder> authorizationBuilder_;

    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public java.util.List<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization> getAuthorizationList() {
      if (authorizationBuilder_ == null) {
        return java.util.Collections.unmodifiableList(authorization_);
      } else {
        return authorizationBuilder_.getMessageList();
      }
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public int getAuthorizationCount() {
      if (authorizationBuilder_ == null) {
        return authorization_.size();
      } else {
        return authorizationBuilder_.getCount();
      }
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization getAuthorization(int index) {
      if (authorizationBuilder_ == null) {
        return authorization_.get(index);
      } else {
        return authorizationBuilder_.getMessage(index);
      }
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder setAuthorization(
        int index, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization value) {
      if (authorizationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAuthorizationIsMutable();
        authorization_.set(index, value);
        onChanged();
      } else {
        authorizationBuilder_.setMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder setAuthorization(
        int index, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder builderForValue) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.set(index, builderForValue.build());
        onChanged();
      } else {
        authorizationBuilder_.setMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder addAuthorization(com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization value) {
      if (authorizationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAuthorizationIsMutable();
        authorization_.add(value);
        onChanged();
      } else {
        authorizationBuilder_.addMessage(value);
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder addAuthorization(
        int index, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization value) {
      if (authorizationBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        ensureAuthorizationIsMutable();
        authorization_.add(index, value);
        onChanged();
      } else {
        authorizationBuilder_.addMessage(index, value);
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder addAuthorization(
        com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder builderForValue) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.add(builderForValue.build());
        onChanged();
      } else {
        authorizationBuilder_.addMessage(builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder addAuthorization(
        int index, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder builderForValue) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.add(index, builderForValue.build());
        onChanged();
      } else {
        authorizationBuilder_.addMessage(index, builderForValue.build());
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder addAllAuthorization(
        java.lang.Iterable<? extends com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization> values) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        com.google.protobuf.AbstractMessageLite.Builder.addAll(
            values, authorization_);
        onChanged();
      } else {
        authorizationBuilder_.addAllMessages(values);
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder clearAuthorization() {
      if (authorizationBuilder_ == null) {
        authorization_ = java.util.Collections.emptyList();
        bitField0_ = (bitField0_ & ~0x00000004);
        onChanged();
      } else {
        authorizationBuilder_.clear();
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public Builder removeAuthorization(int index) {
      if (authorizationBuilder_ == null) {
        ensureAuthorizationIsMutable();
        authorization_.remove(index);
        onChanged();
      } else {
        authorizationBuilder_.remove(index);
      }
      return this;
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder getAuthorizationBuilder(
        int index) {
      return getAuthorizationFieldBuilder().getBuilder(index);
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder getAuthorizationOrBuilder(
        int index) {
      if (authorizationBuilder_ == null) {
        return authorization_.get(index);  } else {
        return authorizationBuilder_.getMessageOrBuilder(index);
      }
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public java.util.List<? extends com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder> 
         getAuthorizationOrBuilderList() {
      if (authorizationBuilder_ != null) {
        return authorizationBuilder_.getMessageOrBuilderList();
      } else {
        return java.util.Collections.unmodifiableList(authorization_);
      }
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder addAuthorizationBuilder() {
      return getAuthorizationFieldBuilder().addBuilder(
          com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.getDefaultInstance());
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder addAuthorizationBuilder(
        int index) {
      return getAuthorizationFieldBuilder().addBuilder(
          index, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.getDefaultInstance());
    }
    /**
     * <pre>
     * 当前授权信息.
     * </pre>
     *
     * <code>repeated .Authorization authorization = 3;</code>
     */
    public java.util.List<com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder> 
         getAuthorizationBuilderList() {
      return getAuthorizationFieldBuilder().getBuilderList();
    }
    private com.google.protobuf.RepeatedFieldBuilderV3<
        com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder, com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder> 
        getAuthorizationFieldBuilder() {
      if (authorizationBuilder_ == null) {
        authorizationBuilder_ = new com.google.protobuf.RepeatedFieldBuilderV3<
            com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder, com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder>(
                authorization_,
                ((bitField0_ & 0x00000004) == 0x00000004),
                getParentForChildren(),
                isClean());
        authorization_ = null;
      }
      return authorizationBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:ModuleUp)
  }

  // @@protoc_insertion_point(class_scope:ModuleUp)
  private static final com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp();
  }

  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<ModuleUp>
      PARSER = new com.google.protobuf.AbstractParser<ModuleUp>() {
    public ModuleUp parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new ModuleUp(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<ModuleUp> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<ModuleUp> getParserForType() {
    return PARSER;
  }

  public com.xforceplus.ultraman.oqsengine.meta.proto.test.ModuleUp getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

