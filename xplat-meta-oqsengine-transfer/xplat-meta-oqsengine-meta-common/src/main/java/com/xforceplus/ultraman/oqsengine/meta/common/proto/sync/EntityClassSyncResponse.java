// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

/**
 * Protobuf type {@code EntityClassSyncResponse}
 */
public  final class EntityClassSyncResponse extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:EntityClassSyncResponse)
    EntityClassSyncResponseOrBuilder {
private static final long serialVersionUID = 0L;
  // Use EntityClassSyncResponse.newBuilder() to construct.
  private EntityClassSyncResponse(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private EntityClassSyncResponse() {
    appId_ = "";
    version_ = 0;
    uid_ = "";
    status_ = 0;
    env_ = "";
    md5_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private EntityClassSyncResponse(
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

            appId_ = s;
            break;
          }
          case 16: {

            version_ = input.readInt32();
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            uid_ = s;
            break;
          }
          case 32: {

            status_ = input.readInt32();
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();

            env_ = s;
            break;
          }
          case 50: {
            java.lang.String s = input.readStringRequireUtf8();

            md5_ = s;
            break;
          }
          case 58: {
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder subBuilder = null;
            if (entityClassSyncRspProto_ != null) {
              subBuilder = entityClassSyncRspProto_.toBuilder();
            }
            entityClassSyncRspProto_ = input.readMessage(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(entityClassSyncRspProto_);
              entityClassSyncRspProto_ = subBuilder.buildPartial();
            }

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
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncResponse_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncResponse_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.Builder.class);
  }

  public static final int APPID_FIELD_NUMBER = 1;
  private volatile java.lang.Object appId_;
  /**
   * <code>string appId = 1;</code>
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
   * <code>string appId = 1;</code>
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

  public static final int VERSION_FIELD_NUMBER = 2;
  private int version_;
  /**
   * <code>int32 version = 2;</code>
   */
  public int getVersion() {
    return version_;
  }

  public static final int UID_FIELD_NUMBER = 3;
  private volatile java.lang.Object uid_;
  /**
   * <code>string uid = 3;</code>
   */
  public java.lang.String getUid() {
    java.lang.Object ref = uid_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      uid_ = s;
      return s;
    }
  }
  /**
   * <code>string uid = 3;</code>
   */
  public com.google.protobuf.ByteString
      getUidBytes() {
    java.lang.Object ref = uid_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      uid_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int STATUS_FIELD_NUMBER = 4;
  private int status_;
  /**
   * <code>int32 status = 4;</code>
   */
  public int getStatus() {
    return status_;
  }

  public static final int ENV_FIELD_NUMBER = 5;
  private volatile java.lang.Object env_;
  /**
   * <code>string env = 5;</code>
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
   * <code>string env = 5;</code>
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

  public static final int MD5_FIELD_NUMBER = 6;
  private volatile java.lang.Object md5_;
  /**
   * <code>string md5 = 6;</code>
   */
  public java.lang.String getMd5() {
    java.lang.Object ref = md5_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      md5_ = s;
      return s;
    }
  }
  /**
   * <code>string md5 = 6;</code>
   */
  public com.google.protobuf.ByteString
      getMd5Bytes() {
    java.lang.Object ref = md5_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      md5_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int ENTITYCLASSSYNCRSPPROTO_FIELD_NUMBER = 7;
  private com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto entityClassSyncRspProto_;
  /**
   * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
   */
  public boolean hasEntityClassSyncRspProto() {
    return entityClassSyncRspProto_ != null;
  }
  /**
   * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
   */
  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto getEntityClassSyncRspProto() {
    return entityClassSyncRspProto_ == null ? com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.getDefaultInstance() : entityClassSyncRspProto_;
  }
  /**
   * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
   */
  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder getEntityClassSyncRspProtoOrBuilder() {
    return getEntityClassSyncRspProto();
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
    if (!getAppIdBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, appId_);
    }
    if (version_ != 0) {
      output.writeInt32(2, version_);
    }
    if (!getUidBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, uid_);
    }
    if (status_ != 0) {
      output.writeInt32(4, status_);
    }
    if (!getEnvBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, env_);
    }
    if (!getMd5Bytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, md5_);
    }
    if (entityClassSyncRspProto_ != null) {
      output.writeMessage(7, getEntityClassSyncRspProto());
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getAppIdBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, appId_);
    }
    if (version_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, version_);
    }
    if (!getUidBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, uid_);
    }
    if (status_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(4, status_);
    }
    if (!getEnvBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, env_);
    }
    if (!getMd5Bytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, md5_);
    }
    if (entityClassSyncRspProto_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(7, getEntityClassSyncRspProto());
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
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse other = (com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse) obj;

    boolean result = true;
    result = result && getAppId()
        .equals(other.getAppId());
    result = result && (getVersion()
        == other.getVersion());
    result = result && getUid()
        .equals(other.getUid());
    result = result && (getStatus()
        == other.getStatus());
    result = result && getEnv()
        .equals(other.getEnv());
    result = result && getMd5()
        .equals(other.getMd5());
    result = result && (hasEntityClassSyncRspProto() == other.hasEntityClassSyncRspProto());
    if (hasEntityClassSyncRspProto()) {
      result = result && getEntityClassSyncRspProto()
          .equals(other.getEntityClassSyncRspProto());
    }
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
    hash = (37 * hash) + APPID_FIELD_NUMBER;
    hash = (53 * hash) + getAppId().hashCode();
    hash = (37 * hash) + VERSION_FIELD_NUMBER;
    hash = (53 * hash) + getVersion();
    hash = (37 * hash) + UID_FIELD_NUMBER;
    hash = (53 * hash) + getUid().hashCode();
    hash = (37 * hash) + STATUS_FIELD_NUMBER;
    hash = (53 * hash) + getStatus();
    hash = (37 * hash) + ENV_FIELD_NUMBER;
    hash = (53 * hash) + getEnv().hashCode();
    hash = (37 * hash) + MD5_FIELD_NUMBER;
    hash = (53 * hash) + getMd5().hashCode();
    if (hasEntityClassSyncRspProto()) {
      hash = (37 * hash) + ENTITYCLASSSYNCRSPPROTO_FIELD_NUMBER;
      hash = (53 * hash) + getEntityClassSyncRspProto().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parseFrom(
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
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse prototype) {
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
   * Protobuf type {@code EntityClassSyncResponse}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:EntityClassSyncResponse)
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponseOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncResponse_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncResponse_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.newBuilder()
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
      appId_ = "";

      version_ = 0;

      uid_ = "";

      status_ = 0;

      env_ = "";

      md5_ = "";

      if (entityClassSyncRspProtoBuilder_ == null) {
        entityClassSyncRspProto_ = null;
      } else {
        entityClassSyncRspProto_ = null;
        entityClassSyncRspProtoBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_EntityClassSyncResponse_descriptor;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.getDefaultInstance();
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse build() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse buildPartial() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse result = new com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse(this);
      result.appId_ = appId_;
      result.version_ = version_;
      result.uid_ = uid_;
      result.status_ = status_;
      result.env_ = env_;
      result.md5_ = md5_;
      if (entityClassSyncRspProtoBuilder_ == null) {
        result.entityClassSyncRspProto_ = entityClassSyncRspProto_;
      } else {
        result.entityClassSyncRspProto_ = entityClassSyncRspProtoBuilder_.build();
      }
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
      if (other instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse other) {
      if (other == com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse.getDefaultInstance()) return this;
      if (!other.getAppId().isEmpty()) {
        appId_ = other.appId_;
        onChanged();
      }
      if (other.getVersion() != 0) {
        setVersion(other.getVersion());
      }
      if (!other.getUid().isEmpty()) {
        uid_ = other.uid_;
        onChanged();
      }
      if (other.getStatus() != 0) {
        setStatus(other.getStatus());
      }
      if (!other.getEnv().isEmpty()) {
        env_ = other.env_;
        onChanged();
      }
      if (!other.getMd5().isEmpty()) {
        md5_ = other.md5_;
        onChanged();
      }
      if (other.hasEntityClassSyncRspProto()) {
        mergeEntityClassSyncRspProto(other.getEntityClassSyncRspProto());
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
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object appId_ = "";
    /**
     * <code>string appId = 1;</code>
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
     * <code>string appId = 1;</code>
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
     * <code>string appId = 1;</code>
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
     * <code>string appId = 1;</code>
     */
    public Builder clearAppId() {
      
      appId_ = getDefaultInstance().getAppId();
      onChanged();
      return this;
    }
    /**
     * <code>string appId = 1;</code>
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

    private int version_ ;
    /**
     * <code>int32 version = 2;</code>
     */
    public int getVersion() {
      return version_;
    }
    /**
     * <code>int32 version = 2;</code>
     */
    public Builder setVersion(int value) {
      
      version_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 version = 2;</code>
     */
    public Builder clearVersion() {
      
      version_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object uid_ = "";
    /**
     * <code>string uid = 3;</code>
     */
    public java.lang.String getUid() {
      java.lang.Object ref = uid_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        uid_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string uid = 3;</code>
     */
    public com.google.protobuf.ByteString
        getUidBytes() {
      java.lang.Object ref = uid_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        uid_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string uid = 3;</code>
     */
    public Builder setUid(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      uid_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string uid = 3;</code>
     */
    public Builder clearUid() {
      
      uid_ = getDefaultInstance().getUid();
      onChanged();
      return this;
    }
    /**
     * <code>string uid = 3;</code>
     */
    public Builder setUidBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      uid_ = value;
      onChanged();
      return this;
    }

    private int status_ ;
    /**
     * <code>int32 status = 4;</code>
     */
    public int getStatus() {
      return status_;
    }
    /**
     * <code>int32 status = 4;</code>
     */
    public Builder setStatus(int value) {
      
      status_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 status = 4;</code>
     */
    public Builder clearStatus() {
      
      status_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object env_ = "";
    /**
     * <code>string env = 5;</code>
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
     * <code>string env = 5;</code>
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
     * <code>string env = 5;</code>
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
     * <code>string env = 5;</code>
     */
    public Builder clearEnv() {
      
      env_ = getDefaultInstance().getEnv();
      onChanged();
      return this;
    }
    /**
     * <code>string env = 5;</code>
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

    private java.lang.Object md5_ = "";
    /**
     * <code>string md5 = 6;</code>
     */
    public java.lang.String getMd5() {
      java.lang.Object ref = md5_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        md5_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string md5 = 6;</code>
     */
    public com.google.protobuf.ByteString
        getMd5Bytes() {
      java.lang.Object ref = md5_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        md5_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string md5 = 6;</code>
     */
    public Builder setMd5(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      md5_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string md5 = 6;</code>
     */
    public Builder clearMd5() {
      
      md5_ = getDefaultInstance().getMd5();
      onChanged();
      return this;
    }
    /**
     * <code>string md5 = 6;</code>
     */
    public Builder setMd5Bytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      md5_ = value;
      onChanged();
      return this;
    }

    private com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto entityClassSyncRspProto_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder> entityClassSyncRspProtoBuilder_;
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public boolean hasEntityClassSyncRspProto() {
      return entityClassSyncRspProtoBuilder_ != null || entityClassSyncRspProto_ != null;
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto getEntityClassSyncRspProto() {
      if (entityClassSyncRspProtoBuilder_ == null) {
        return entityClassSyncRspProto_ == null ? com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.getDefaultInstance() : entityClassSyncRspProto_;
      } else {
        return entityClassSyncRspProtoBuilder_.getMessage();
      }
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public Builder setEntityClassSyncRspProto(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto value) {
      if (entityClassSyncRspProtoBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        entityClassSyncRspProto_ = value;
        onChanged();
      } else {
        entityClassSyncRspProtoBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public Builder setEntityClassSyncRspProto(
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder builderForValue) {
      if (entityClassSyncRspProtoBuilder_ == null) {
        entityClassSyncRspProto_ = builderForValue.build();
        onChanged();
      } else {
        entityClassSyncRspProtoBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public Builder mergeEntityClassSyncRspProto(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto value) {
      if (entityClassSyncRspProtoBuilder_ == null) {
        if (entityClassSyncRspProto_ != null) {
          entityClassSyncRspProto_ =
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.newBuilder(entityClassSyncRspProto_).mergeFrom(value).buildPartial();
        } else {
          entityClassSyncRspProto_ = value;
        }
        onChanged();
      } else {
        entityClassSyncRspProtoBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public Builder clearEntityClassSyncRspProto() {
      if (entityClassSyncRspProtoBuilder_ == null) {
        entityClassSyncRspProto_ = null;
        onChanged();
      } else {
        entityClassSyncRspProto_ = null;
        entityClassSyncRspProtoBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder getEntityClassSyncRspProtoBuilder() {
      
      onChanged();
      return getEntityClassSyncRspProtoFieldBuilder().getBuilder();
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder getEntityClassSyncRspProtoOrBuilder() {
      if (entityClassSyncRspProtoBuilder_ != null) {
        return entityClassSyncRspProtoBuilder_.getMessageOrBuilder();
      } else {
        return entityClassSyncRspProto_ == null ?
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.getDefaultInstance() : entityClassSyncRspProto_;
      }
    }
    /**
     * <code>.EntityClassSyncRspProto entityClassSyncRspProto = 7;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder> 
        getEntityClassSyncRspProtoFieldBuilder() {
      if (entityClassSyncRspProtoBuilder_ == null) {
        entityClassSyncRspProtoBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProto.Builder, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncRspProtoOrBuilder>(
                getEntityClassSyncRspProto(),
                getParentForChildren(),
                isClean());
        entityClassSyncRspProto_ = null;
      }
      return entityClassSyncRspProtoBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:EntityClassSyncResponse)
  }

  // @@protoc_insertion_point(class_scope:EntityClassSyncResponse)
  private static final com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse();
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<EntityClassSyncResponse>
      PARSER = new com.google.protobuf.AbstractParser<EntityClassSyncResponse>() {
    public EntityClassSyncResponse parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new EntityClassSyncResponse(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<EntityClassSyncResponse> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<EntityClassSyncResponse> getParserForType() {
    return PARSER;
  }

  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncResponse getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
