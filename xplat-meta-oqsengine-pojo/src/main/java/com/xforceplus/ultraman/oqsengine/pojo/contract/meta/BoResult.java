package com.xforceplus.ultraman.oqsengine.pojo.contract.meta;

import com.xforceplus.ultraman.oqsengine.pojo.contract.Result;
import com.xforceplus.ultraman.oqsengine.pojo.dto.Api;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 业务对象结果对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class BoResult extends Result implements Serializable {
    private Long id;
    private String code;
    private EntityClass entityClass;
    private List<Field> fields;
    private List<Api> apis;

    public BoResult(Object status) {
        super(status);
    }

    public BoResult(Object status, String message) {
        super(status, message);
    }

    public BoResult(Object status, Collection values, String message) {
        super(status, values, message);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public EntityClass getEntityClass() {
        return entityClass;
    }

    public void setEntityClass(EntityClass entityClass) {
        this.entityClass = entityClass;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public List<Api> getApis() {
        return apis;
    }

    public void setApis(List<Api> apis) {
        this.apis = apis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoResult)) return false;
        if (!super.equals(o)) return false;
        BoResult boResult = (BoResult) o;
        return Objects.equals(getId(), boResult.getId()) &&
                Objects.equals(getCode(), boResult.getCode()) &&
                Objects.equals(getEntityClass(), boResult.getEntityClass()) &&
                Objects.equals(getFields(), boResult.getFields()) &&
                Objects.equals(getApis(), boResult.getApis());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), getCode(), getEntityClass(), getFields(), getApis());
    }

}
