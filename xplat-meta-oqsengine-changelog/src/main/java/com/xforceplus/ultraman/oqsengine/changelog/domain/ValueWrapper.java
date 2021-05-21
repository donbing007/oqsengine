package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * value wrapper
 */
public class ValueWrapper {

    private String value;

    private FieldType type;

    private Long fieldId;

    private Long ownerEntityId;

    public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    public ValueWrapper() {
    }

    public ValueWrapper(String value, FieldType type, Long fieldId) {
        this.value = value;
        this.type = type;
        this.fieldId = fieldId;
    }

    public static ZoneId getZoneId() {
        return zoneId;
    }

    @JsonIgnore
    public IValue getIValue(){
        switch (type){
            case LONG:
                return new LongValue(null, Long.parseLong(value));
            case DECIMAL:
                return new DecimalValue(null, new BigDecimal(value));
            case DATETIME:
                long timestamp = Long.parseLong(value);
                LocalDateTime time =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                                DateTimeValue.ZONE_ID);
                return new DateTimeValue(null, time);
            case BOOLEAN:
                boolean b = Boolean.parseBoolean(value);
                return new BooleanValue(null, b);
            case ENUM:
                return new EnumValue(null, value);
            case STRINGS:
                return new StringsValue(null, Optional.ofNullable(value).orElse("").split(","));
            case STRING:
                return new StringValue(null, value);
            default:
                throw new UnsupportedOperationException("Cannnot convert to ivalue");
        }
    }

    public Long valueToLong(){
        return getIValue().valueToLong();
    }

    @JsonGetter
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @JsonGetter
    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    @JsonGetter
    public Long getFieldId() {
        return fieldId;
    }

    public void setFieldId(Long fieldId) {
        this.fieldId = fieldId;
    }

    @JsonGetter
    public Long getOwnerEntityId() {
        return ownerEntityId;
    }

    public void setOwnerEntityId(Long ownerEntityId) {
        this.ownerEntityId = ownerEntityId;
    }
}
