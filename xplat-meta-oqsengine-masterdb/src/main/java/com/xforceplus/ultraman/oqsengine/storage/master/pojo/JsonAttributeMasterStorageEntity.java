package com.xforceplus.ultraman.oqsengine.storage.master.pojo;

/**
 * 构造一个包含JSON的储存实例.
 *
 * @author dongbin
 * @version 0.1 2021/12/23 14:34
 * @since 1.8
 */
public class JsonAttributeMasterStorageEntity extends BaseMasterStorageEntity {

    private String attribute;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
