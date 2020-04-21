package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author admin
 */
public class AliasField implements IEntityField, Wrapped<IEntityField>, Serializable {

    /**
     * origin field
     */
    private final IEntityField originField;

    private Set<String> alias = new HashSet<>();

    public AliasField(IEntityField originField){
        this.originField = originField;
        alias.add(originField.name());
    }

    public void addName(String name){
        alias.add(name);
    }

    @Override
    public long id() {
        return originField.id();
    }

    @Override
    public String name() {
        return String.join(",", alias);
    }

    @Override
    public String cnName() {
        return originField.cnName();
    }

    @Override
    public FieldType type() {
        return originField.type();
    }

    @Override
    public FieldConfig config() {
        return originField.config();
    }

    @Override
    public String dictId() {
        return originField.dictId();
    }

    @Override
    public String defaultValue() {
        return originField.defaultValue();
    }

    @Override
    public String toString() {
        return "AliasField{" +
                "id=" + id() +
                ", alias=" + alias +
                ", originField=" + originField +
                '}';
    }

    @Override
    public IEntityField getOriginObject() {
        return originField;
    }
}
