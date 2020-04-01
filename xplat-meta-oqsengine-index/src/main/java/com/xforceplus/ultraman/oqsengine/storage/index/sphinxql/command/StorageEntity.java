package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.helper.SphinxQLHelper;

import java.util.HashSet;
import java.util.Set;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 3/27/2020 5:09 PM
 * 功能描述:
 * 修改历史:
 */
public class StorageEntity {
    private long id;
    private long entity;
    private long pref;
    private long cref;
    private JSONObject jsonFields;
    private Set<String> fullFields;

    public StorageEntity(long id, long entity, long pref, long cref, JSONObject jsonFields, Set<String> fullFields) {
        this.id = id;
        this.entity = entity;
        this.pref = pref;
        this.cref = cref;
        this.jsonFields = jsonFields;
        this.fullFields = setGlobalFlag(fullFields);
    }

    public long getId() {
        return id;
    }

    public long getEntity() {
        return entity;
    }

    public long getPref() {
        return pref;
    }

    public long getCref() {
        return cref;
    }

    public JSONObject getJsonFields() {
        return jsonFields;
    }

    public Set<String> getFullFields() {
        return fullFields;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setEntity(long entity) {
        this.entity = entity;
    }

    public void setPref(long pref) {
        this.pref = pref;
    }

    public void setCref(long cref) {
        this.cref = cref;
    }

    public void setJsonFields(JSONObject jsonFields) {
        this.jsonFields = jsonFields;
    }

    public void setFullFields(Set<String> fullFields) {
        this.fullFields = setGlobalFlag(fullFields);
    }

    private Set<String> setGlobalFlag(Set<String> fullfields) {
        if (fullfields == null) {
            return fullfields;
        }
        /**
         * 增加一个系统字段,当在查询所有数据的时候利用全文搜索引擎可以使用.
         */
        return new HashSet<String>(fullfields) {{
            add(SphinxQLHelper.ALL_DATA_FULL_TEXT);
        }};
    }
}
