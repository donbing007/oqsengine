// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: transfer.proto

package com.xforceplus.ultraman.oqsengine.sdk;

/**
 * Protobuf type {@code FieldSortUp}
 */
public  final class FieldSortUp extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:FieldSortUp)
    FieldSortUpOrBuilder {
private static final long serialVersionUID = 0L;
  // Use FieldSortUp.newBuilder() to construct.
  private FieldSortUp(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private FieldSortUp() {
    code_ = "";
    order_ = 0;
  }

  @Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private FieldSortUp(
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

            order_ = rawValue;
            break;
          }
          case 26: {
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
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return EntityResourceProto.internal_static_FieldSortUp_descriptor;
  }

  protected FieldAccessorTable
      internalGetFieldAccessorTable() {
    return EntityResourceProto.internal_static_FieldSortUp_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            FieldSortUp.class, FieldSortUp.Builder.class);
  }

  /**
   * Protobuf enum {@code FieldSortUp.Order}
   */
  public enum Order
      implements com.google.protobuf.ProtocolMessageEnum {
    /**
     * <code>asc = 0;</code>
     */
    asc(0),
    /**
     * <code>desc = 1;</code>
     */
    desc(1),
    UNRECOGNIZED(-1),
    ;

    /**
     * <code>asc = 0;</code>
     */
    public static final int asc_VALUE = 0;
    /**
     * <code>desc = 1;</code>
     */
    public static final int desc_VALUE = 1;


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
    public static Order valueOf(int value) {
      return forNumber(value);
    }

    public static Order forNumber(int value) {
      switch (value) {
        case 0: return asc;
        case 1: return desc;
        default: return null;
      }
    }

    public static com.google.protobuf.Internal.EnumLiteMap<Order>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static final com.google.protobuf.Internal.EnumLiteMap<
        Order> internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<Order>() {
            public Order findValueByNumber(int number) {
              return Order.forNumber(number);
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
      return FieldSortUp.getDescriptor().getEnumTypes().get(0);
    }

    private static final Order[] VALUES = values();

    public static Order valueOf(
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

    private Order(int value) {
      this.value = value;
    }

    // @@protoc_insertion_point(enum_scope:FieldSortUp.Order)
  }

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

  public static final int ORDER_FIELD_NUMBER = 2;
  private int order_;
  /**
   * <code>.FieldSortUp.Order order = 2;</code>
   */
  public int getOrderValue() {
    return order_;
  }
  /**
   * <code>.FieldSortUp.Order order = 2;</code>
   */
  public FieldSortUp.Order getOrder() {
    FieldSortUp.Order result = FieldSortUp.Order.valueOf(order_);
    return result == null ? FieldSortUp.Order.UNRECOGNIZED : result;
  }

  public static final int FIELD_FIELD_NUMBER = 3;
  private FieldUp field_;
  /**
   * <code>.FieldUp field = 3;</code>
   */
  public boolean hasField() {
    return field_ != null;
  }
  /**
   * <code>.FieldUp field = 3;</code>
   */
  public FieldUp getField() {
    return field_ == null ? FieldUp.getDefaultInstance() : field_;
  }
  /**
   * <code>.FieldUp field = 3;</code>
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
    if (order_ != FieldSortUp.Order.asc.getNumber()) {
      output.writeEnum(2, order_);
    }
    if (field_ != null) {
      output.writeMessage(3, getField());
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
    if (order_ != FieldSortUp.Order.asc.getNumber()) {
      size += com.google.protobuf.CodedOutputStream
        .computeEnumSize(2, order_);
    }
    if (field_ != null) {
      size += com.google.protobuf.CodedOutputStream
        .computeMessageSize(3, getField());
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
    if (!(obj instanceof FieldSortUp)) {
      return super.equals(obj);
    }
    FieldSortUp other = (FieldSortUp) obj;

    boolean result = true;
    result = result && getCode()
        .equals(other.getCode());
    result = result && order_ == other.order_;
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
    hash = (37 * hash) + ORDER_FIELD_NUMBER;
    hash = (53 * hash) + order_;
    if (hasField()) {
      hash = (37 * hash) + FIELD_FIELD_NUMBER;
      hash = (53 * hash) + getField().hashCode();
    }
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static FieldSortUp parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static FieldSortUp parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static FieldSortUp parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static FieldSortUp parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static FieldSortUp parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static FieldSortUp parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static FieldSortUp parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static FieldSortUp parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static FieldSortUp parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static FieldSortUp parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static FieldSortUp parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static FieldSortUp parseFrom(
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
  public static Builder newBuilder(FieldSortUp prototype) {
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
   * Protobuf type {@code FieldSortUp}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:FieldSortUp)
      FieldSortUpOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return EntityResourceProto.internal_static_FieldSortUp_descriptor;
    }

    protected FieldAccessorTable
        internalGetFieldAccessorTable() {
      return EntityResourceProto.internal_static_FieldSortUp_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              FieldSortUp.class, FieldSortUp.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.sdk.FieldSortUp.newBuilder()
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

      order_ = 0;

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
      return EntityResourceProto.internal_static_FieldSortUp_descriptor;
    }

    public FieldSortUp getDefaultInstanceForType() {
      return FieldSortUp.getDefaultInstance();
    }

    public FieldSortUp build() {
      FieldSortUp result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public FieldSortUp buildPartial() {
      FieldSortUp result = new FieldSortUp(this);
      result.code_ = code_;
      result.order_ = order_;
      if (fieldBuilder_ == null) {
        result.field_ = field_;
      } else {
        result.field_ = fieldBuilder_.build();
      }
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
      if (other instanceof FieldSortUp) {
        return mergeFrom((FieldSortUp)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(FieldSortUp other) {
      if (other == FieldSortUp.getDefaultInstance()) return this;
      if (!other.getCode().isEmpty()) {
        code_ = other.code_;
        onChanged();
      }
      if (other.order_ != 0) {
        setOrderValue(other.getOrderValue());
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
      FieldSortUp parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (FieldSortUp) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

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

    private int order_ = 0;
    /**
     * <code>.FieldSortUp.Order order = 2;</code>
     */
    public int getOrderValue() {
      return order_;
    }
    /**
     * <code>.FieldSortUp.Order order = 2;</code>
     */
    public Builder setOrderValue(int value) {
      order_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>.FieldSortUp.Order order = 2;</code>
     */
    public FieldSortUp.Order getOrder() {
      FieldSortUp.Order result = FieldSortUp.Order.valueOf(order_);
      return result == null ? FieldSortUp.Order.UNRECOGNIZED : result;
    }
    /**
     * <code>.FieldSortUp.Order order = 2;</code>
     */
    public Builder setOrder(FieldSortUp.Order value) {
      if (value == null) {
        throw new NullPointerException();
      }

      order_ = value.getNumber();
      onChanged();
      return this;
    }
    /**
     * <code>.FieldSortUp.Order order = 2;</code>
     */
    public Builder clearOrder() {

      order_ = 0;
      onChanged();
      return this;
    }

    private FieldUp field_ = null;
    private com.google.protobuf.SingleFieldBuilderV3<
        FieldUp, FieldUp.Builder, FieldUpOrBuilder> fieldBuilder_;
    /**
     * <code>.FieldUp field = 3;</code>
     */
    public boolean hasField() {
      return fieldBuilder_ != null || field_ != null;
    }
    /**
     * <code>.FieldUp field = 3;</code>
     */
    public FieldUp getField() {
      if (fieldBuilder_ == null) {
        return field_ == null ? FieldUp.getDefaultInstance() : field_;
      } else {
        return fieldBuilder_.getMessage();
      }
    }
    /**
     * <code>.FieldUp field = 3;</code>
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
     * <code>.FieldUp field = 3;</code>
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
     * <code>.FieldUp field = 3;</code>
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
     * <code>.FieldUp field = 3;</code>
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
     * <code>.FieldUp field = 3;</code>
     */
    public FieldUp.Builder getFieldBuilder() {

      onChanged();
      return getFieldFieldBuilder().getBuilder();
    }
    /**
     * <code>.FieldUp field = 3;</code>
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
     * <code>.FieldUp field = 3;</code>
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


    // @@protoc_insertion_point(builder_scope:FieldSortUp)
  }

  // @@protoc_insertion_point(class_scope:FieldSortUp)
  private static final FieldSortUp DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new FieldSortUp();
  }

  public static FieldSortUp getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<FieldSortUp>
      PARSER = new com.google.protobuf.AbstractParser<FieldSortUp>() {
    public FieldSortUp parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new FieldSortUp(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<FieldSortUp> parser() {
    return PARSER;
  }

  @Override
  public com.google.protobuf.Parser<FieldSortUp> getParserForType() {
    return PARSER;
  }

  public FieldSortUp getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
