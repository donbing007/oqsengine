// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ForBo.proto

package com.xforceplus.ultraman.oqsengine.meta.proto.test;

/**
 * Protobuf type {@code Authorization}
 */
public  final class Authorization extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Authorization)
    AuthorizationOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Authorization.newBuilder() to construct.
  private Authorization(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Authorization() {
    tenantId_ = "";
    appId_ = "";
    role_ = "";
    env_ = "";
    branch_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Authorization(
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

            tenantId_ = s;
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            appId_ = s;
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            role_ = s;
            break;
          }
          case 34: {
            java.lang.String s = input.readStringRequireUtf8();

            env_ = s;
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();

            branch_ = s;
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
    return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_Authorization_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_Authorization_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.class, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder.class);
  }

  public static final int TENANTID_FIELD_NUMBER = 1;
  private volatile java.lang.Object tenantId_;
  /**
   * <pre>
   * 租户标识
   * </pre>
   *
   * <code>string tenantId = 1;</code>
   */
  public java.lang.String getTenantId() {
    java.lang.Object ref = tenantId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      tenantId_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * 租户标识
   * </pre>
   *
   * <code>string tenantId = 1;</code>
   */
  public com.google.protobuf.ByteString
      getTenantIdBytes() {
    java.lang.Object ref = tenantId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      tenantId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int APPID_FIELD_NUMBER = 2;
  private volatile java.lang.Object appId_;
  /**
   * <pre>
   *应用标识
   * </pre>
   *
   * <code>string appId = 2;</code>
   */
  public java.lang.String getAppId() {
    java.lang.Object ref = appId_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      appId_ = s;
      return s;
    }
  }
  /**
   * <pre>
   *应用标识
   * </pre>
   *
   * <code>string appId = 2;</code>
   */
  public com.google.protobuf.ByteString
      getAppIdBytes() {
    java.lang.Object ref = appId_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      appId_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ROLE_FIELD_NUMBER = 3;
  private volatile java.lang.Object role_;
  /**
   * <pre>
   * 角色标识
   * </pre>
   *
   * <code>string role = 3;</code>
   */
  public java.lang.String getRole() {
    java.lang.Object ref = role_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      role_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * 角色标识
   * </pre>
   *
   * <code>string role = 3;</code>
   */
  public com.google.protobuf.ByteString
      getRoleBytes() {
    java.lang.Object ref = role_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      role_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ENV_FIELD_NUMBER = 4;
  private volatile java.lang.Object env_;
  /**
   * <pre>
   *环境标识
   * </pre>
   *
   * <code>string env = 4;</code>
   */
  public java.lang.String getEnv() {
    java.lang.Object ref = env_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      env_ = s;
      return s;
    }
  }
  /**
   * <pre>
   *环境标识
   * </pre>
   *
   * <code>string env = 4;</code>
   */
  public com.google.protobuf.ByteString
      getEnvBytes() {
    java.lang.Object ref = env_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      env_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int BRANCH_FIELD_NUMBER = 5;
  private volatile java.lang.Object branch_;
  /**
   * <pre>
   *分支标识
   * </pre>
   *
   * <code>string branch = 5;</code>
   */
  public java.lang.String getBranch() {
    java.lang.Object ref = branch_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      branch_ = s;
      return s;
    }
  }
  /**
   * <pre>
   *分支标识
   * </pre>
   *
   * <code>string branch = 5;</code>
   */
  public com.google.protobuf.ByteString
      getBranchBytes() {
    java.lang.Object ref = branch_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      branch_ = b;
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
    if (!getTenantIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, tenantId_);
    }
    if (!getAppIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, appId_);
    }
    if (!getRoleBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, role_);
    }
    if (!getEnvBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, env_);
    }
    if (!getBranchBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, branch_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getTenantIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, tenantId_);
    }
    if (!getAppIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, appId_);
    }
    if (!getRoleBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, role_);
    }
    if (!getEnvBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, env_);
    }
    if (!getBranchBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, branch_);
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
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization other = (com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization) obj;

    boolean result = true;
    result = result && getTenantId()
        .equals(other.getTenantId());
    result = result && getAppId()
        .equals(other.getAppId());
    result = result && getRole()
        .equals(other.getRole());
    result = result && getEnv()
        .equals(other.getEnv());
    result = result && getBranch()
        .equals(other.getBranch());
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
    hash = (37 * hash) + TENANTID_FIELD_NUMBER;
    hash = (53 * hash) + getTenantId().hashCode();
    hash = (37 * hash) + APPID_FIELD_NUMBER;
    hash = (53 * hash) + getAppId().hashCode();
    hash = (37 * hash) + ROLE_FIELD_NUMBER;
    hash = (53 * hash) + getRole().hashCode();
    hash = (37 * hash) + ENV_FIELD_NUMBER;
    hash = (53 * hash) + getEnv().hashCode();
    hash = (37 * hash) + BRANCH_FIELD_NUMBER;
    hash = (53 * hash) + getBranch().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parseFrom(
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
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization prototype) {
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
   * Protobuf type {@code Authorization}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Authorization)
      com.xforceplus.ultraman.oqsengine.meta.proto.test.AuthorizationOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_Authorization_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_Authorization_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.class, com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.newBuilder()
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
      tenantId_ = "";

      appId_ = "";

      role_ = "";

      env_ = "";

      branch_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.MetadataResourceProto.internal_static_Authorization_descriptor;
    }

    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.getDefaultInstance();
    }

    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization build() {
      com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization buildPartial() {
      com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization result = new com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization(this);
      result.tenantId_ = tenantId_;
      result.appId_ = appId_;
      result.role_ = role_;
      result.env_ = env_;
      result.branch_ = branch_;
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
      if (other instanceof com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization other) {
      if (other == com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization.getDefaultInstance()) return this;
      if (!other.getTenantId().isEmpty()) {
        tenantId_ = other.tenantId_;
        onChanged();
      }
      if (!other.getAppId().isEmpty()) {
        appId_ = other.appId_;
        onChanged();
      }
      if (!other.getRole().isEmpty()) {
        role_ = other.role_;
        onChanged();
      }
      if (!other.getEnv().isEmpty()) {
        env_ = other.env_;
        onChanged();
      }
      if (!other.getBranch().isEmpty()) {
        branch_ = other.branch_;
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
      com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object tenantId_ = "";
    /**
     * <pre>
     * 租户标识
     * </pre>
     *
     * <code>string tenantId = 1;</code>
     */
    public java.lang.String getTenantId() {
      java.lang.Object ref = tenantId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        tenantId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * 租户标识
     * </pre>
     *
     * <code>string tenantId = 1;</code>
     */
    public com.google.protobuf.ByteString
        getTenantIdBytes() {
      java.lang.Object ref = tenantId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        tenantId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * 租户标识
     * </pre>
     *
     * <code>string tenantId = 1;</code>
     */
    public Builder setTenantId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      tenantId_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 租户标识
     * </pre>
     *
     * <code>string tenantId = 1;</code>
     */
    public Builder clearTenantId() {
      
      tenantId_ = getDefaultInstance().getTenantId();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 租户标识
     * </pre>
     *
     * <code>string tenantId = 1;</code>
     */
    public Builder setTenantIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      tenantId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object appId_ = "";
    /**
     * <pre>
     *应用标识
     * </pre>
     *
     * <code>string appId = 2;</code>
     */
    public java.lang.String getAppId() {
      java.lang.Object ref = appId_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        appId_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     *应用标识
     * </pre>
     *
     * <code>string appId = 2;</code>
     */
    public com.google.protobuf.ByteString
        getAppIdBytes() {
      java.lang.Object ref = appId_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        appId_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     *应用标识
     * </pre>
     *
     * <code>string appId = 2;</code>
     */
    public Builder setAppId(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      appId_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *应用标识
     * </pre>
     *
     * <code>string appId = 2;</code>
     */
    public Builder clearAppId() {
      
      appId_ = getDefaultInstance().getAppId();
      onChanged();
      return this;
    }
    /**
     * <pre>
     *应用标识
     * </pre>
     *
     * <code>string appId = 2;</code>
     */
    public Builder setAppIdBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      appId_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object role_ = "";
    /**
     * <pre>
     * 角色标识
     * </pre>
     *
     * <code>string role = 3;</code>
     */
    public java.lang.String getRole() {
      java.lang.Object ref = role_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        role_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * 角色标识
     * </pre>
     *
     * <code>string role = 3;</code>
     */
    public com.google.protobuf.ByteString
        getRoleBytes() {
      java.lang.Object ref = role_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        role_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * 角色标识
     * </pre>
     *
     * <code>string role = 3;</code>
     */
    public Builder setRole(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      role_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 角色标识
     * </pre>
     *
     * <code>string role = 3;</code>
     */
    public Builder clearRole() {
      
      role_ = getDefaultInstance().getRole();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * 角色标识
     * </pre>
     *
     * <code>string role = 3;</code>
     */
    public Builder setRoleBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      role_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object env_ = "";
    /**
     * <pre>
     *环境标识
     * </pre>
     *
     * <code>string env = 4;</code>
     */
    public java.lang.String getEnv() {
      java.lang.Object ref = env_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        env_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     *环境标识
     * </pre>
     *
     * <code>string env = 4;</code>
     */
    public com.google.protobuf.ByteString
        getEnvBytes() {
      java.lang.Object ref = env_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        env_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     *环境标识
     * </pre>
     *
     * <code>string env = 4;</code>
     */
    public Builder setEnv(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      env_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *环境标识
     * </pre>
     *
     * <code>string env = 4;</code>
     */
    public Builder clearEnv() {
      
      env_ = getDefaultInstance().getEnv();
      onChanged();
      return this;
    }
    /**
     * <pre>
     *环境标识
     * </pre>
     *
     * <code>string env = 4;</code>
     */
    public Builder setEnvBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      env_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object branch_ = "";
    /**
     * <pre>
     *分支标识
     * </pre>
     *
     * <code>string branch = 5;</code>
     */
    public java.lang.String getBranch() {
      java.lang.Object ref = branch_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        branch_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     *分支标识
     * </pre>
     *
     * <code>string branch = 5;</code>
     */
    public com.google.protobuf.ByteString
        getBranchBytes() {
      java.lang.Object ref = branch_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        branch_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     *分支标识
     * </pre>
     *
     * <code>string branch = 5;</code>
     */
    public Builder setBranch(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      branch_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     *分支标识
     * </pre>
     *
     * <code>string branch = 5;</code>
     */
    public Builder clearBranch() {
      
      branch_ = getDefaultInstance().getBranch();
      onChanged();
      return this;
    }
    /**
     * <pre>
     *分支标识
     * </pre>
     *
     * <code>string branch = 5;</code>
     */
    public Builder setBranchBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      branch_ = value;
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


    // @@protoc_insertion_point(builder_scope:Authorization)
  }

  // @@protoc_insertion_point(class_scope:Authorization)
  private static final com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization();
  }

  public static com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Authorization>
      PARSER = new com.google.protobuf.AbstractParser<Authorization>() {
    public Authorization parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Authorization(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Authorization> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Authorization> getParserForType() {
    return PARSER;
  }

  public com.xforceplus.ultraman.oqsengine.meta.proto.test.Authorization getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

