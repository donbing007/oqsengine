package com.xforceplus.ultraman.oqsengine.meta.common.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

import java.util.ArrayList;
import java.util.List;

/**
 * desc :
 * name : EntityClassStorage
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class EntityClassStorage {

    /**
     * 元数据boId
     */
    private long id;

    /**
     * 对象名称
     */
    private String name;

    /**
     * 对象code
     */
    private String code;
    /**
     * 元数据版本.
     */
    private int version;

    /**
     * 元信息处于的继承层级
     */
    private int level;

    /**
     * 关系信息
     */
    private List<RelationStorage> relations;

    /**
     * 继承的对象类型.
     */
    private Long fatherId;


    /**
     * 家族祖先的对象Id.
     */
    private List<Long> ancestors;

    /**
     * entityField
     */
    private List<IEntityField> fields;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<RelationStorage> getRelations() {
        return relations;
    }

    public void setRelations(List<RelationStorage> relations) {
        this.relations = relations;
    }

    public Long getFatherId() {
        return fatherId;
    }

    public void setFatherId(Long fatherId) {
        this.fatherId = fatherId;
    }

    public List<Long> getAncestors() {
        return ancestors;
    }

    public void setAncestors(List<Long> ancestors) {
        this.ancestors = ancestors;
    }

    public void addAncestors(Long ancestor) {
        if (null == this.ancestors) {
            this.ancestors = new ArrayList<>();
        }
        this.ancestors.add(ancestor);
    }

    public List<IEntityField> getFields() {
        return fields;
    }

    public void setFields(List<IEntityField> fields) {
        this.fields = fields;
    }
}
