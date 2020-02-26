// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

/**
 * Protobuf type {@code FieldConditionUp}
 */
public  final class FieldConditionUp extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:FieldConditionUp)
    FieldConditionUpOrBuilder {
private static final long serialVersionUID = 0L;
  // Use FieldConditionUp.newBuilder() to construct.
  private FieldConditionUp(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FieldConditionUp() {
    code_ = "";
    operation_ = 0;
    values_ = com.google.protobuf.LazyStringArrayList.EMPTY;
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private FieldConditionUp(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new NullPointerException();
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
            String s = input.readStringRequireUtf8();

            code_ = s;
            break;
          }
          case 16: {
            int rawValue = input.readEnum();

            operation_ = rawValue;
            break;
          }
          case 26: {
            String s = input.readStringRequireUtf8();
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              values_ = new com.google.protobuf.LazyStringArrayList();
              mutable_bitField0_ |= 0x00000004;
            }
            values_.add(s);
            break;
          }
          case 34: {
            FieldUp.Builder subBuilder = null;
            if (field_ != null) {
              subBuilder = field_.toBuilder();
            }
            field_ = input.readMessage(FieldUp.parser(), extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(field_);
              field_ = subBuilder.buildPartial();
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
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        values_ = values_.getUnmodifiableView();
      }
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return EntityResourceProto.internal_static_FieldConditionUp_descriptor;
  }

  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return EntityResourceProto.internal_static_FieldConditionUp_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            FieldConditionUp.class, FieldConditionUp.Builder.class);
  }

  /**
   * Protobuf enum {@code FieldConditionUp.Op}
   */
  public enum Op
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>eq = 0;</code>
     */
    eq(0),
    /**
     * <code>like = 1;</code>
     */
    like(1),
    /**
     * <code>in = 2;</code>
     */
    in(2),
    /**
     * <code>ge_le = 3;</code>
     */
    ge_le(3),
    /**
     * <code>ge_lt = 4;</code>
     */
    ge_lt(4),
    /**
     * <code>gt_le = 5;</code>
     */
    gt_le(5),
    /**
     * <code>gt_lt = 6;</code>
     */
    gt_lt(6),
    /**
     * <code>gt = 7;</code>
     */
    gt(7),
    /**
     * <code>ge = 8;</code>
     */
    ge(8),
    /**
     * <code>lt = 9;</code>
     */
    lt(9),
    /**
     * <code>le = 10;</code>
     */
    le(10),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>eq = 0;</code>
     */
    public static final int eq_VALUE = 0;
    /**
     * <code>like = 1;</code>
     */
    public static final int like_VALUE = 1;
    /**
     * <code>in = 2;</code>
     */
    public static final int in_VALUE = 2;
    /**
     * <code>ge_le = 3;</code>
     */
    public static final int ge_le_VALUE = 3;
    /**
     * <code>ge_lt = 4;</code>
     */
    public static final int ge_lt_VALUE = 4;
    /**
     * <code>gt_le = 5;</code>
     */
    public static final int gt_le_VALUE = 5;
    /**
     * <code>gt_lt = 6;</code>
     */
    public static final int gt_lt_VALUE = 6;
    /**
     * <code>gt = 7;</code>
     */
    public static final int gt_VALUE = 7;
    /**
     * <code>ge = 8;</code>
     */
    public static final int ge_VALUE = 8;
    /**
     * <code>lt = 9;</code>
     */
    public static final int lt_VALUE = 9;
    /**
     * <code>le = 10;</code>
     */
    public static final int le_VALUE = 10;


    public final int getNumber() {
      if (this == UNRECOGNIZED) {
        throw new IllegalArgumentException(
            "Can't get the number of an unknown enum value.");
      }
      return value;
    }

    /**
     * @deprecated Use {@link #forNumber(int)} instead.
     */
    @Deprecated
    public static Op valueOf(int value) {
      return forNumber(value);
    }

    public static Op forNumber(int value) {
      switch (value) {
        case 0: return eq;
        case 1: return like;
        case 2: return in;
        case 3: return ge_le;
        case 4: return ge_lt;
        case 5: return gt_le;
        case 6: return gt_lt;
        case 7: return gt;
        case 8: return ge;
        case 9: return lt;
        case 10: return le;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Op>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Op> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Op>() {
            public Op findValueByNumber(int number) {
              return Op.forNumber(number);
            }
          };

    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(ordinal());
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return FieldConditionUp.getDescriptor().getEnumTypes().get(0);
    }

    private static final Op[] VALUES = values();

    public static Op valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      if (desc.getIndex() == -1) {
        return UNRECOGNIZED;
      }
      return VALUES[desc.getIndex()];
    }

    private final int value;

    private Op(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:FieldConditionUp.Op)
  }

  private int bitField0_;
  public static final int CODE_FIELD_NUMBER = 1;
  private volatile Object code_;
  /**
   * <code>string code = 1;</code>
   */
  public String getCode() {
    Object ref = code_;
    if (ref instanceof String) {
      return (String) ref;
    } else {
      com.google.protobuf.ByteString bs =
          (com.google.protobuf.ByteString) ref;
      String s = bs.toStringUtf8();
      code_ = s;
      return s;
    }
  }
  /**
   * <code>string code = 1;</code>
   */
  public com.google.protobuf.ByteString
      getCodeBytes() {
    Object ref = code_;
    if (ref instanceof String) {
      com.google.protobuf.ByteString b =
          com.google.protobuf.ByteString.copyFromUtf8(
              (String) ref);
      code_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int OPERATION_FIELD_NUMBER = 2;
  private int operation_;
  /**
   * <code>.FieldConditionUp.Op operation = 2;</code>
   */
  public int getOperationValue() {
    return operation_;
  }
  /**
   * <code>.FieldConditionUp.Op operation = 2;</code>
   */
  public FieldConditionUp.Op getOperation() {
    FieldConditionUp.Op result = FieldConditionUp.Op.valueOf(operation_);
    return result == null ? FieldConditionUp.Op.UNRECOGNIZED : result;
  }

  public static final int VALUES_FIELD_NUMBER = 3;
  private com.google.protobuf.LazyStringList values_;
  /**
   * <code>repeated string values = 3;</code>
   */
  public com.google.protobuf.ProtocolStringList
      getValuesList() {
    return values_;
  }
  /**
   * <code>repeated string values = 3;</code>
   */
  public int getValuesCount() {
    return values_.size();
  }
  /**
   * <code>repeated string values = 3;</code>
   */
  public String getValues(int index) {
    return values_.get(index);
  }
  /**
   * <code>repeated string values = 3;</code>
   */
  public com.google.protobuf.ByteString
      getValuesBytes(int index) {
    return values_.getByteString(index);
  }

  public static final int FIELD_FIELD_NUMBER = 4;
  private FieldUp field_;
  /**
   * <code>.FieldUp field = 4;</code>
   */
  public boolean hasField() {
    return field_ != null;
  }
  /**
   * <code>.FieldUp field = 4;</code>
   */
  public FieldUp getField() {
    return field_ == null ? FieldUp.getDefaultInstance() : field_;
  }
  /**
   * <code>.FieldUp field = 4;</code>
   */
  public FieldUpOrBuilder getFieldOrBuilder() {
    return getField();
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
    if (!getCodeBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, code_);
    }
    if (operation_ != FieldConditionUp.Op.eq.getNumber()) {
      output.writeEnum(2, operation_);
    }
    for (int i = 0; i < values_.size(); i++) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, values_.getRaw(i));
    }
    if (field_ != null) {
      output.writeMessage(4, getField());
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getCodeBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, code_);
    }
    if (operation_ != FieldConditionUp.Op.eq.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, operation_);
    }
    {
      int dataSize = 0;
      for (int i = 0; i < values_.size(); i++) {
        dataSize += computeStringSizeNoTag(values_.getRaw(i));
      }
      size += dataSize;
      size += 1 * getValuesList().size();
    }
    if (field_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(4, getField());
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof FieldConditionUp)) {
      return super.equals(obj);
    }
    FieldConditionUp other = (FieldConditionUp) obj;

    boolean result = true;
    result = result && getCode()
        .equals(other.getCode());
    result = result && operation_ == other.operation_;
    result = result && getValuesList()
        .equals(other.getValuesList());
    result = result && (hasField() == other.hasField());
    if (hasField()) {
      result = result && getField()
          .equals(other.getField());
    }
    result = result && unknownFields.equals(other.unknownFields);
    return result;
  }

  @Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + CODE_FIELD_NUMBER;
    hash = (53 * hash) + getCode().hashCode();
    hash = (37 * hash) + OPERATION_FIELD_NUMBER;
    hash = (53 * hash) + operation_;
    if (getValuesCount() > 0) {
      hash = (37 * hash) + VALUES_FIELD_NUMBER;
      hash = (53 * hash) + getValuesList().hashCode();
    }
    if (hasField()) {
      hash = (37 * hash) + FIELD_FIELD_NUMBER;
      hash = (53 * hash) + getField().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static FieldConditionUp parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static FieldConditionUp parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static FieldConditionUp parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static FieldConditionUp parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static FieldConditionUp parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static FieldConditionUp parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static FieldConditionUp parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static FieldConditionUp parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static FieldConditionUp parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static FieldConditionUp parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static FieldConditionUp parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static FieldConditionUp parseFrom(
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
  public static Builder newBuilder(FieldConditionUp prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @Override
  protected Builder newBuilderForType(
      BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code FieldConditionUp}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:FieldConditionUp)
      FieldConditionUpOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return EntityResourceProto.internal_static_FieldConditionUp_descriptor;
    }

    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return EntityResourceProto.internal_static_FieldConditionUp_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              FieldConditionUp.class, FieldConditionUp.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.sdk.FieldConditionUp.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        BuilderParent parent) {
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
      code_ = "";

      operation_ = 0;

      values_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000004);
      if (fieldBuilder_ == null) {
        field_ = null;
      } else {
        field_ = null;
        fieldBuilder_ = null;
      }
      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return EntityResourceProto.internal_static_FieldConditionUp_descriptor;
    }

    public FieldConditionUp getDefaultInstanceForType() {
      return FieldConditionUp.getDefaultInstance();
    }

    public FieldConditionUp build() {
      FieldConditionUp result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public FieldConditionUp buildPartial() {
      FieldConditionUp result = new FieldConditionUp(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      result.code_ = code_;
      result.operation_ = operation_;
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        values_ = values_.getUnmodifiableView();
        bitField0_ = (bitField0_ & ~0x00000004);
      }
      result.values_ = values_;
      if (fieldBuilder_ == null) {
        result.field_ = field_;
      } else {
        result.field_ = fieldBuilder_.build();
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
        Object value) {
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
        int index, Object value) {
      return (Builder) super.setRepeatedField(field, index, value);
    }
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        Object value) {
      return (Builder) super.addRepeatedField(field, value);
    }
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof FieldConditionUp) {
        return mergeFrom((FieldConditionUp)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(FieldConditionUp other) {
      if (other == FieldConditionUp.getDefaultInstance()) return this;
      if (!other.getCode().isEmpty()) {
        code_ = other.code_;
        onChanged();
      }
      if (other.operation_ != 0) {
        setOperationValue(other.getOperationValue());
      }
      if (!other.values_.isEmpty()) {
        if (values_.isEmpty()) {
          values_ = other.values_;
          bitField0_ = (bitField0_ & ~0x00000004);
        } else {
          ensureValuesIsMutable();
          values_.addAll(other.values_);
        }
        onChanged();
      }
      if (other.hasField()) {
        mergeField(other.getField());
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
      FieldConditionUp parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (FieldConditionUp) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private Object code_ = "";
    /**
     * <code>string code = 1;</code>
     */
    public String getCode() {
      Object ref = code_;
      if (!(ref instanceof String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        String s = bs.toStringUtf8();
        code_ = s;
        return s;
      } else {
        return (String) ref;
      }
    }
    /**
     * <code>string code = 1;</code>
     */
    public com.google.protobuf.ByteString
        getCodeBytes() {
      Object ref = code_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b =
            com.google.protobuf.ByteString.copyFromUtf8(
                (String) ref);
        code_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string code = 1;</code>
     */
    public Builder setCode(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }

      code_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string code = 1;</code>
     */
    public Builder clearCode() {

      code_ = getDefaultInstance().getCode();
      onChanged();
      return this;
    }
    /**
     * <code>string code = 1;</code>
     */
    public Builder setCodeBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);

      code_ = value;
      onChanged();
      return this;
    }

    private int operation_ = 0;
    /**
     * <code>.FieldConditionUp.Op operation = 2;</code>
     */
    public int getOperationValue() {
      return operation_;
    }
    /**
     * <code>.FieldConditionUp.Op operation = 2;</code>
     */
    public Builder setOperationValue(int value) {
      operation_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.FieldConditionUp.Op operation = 2;</code>
     */
    public FieldConditionUp.Op getOperation() {
      FieldConditionUp.Op result = FieldConditionUp.Op.valueOf(operation_);
      return result == null ? FieldConditionUp.Op.UNRECOGNIZED : result;
    }
    /**
     * <code>.FieldConditionUp.Op operation = 2;</code>
     */
    public Builder setOperation(FieldConditionUp.Op value) {
      if (value == null) {
        throw new NullPointerException();
      }

      operation_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.FieldConditionUp.Op operation = 2;</code>
     */
    public Builder clearOperation() {

      operation_ = 0;
      onChanged();
      return this;
    }

    private com.google.protobuf.LazyStringList values_ = com.google.protobuf.LazyStringArrayList.EMPTY;
    private void ensureValuesIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        values_ = new com.google.protobuf.LazyStringArrayList(values_);
        bitField0_ |= 0x00000004;
       }
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public com.google.protobuf.ProtocolStringList
        getValuesList() {
      return values_.getUnmodifiableView();
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public int getValuesCount() {
      return values_.size();
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public String getValues(int index) {
      return values_.get(index);
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public com.google.protobuf.ByteString
        getValuesBytes(int index) {
      return values_.getByteString(index);
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public Builder setValues(
        int index, String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureValuesIsMutable();
      values_.set(index, value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public Builder addValues(
        String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  ensureValuesIsMutable();
      values_.add(value);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public Builder addAllValues(
        Iterable<String> values) {
      ensureValuesIsMutable();
      com.google.protobuf.AbstractMessageLite.Builder.addAll(
          values, values_);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public Builder clearValues() {
      values_ = com.google.protobuf.LazyStringArrayList.EMPTY;
      bitField0_ = (bitField0_ & ~0x00000004);
      onChanged();
      return this;
    }
    /**
     * <code>repeated string values = 3;</code>
     */
    public Builder addValuesBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      ensureValuesIsMutable();
      values_.add(value);
      onChanged();
      return this;
    }

    private FieldUp field_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        FieldUp, FieldUp.Builder, FieldUpOrBuilder> fieldBuilder_;
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public boolean hasField() {
      return fieldBuilder_ != null || field_ != null;
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public FieldUp getField() {
      if (fieldBuilder_ == null) {
        return field_ == null ? FieldUp.getDefaultInstance() : field_;
      } else {
        return fieldBuilder_.getMessage();
      }
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public Builder setField(FieldUp value) {
      if (fieldBuilder_ == null) {
        if (value == null) {
          throw new NullPointerException();
        }
        field_ = value;
        onChanged();
      } else {
        fieldBuilder_.setMessage(value);
      }

      return this;
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public Builder setField(
        FieldUp.Builder builderForValue) {
      if (fieldBuilder_ == null) {
        field_ = builderForValue.build();
        onChanged();
      } else {
        fieldBuilder_.setMessage(builderForValue.build());
      }

      return this;
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public Builder mergeField(FieldUp value) {
      if (fieldBuilder_ == null) {
        if (field_ != null) {
          field_ =
            FieldUp.newBuilder(field_).mergeFrom(value).buildPartial();
        } else {
          field_ = value;
        }
        onChanged();
      } else {
        fieldBuilder_.mergeFrom(value);
      }

      return this;
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public Builder clearField() {
      if (fieldBuilder_ == null) {
        field_ = null;
        onChanged();
      } else {
        field_ = null;
        fieldBuilder_ = null;
      }

      return this;
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public FieldUp.Builder getFieldBuilder() {

      onChanged();
      return getFieldFieldBuilder().getBuilder();
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    public FieldUpOrBuilder getFieldOrBuilder() {
      if (fieldBuilder_ != null) {
        return fieldBuilder_.getMessageOrBuilder();
      } else {
        return field_ == null ?
            FieldUp.getDefaultInstance() : field_;
      }
    }
    /**
     * <code>.FieldUp field = 4;</code>
     */
    private com.google.protobuf.SingleFieldBuilderV3<
        FieldUp, FieldUp.Builder, FieldUpOrBuilder>
        getFieldFieldBuilder() {
      if (fieldBuilder_ == null) {
        fieldBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
            FieldUp, FieldUp.Builder, FieldUpOrBuilder>(
                getField(),
                getParentForChildren(),
                isClean());
        field_ = null;
      }
      return fieldBuilder_;
    }
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFieldsProto3(unknownFields);
    }

    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:FieldConditionUp)
  }

  // @@protoc_insertion_point(class_scope:FieldConditionUp)
  private static final FieldConditionUp DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new FieldConditionUp();
  }

  public static FieldConditionUp getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FieldConditionUp>
      PARSER = new com.google.protobuf.AbstractParser<FieldConditionUp>() {
    public FieldConditionUp parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new FieldConditionUp(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<FieldConditionUp> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<FieldConditionUp> getParserForType() {
    return PARSER;
  }

  public FieldConditionUp getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

