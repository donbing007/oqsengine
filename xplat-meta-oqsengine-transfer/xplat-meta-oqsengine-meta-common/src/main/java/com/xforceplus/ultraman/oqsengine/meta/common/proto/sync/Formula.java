// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: sync.proto

package com.xforceplus.ultraman.oqsengine.meta.common.proto.sync;

/**
 * Protobuf type {@code Formula}
 */
public  final class Formula extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:Formula)
    FormulaOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Formula.newBuilder() to construct.
  private Formula(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Formula() {
    formula_ = "";
    level_ = 0;
    validator_ = "";
    min_ = "";
    max_ = "";
    condition_ = "";
    emptyValueTransfer_ = "";
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Formula(
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

            formula_ = s;
            break;
          }
          case 16: {

            level_ = input.readInt32();
            break;
          }
          case 26: {
            java.lang.String s = input.readStringRequireUtf8();

            validator_ = s;
            break;
          }
          case 34: {
            java.lang.String s = input.readStringRequireUtf8();

            min_ = s;
            break;
          }
          case 42: {
            java.lang.String s = input.readStringRequireUtf8();

            max_ = s;
            break;
          }
          case 50: {
            java.lang.String s = input.readStringRequireUtf8();

            condition_ = s;
            break;
          }
          case 58: {
            java.lang.String s = input.readStringRequireUtf8();

            emptyValueTransfer_ = s;
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
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_Formula_descriptor;
  }

  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_Formula_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.Builder.class);
  }

  public static final int FORMULA_FIELD_NUMBER = 1;
  private volatile java.lang.Object formula_;
  /**
   * <code>string formula = 1;</code>
   */
  public java.lang.String getFormula() {
    java.lang.Object ref = formula_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      formula_ = s;
      return s;
    }
  }
  /**
   * <code>string formula = 1;</code>
   */
  public com.google.protobuf.ByteString
      getFormulaBytes() {
    java.lang.Object ref = formula_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      formula_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int LEVEL_FIELD_NUMBER = 2;
  private int level_;
  /**
   * <code>int32 level = 2;</code>
   */
  public int getLevel() {
    return level_;
  }

  public static final int VALIDATOR_FIELD_NUMBER = 3;
  private volatile java.lang.Object validator_;
  /**
   * <code>string validator = 3;</code>
   */
  public java.lang.String getValidator() {
    java.lang.Object ref = validator_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      validator_ = s;
      return s;
    }
  }
  /**
   * <code>string validator = 3;</code>
   */
  public com.google.protobuf.ByteString
      getValidatorBytes() {
    java.lang.Object ref = validator_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      validator_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MIN_FIELD_NUMBER = 4;
  private volatile java.lang.Object min_;
  /**
   * <code>string min = 4;</code>
   */
  public java.lang.String getMin() {
    java.lang.Object ref = min_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      min_ = s;
      return s;
    }
  }
  /**
   * <code>string min = 4;</code>
   */
  public com.google.protobuf.ByteString
      getMinBytes() {
    java.lang.Object ref = min_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      min_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int MAX_FIELD_NUMBER = 5;
  private volatile java.lang.Object max_;
  /**
   * <code>string max = 5;</code>
   */
  public java.lang.String getMax() {
    java.lang.Object ref = max_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      max_ = s;
      return s;
    }
  }
  /**
   * <code>string max = 5;</code>
   */
  public com.google.protobuf.ByteString
      getMaxBytes() {
    java.lang.Object ref = max_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      max_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int CONDITION_FIELD_NUMBER = 6;
  private volatile java.lang.Object condition_;
  /**
   * <code>string condition = 6;</code>
   */
  public java.lang.String getCondition() {
    java.lang.Object ref = condition_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      condition_ = s;
      return s;
    }
  }
  /**
   * <code>string condition = 6;</code>
   */
  public com.google.protobuf.ByteString
      getConditionBytes() {
    java.lang.Object ref = condition_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      condition_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  public static final int EMPTYVALUETRANSFER_FIELD_NUMBER = 7;
  private volatile java.lang.Object emptyValueTransfer_;
  /**
   * <code>string emptyValueTransfer = 7;</code>
   */
  public java.lang.String getEmptyValueTransfer() {
    java.lang.Object ref = emptyValueTransfer_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      emptyValueTransfer_ = s;
      return s;
    }
  }
  /**
   * <code>string emptyValueTransfer = 7;</code>
   */
  public com.google.protobuf.ByteString
      getEmptyValueTransferBytes() {
    java.lang.Object ref = emptyValueTransfer_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      emptyValueTransfer_ = b;
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
    if (!getFormulaBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 1, formula_);
    }
    if (level_ != 0) {
      output.writeInt32(2, level_);
    }
    if (!getValidatorBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 3, validator_);
    }
    if (!getMinBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 4, min_);
    }
    if (!getMaxBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 5, max_);
    }
    if (!getConditionBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 6, condition_);
    }
    if (!getEmptyValueTransferBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 7, emptyValueTransfer_);
    }
    unknownFields.writeTo(output);
  }

  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!getFormulaBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, formula_);
    }
    if (level_ != 0) {
      size += com.google.protobuf.CodedOutputStream
        .computeInt32Size(2, level_);
    }
    if (!getValidatorBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(3, validator_);
    }
    if (!getMinBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(4, min_);
    }
    if (!getMaxBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(5, max_);
    }
    if (!getConditionBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(6, condition_);
    }
    if (!getEmptyValueTransferBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(7, emptyValueTransfer_);
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
    if (!(obj instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula)) {
      return super.equals(obj);
    }
    com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula other = (com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula) obj;

    boolean result = true;
    result = result && getFormula()
        .equals(other.getFormula());
    result = result && (getLevel()
        == other.getLevel());
    result = result && getValidator()
        .equals(other.getValidator());
    result = result && getMin()
        .equals(other.getMin());
    result = result && getMax()
        .equals(other.getMax());
    result = result && getCondition()
        .equals(other.getCondition());
    result = result && getEmptyValueTransfer()
        .equals(other.getEmptyValueTransfer());
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
    hash = (37 * hash) + FORMULA_FIELD_NUMBER;
    hash = (53 * hash) + getFormula().hashCode();
    hash = (37 * hash) + LEVEL_FIELD_NUMBER;
    hash = (53 * hash) + getLevel();
    hash = (37 * hash) + VALIDATOR_FIELD_NUMBER;
    hash = (53 * hash) + getValidator().hashCode();
    hash = (37 * hash) + MIN_FIELD_NUMBER;
    hash = (53 * hash) + getMin().hashCode();
    hash = (37 * hash) + MAX_FIELD_NUMBER;
    hash = (53 * hash) + getMax().hashCode();
    hash = (37 * hash) + CONDITION_FIELD_NUMBER;
    hash = (53 * hash) + getCondition().hashCode();
    hash = (37 * hash) + EMPTYVALUETRANSFER_FIELD_NUMBER;
    hash = (53 * hash) + getEmptyValueTransfer().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parseFrom(
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
  public static Builder newBuilder(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula prototype) {
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
   * Protobuf type {@code Formula}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:Formula)
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.FormulaOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_Formula_descriptor;
    }

    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_Formula_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.class, com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.Builder.class);
    }

    // Construct using com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.newBuilder()
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
      formula_ = "";

      level_ = 0;

      validator_ = "";

      min_ = "";

      max_ = "";

      condition_ = "";

      emptyValueTransfer_ = "";

      return this;
    }

    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.EntityClassSyncProto.internal_static_Formula_descriptor;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula getDefaultInstanceForType() {
      return com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.getDefaultInstance();
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula build() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula buildPartial() {
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula result = new com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula(this);
      result.formula_ = formula_;
      result.level_ = level_;
      result.validator_ = validator_;
      result.min_ = min_;
      result.max_ = max_;
      result.condition_ = condition_;
      result.emptyValueTransfer_ = emptyValueTransfer_;
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
      if (other instanceof com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula) {
        return mergeFrom((com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula other) {
      if (other == com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula.getDefaultInstance()) return this;
      if (!other.getFormula().isEmpty()) {
        formula_ = other.formula_;
        onChanged();
      }
      if (other.getLevel() != 0) {
        setLevel(other.getLevel());
      }
      if (!other.getValidator().isEmpty()) {
        validator_ = other.validator_;
        onChanged();
      }
      if (!other.getMin().isEmpty()) {
        min_ = other.min_;
        onChanged();
      }
      if (!other.getMax().isEmpty()) {
        max_ = other.max_;
        onChanged();
      }
      if (!other.getCondition().isEmpty()) {
        condition_ = other.condition_;
        onChanged();
      }
      if (!other.getEmptyValueTransfer().isEmpty()) {
        emptyValueTransfer_ = other.emptyValueTransfer_;
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
      com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private java.lang.Object formula_ = "";
    /**
     * <code>string formula = 1;</code>
     */
    public java.lang.String getFormula() {
      java.lang.Object ref = formula_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        formula_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string formula = 1;</code>
     */
    public com.google.protobuf.ByteString
        getFormulaBytes() {
      java.lang.Object ref = formula_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        formula_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string formula = 1;</code>
     */
    public Builder setFormula(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      formula_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string formula = 1;</code>
     */
    public Builder clearFormula() {
      
      formula_ = getDefaultInstance().getFormula();
      onChanged();
      return this;
    }
    /**
     * <code>string formula = 1;</code>
     */
    public Builder setFormulaBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      formula_ = value;
      onChanged();
      return this;
    }

    private int level_ ;
    /**
     * <code>int32 level = 2;</code>
     */
    public int getLevel() {
      return level_;
    }
    /**
     * <code>int32 level = 2;</code>
     */
    public Builder setLevel(int value) {
      
      level_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>int32 level = 2;</code>
     */
    public Builder clearLevel() {
      
      level_ = 0;
      onChanged();
      return this;
    }

    private java.lang.Object validator_ = "";
    /**
     * <code>string validator = 3;</code>
     */
    public java.lang.String getValidator() {
      java.lang.Object ref = validator_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        validator_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string validator = 3;</code>
     */
    public com.google.protobuf.ByteString
        getValidatorBytes() {
      java.lang.Object ref = validator_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        validator_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string validator = 3;</code>
     */
    public Builder setValidator(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      validator_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string validator = 3;</code>
     */
    public Builder clearValidator() {
      
      validator_ = getDefaultInstance().getValidator();
      onChanged();
      return this;
    }
    /**
     * <code>string validator = 3;</code>
     */
    public Builder setValidatorBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      validator_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object min_ = "";
    /**
     * <code>string min = 4;</code>
     */
    public java.lang.String getMin() {
      java.lang.Object ref = min_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        min_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string min = 4;</code>
     */
    public com.google.protobuf.ByteString
        getMinBytes() {
      java.lang.Object ref = min_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        min_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string min = 4;</code>
     */
    public Builder setMin(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      min_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string min = 4;</code>
     */
    public Builder clearMin() {
      
      min_ = getDefaultInstance().getMin();
      onChanged();
      return this;
    }
    /**
     * <code>string min = 4;</code>
     */
    public Builder setMinBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      min_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object max_ = "";
    /**
     * <code>string max = 5;</code>
     */
    public java.lang.String getMax() {
      java.lang.Object ref = max_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        max_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string max = 5;</code>
     */
    public com.google.protobuf.ByteString
        getMaxBytes() {
      java.lang.Object ref = max_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        max_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string max = 5;</code>
     */
    public Builder setMax(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      max_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string max = 5;</code>
     */
    public Builder clearMax() {
      
      max_ = getDefaultInstance().getMax();
      onChanged();
      return this;
    }
    /**
     * <code>string max = 5;</code>
     */
    public Builder setMaxBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      max_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object condition_ = "";
    /**
     * <code>string condition = 6;</code>
     */
    public java.lang.String getCondition() {
      java.lang.Object ref = condition_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        condition_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string condition = 6;</code>
     */
    public com.google.protobuf.ByteString
        getConditionBytes() {
      java.lang.Object ref = condition_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        condition_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string condition = 6;</code>
     */
    public Builder setCondition(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      condition_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string condition = 6;</code>
     */
    public Builder clearCondition() {
      
      condition_ = getDefaultInstance().getCondition();
      onChanged();
      return this;
    }
    /**
     * <code>string condition = 6;</code>
     */
    public Builder setConditionBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      condition_ = value;
      onChanged();
      return this;
    }

    private java.lang.Object emptyValueTransfer_ = "";
    /**
     * <code>string emptyValueTransfer = 7;</code>
     */
    public java.lang.String getEmptyValueTransfer() {
      java.lang.Object ref = emptyValueTransfer_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        emptyValueTransfer_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <code>string emptyValueTransfer = 7;</code>
     */
    public com.google.protobuf.ByteString
        getEmptyValueTransferBytes() {
      java.lang.Object ref = emptyValueTransfer_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        emptyValueTransfer_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <code>string emptyValueTransfer = 7;</code>
     */
    public Builder setEmptyValueTransfer(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      emptyValueTransfer_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>string emptyValueTransfer = 7;</code>
     */
    public Builder clearEmptyValueTransfer() {
      
      emptyValueTransfer_ = getDefaultInstance().getEmptyValueTransfer();
      onChanged();
      return this;
    }
    /**
     * <code>string emptyValueTransfer = 7;</code>
     */
    public Builder setEmptyValueTransferBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      emptyValueTransfer_ = value;
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


    // @@protoc_insertion_point(builder_scope:Formula)
  }

  // @@protoc_insertion_point(class_scope:Formula)
  private static final com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula();
  }

  public static com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Formula>
      PARSER = new com.google.protobuf.AbstractParser<Formula>() {
    public Formula parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Formula(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Formula> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Formula> getParserForType() {
    return PARSER;
  }

  public com.xforceplus.ultraman.oqsengine.meta.common.proto.sync.Formula getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}
