package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.CalculationType;
import java.io.Serializable;

/**
 * Created by justin.xu on 07/2021.
 *
 * @since 1.8
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "calculationType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = Formula.class, name = "FORMULA"),
    @JsonSubTypes.Type(value = AutoFill.class, name = "AUTO_FILL"),
    @JsonSubTypes.Type(value = Lookup.class, name = "LOOKUP"),
    @JsonSubTypes.Type(value = Aggregation.class, name = "AGGREGATION"),
    @JsonSubTypes.Type(value = StaticCalculation.class, name = "STATIC"),
    @JsonSubTypes.Type(value = StaticCalculation.class, name = "UNKNOWN"),
})
public abstract class AbstractCalculation implements Serializable {
    @JsonProperty(value = "calculationType")
    protected CalculationType calculationType;

    @JsonProperty(value = "level")
    protected int level;

    public AbstractCalculation(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public CalculationType getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(CalculationType calculationType) {
        this.calculationType = calculationType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public abstract AbstractCalculation clone();


    /**
     * 是否需要在创建索引时索引附件信息.
     *
     * @return 默认为true，子类可重载逻辑.
     */
    public boolean indexAttachment() {
        return true;
    }
}

