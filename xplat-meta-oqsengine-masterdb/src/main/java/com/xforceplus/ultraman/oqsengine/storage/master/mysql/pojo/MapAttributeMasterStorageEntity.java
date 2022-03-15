package com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo;

import java.util.Map;

/**
 * 更新使用的实例.
 *
 * @author dongbin
 * @version 0.1 2021/12/23 14:40
 * @since 1.8
 */
public class MapAttributeMasterStorageEntity<K, V> extends BaseMasterStorageEntity {

    private Map<K, V> attributes;

    public Map<K, V> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<K, V> attributes) {
        this.attributes = attributes;
    }
}
