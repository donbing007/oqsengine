package com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo;

import java.util.Map;

/**
 * 更新使用的实例.
 *
 * @author dongbin
 * @version 0.1 2021/12/23 14:40
 * @since 1.8
 */
public class MapAttributeMasterStorageEntity extends BaseMasterStorageEntity {

    private Map<String, Object> attributes;

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
