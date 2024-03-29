package com.xforceplus.ultraman.oqsengine.metadata.dto.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 缓存的EntityClass storage.
 *
 * @author xujia
 * @since 1.8
 */
public class EntityClassStorage {

    /**
     * 元数据boId.
     */
    private long id;

    /**
     * 元数据appCode.
     */
    private String appCode;

    /**
     * 类型type.
     */
    private int type;

    /**
     * 对象名称.
     */
    private String name;

    /**
     * 对象code.
     */
    private String code;
    /**
     * 元数据版本.
     */
    private int version;

    /**
     * 元信息处于的继承层级.
     */
    private int level;

    /**
     * 关系信息.
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
     * entityField.
     */
    private List<EntityField> fields;

    /**
     * custom定制结构，
     * 目前需求驱动于用于租户定制，可支持多种定制.
     */
    private Map<String, ProfileStorage> profileStorageMap;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    /**
     * add ancestors.
     */
    public void addAncestors(Long ancestor) {
        if (null == this.ancestors) {
            this.ancestors = new ArrayList<>();
        }
        this.ancestors.add(ancestor);
    }

    public List<EntityField> getFields() {
        return fields;
    }

    /**
     * 获取一个EntityField.
     */
    public EntityField find(long id, String profile) {
        if (null != profile) {
            ProfileStorage profileStorage =
                profileStorageMap.get(profile);

            return null != profileStorage ? profileStorage.find(id) : null;
        } else {
            return fields.stream().filter(f -> f.id() == id).findFirst().orElse(null);
        }
    }

    public void setFields(List<EntityField> fields) {
        this.fields = fields;
    }

    public Map<String, ProfileStorage> getProfileStorageMap() {
        return profileStorageMap;
    }

    public void setProfileStorageMap(Map<String, ProfileStorage> profileStorageMap) {
        this.profileStorageMap = profileStorageMap;
    }
}
