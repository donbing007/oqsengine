// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: rebuild.proto

package com.xforceplus.ultraman.oqsengine.sdk;

/**
 * Protobuf type {@code RepairRequest}
 */
public  final class RepairRequest extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:RepairRequest)
    RepairRequestOrBuilder {
private static final long serialVersionUID = 0L;
  // Use RepairRequest.newBuilder() to construct.
  private RepairRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private RepairRequest() {
    rid_ = emptyLongList();
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new RepairRequest();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private RepairRequest(
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
          case 8: {
            if (!((mutable_bitField0_ & 0x00000001) != 0)) {
              rid_ = newLongList();
              mutable_bitField0_ |= 0x00000001;
            }
            rid_.addLong(input.readInt64());
            break;
          }
          case 10: {
            int length = input.readRawVarint32();
            int limit = input.pushLimit(length);
            if (!((mutable_bitField0_ & 0x00000001) != 0) && input.getBytesUntilLimit() > 0) {
              rid_ = newLongList();
              mutable_bitField0_ |= 0x00000001;
            }
            while (input.getBytesUntilLimit() > 0) {
              rid_.addLong(input.readInt64());
            }
            input.popLimit(limit);
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
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
      if (((mutable_bitField0_ & 0x00000001) != 0)) {
        rid_.makeImmutable(); // C
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildResourceProto.internal_static_RepairRequest_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildResourceProto.internal_static_RepairRequest_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.class, com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.Builder.class);
  }

  public static final int RID_FIELD_NUMBER = 1;
  private com.google.protobuf.Internal.LongList rid_;
  /**
   * <code>repeated int64 rid = 1;</code>
   * @return A list containing the rid.
   */
  public java.util.List<java.lang.Long>
      getRidList() {
    return rid_;
  }
  /**
   * <code>repeated int64 rid = 1;</code>
   * @return The count of rid.
   */
  public int getRidCount() {
    return rid_.size();
  }
  /**
   * <code>repeated int64 rid = 1;</code>
   * @param index The index of the element to return.
   * @return The rid at the given index.
   */
  public long getRid(int index) {
    return rid_.getLong(index);
  }
  private int ridMemoizedSerializedSize = -1;

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    if (getRidList().size() > 0) {
      output.writeUInt32NoTag(10);
      output.writeUInt32NoTag(ridMemoizedSerializedSize);
    }
    for (int i = 0; i < rid_.size(); i++) {
      output.writeInt64NoTag(rid_.getLong(i));
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    {
      int dataSize = 0;
      for (int i = 0; i < rid_.size(); i++) {
        dataSize += com.google.protobuf.CodedOutputStream
          .computeInt64SizeNoTag(rid_.getLong(i));
      }
      size += dataSize;
      if (!getRidList().isEmpty()) {
        size += 1;
        size += com.google.protobuf.CodedOutputStream
            .computeInt32SizeNoTag(dataSize);
      }
      ridMemoizedSerializedSize = dataSize;
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
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.sdk.RepairRequest)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.sdk.RepairRequest other = (com.xforceplus.ultraman.oqsengine.sdk.RepairRequest) obj;

    if (!getRidList()
        .equals(other.getRidList())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    if (getRidCount() > 0) {
      hash = (37 * hash) + RID_FIELD_NUMBER;
      hash = (53 * hash) + getRidList().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
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
   * Protobuf type {@code RepairRequest}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:RepairRequest)
      com.xforceplus.ultraman.oqsengine.sdk.RepairRequestOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildResourceProto.internal_static_RepairRequest_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildResourceProto.internal_static_RepairRequest_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.class, com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.newBuilder()
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
    @java.lang.Override
    public Builder clear() {
      super.clear();
      rid_ = emptyLongList();
      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildResourceProto.internal_static_RepairRequest_descriptor;
    }

    @java.lang.Override
    public com.xforceplus.ultraman.oqsengine.sdk.RepairRequest getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.getDefaultInstance();
    }

    @java.lang.Override
    public com.xforceplus.ultraman.oqsengine.sdk.RepairRequest build() {
      com.xforceplus.ultraman.oqsengine.sdk.RepairRequest result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public com.xforceplus.ultraman.oqsengine.sdk.RepairRequest buildPartial() {
      com.xforceplus.ultraman.oqsengine.sdk.RepairRequest result = new com.xforceplus.ultraman.oqsengine.sdk.RepairRequest(this);
      int from_bitField0_ = bitField0_;
      if (((bitField0_ & 0x00000001) != 0)) {
        rid_.makeImmutable();
        bitField0_ = (bitField0_ & ~0x00000001);
      }
      result.rid_ = rid_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof com.xforceplus.ultraman.oqsengine.sdk.RepairRequest) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.sdk.RepairRequest)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest other) {
      if (other == com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.getDefaultInstance()) return this;
      if (!other.rid_.isEmpty()) {
        if (rid_.isEmpty()) {
          rid_ = other.rid_;
          bitField0_ = (bitField0_ & ~0x00000001);
        } else {
          ensureRidIsMutable();
          rid_.addAll(other.rid_);
        }
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      com.xforceplus.ultraman.oqsengine.sdk.RepairRequest parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.sdk.RepairRequest) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private com.google.protobuf.Internal.LongList rid_ = emptyLongList();
    private void ensureRidIsMutable() {
      if (!((bitField0_ & 0x00000001) != 0)) {
        rid_ = mutableCopy(rid_);
        bitField0_ |= 0x00000001;
       }
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @return A list containing the rid.
     */
    public java.util.List<java.lang.Long>
        getRidList() {
      return ((bitField0_ & 0x00000001) != 0) ?
               java.util.Collections.unmodifiableList(rid_) : rid_;
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @return The count of rid.
     */
    public int getRidCount() {
      return rid_.size();
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @param index The index of the element to return.
     * @return The rid at the given index.
     */
    public long getRid(int index) {
      return rid_.getLong(index);
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @param index The index to set the value at.
     * @param value The rid to set.
     * @return This builder for chaining.
     */
    public Builder setRid(
        int index, long value) {
      ensureRidIsMutable();
      rid_.setLong(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @param value The rid to add.
     * @return This builder for chaining.
     */
    public Builder addRid(long value) {
      ensureRidIsMutable();
      rid_.addLong(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @param values The rid to add.
     * @return This builder for chaining.
     */
    public Builder addAllRid(
        java.lang.Iterable<? extends java.lang.Long> values) {
      ensureRidIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, rid_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated int64 rid = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearRid() {
      rid_ = emptyLongList();
      bitField0_ = (bitField0_ & ~0x00000001);
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:RepairRequest)
  }

  // @@protoc_insertion_point(class_scope:RepairRequest)
  private static final com.xforceplus.ultraman.oqsengine.sdk.RepairRequest DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.sdk.RepairRequest();
  }

  public static com.xforceplus.ultraman.oqsengine.sdk.RepairRequest getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<RepairRequest>
      PARSER = new com.google.protobuf.AbstractParser<RepairRequest>() {
    @java.lang.Override
    public RepairRequest parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new RepairRequest(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<RepairRequest> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<RepairRequest> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public com.xforceplus.ultraman.oqsengine.sdk.RepairRequest getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
