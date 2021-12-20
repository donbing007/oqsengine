package com.xforceplus.ultraman.oqsengine.storage.value;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 物理储存通用工具测试.
 */
public class AnyStorageValueTest {

    /**
     * 判断是否为普通属性名称.
     */
    @Test
    public void testCheckStorageName() throws Exception {
        Assertions.assertEquals(true, AnyStorageValue.isStorageValueName("9223372036854775805S"));
        Assertions.assertEquals(true, AnyStorageValue.isStorageValueName(
            AnyStorageValue.ATTRIBUTE_PREFIX + "9223372036854775805S"));
        Assertions.assertEquals(false, AnyStorageValue.isStorageValueName(
            AnyStorageValue.ATTACHMENT_PREFIX + "9223372036854775805S"));
    }

    @Test
    public void testCheckAttachmentStorgaeName() throws Exception {
        Assertions.assertEquals(false, AnyStorageValue.isAttachemntStorageName("9223372036854775805S"));
        Assertions.assertEquals(false, AnyStorageValue.isAttachemntStorageName(
            AnyStorageValue.ATTRIBUTE_PREFIX + "9223372036854775805S"));
        Assertions.assertEquals(true, AnyStorageValue.isAttachemntStorageName(
            AnyStorageValue.ATTACHMENT_PREFIX + "9223372036854775805S"));
    }
}