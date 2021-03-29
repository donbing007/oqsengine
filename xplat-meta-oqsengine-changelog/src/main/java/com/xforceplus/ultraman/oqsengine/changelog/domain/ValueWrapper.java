package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * value wrapper
 */
public class ValueWrapper {

    private Object value;

    private FieldType type;

    private Long fieldId;

    private Long ownerEntityId;

    public static final ZoneId zoneId = ZoneId.of("Asia/Shanghai");

    public ValueWrapper() {
    }

    public ValueWrapper(Object value, FieldType type, Long fieldId) {
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
                return new LongValue(null, (Long)value);
            case DECIMAL:
                return new DecimalValue(null, (BigDecimal) value);
            case DATETIME:
                return new DateTimeValue(null, (LocalDateTime)value);
            case BOOLEAN:
                return new BooleanValue(null, (Boolean) value);
            case ENUM:
                return new EnumValue(null, (String)value);
            case STRINGS:
                return new StringsValue(null, (String[])value);
            case STRING:
                return new StringValue(null, (String)value);
            default:
                throw new UnsupportedOperationException("Cannnot convert to ivalue");
        }
    }

    public Long valueToLong(){
        return getIValue().valueToLong();
    }

    @JsonGetter
    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
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
