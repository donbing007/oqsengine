package com.xforceplus.ultraman.oqsengine.storage.master.pojo;

import org.junit.Assert;
import org.junit.Test;

/**
 * 测试获取真实类型ID.
 *
 * @author dongbin
 * @version 0.1 2021/05/19 15:20
 * @since 1.8
 */
public class MasterStorageEntityTest {

    @Test
    public void getSelfEntityClassId() {
        MasterStorageEntity entity = MasterStorageEntity.Builder.anStorageEntity()
            .withEntityClasses(new long[] {
                1, 2, 3, 0, 0
            }).build();
        Assert.assertEquals(3, entity.getSelfEntityClassId());

        entity = MasterStorageEntity.Builder.anStorageEntity()
            .withEntityClasses(new long[] {
                1, 2, 3, 4, 5
            }).build();
        Assert.assertEquals(5, entity.getSelfEntityClassId());

        entity = MasterStorageEntity.Builder.anStorageEntity()
            .withEntityClasses(new long[] {
                0
            }).build();
        Assert.assertEquals(0, entity.getSelfEntityClassId());

        entity = MasterStorageEntity.Builder.anStorageEntity()
            .withEntityClasses(new long[] {
                0, 0
            }).build();
        Assert.assertEquals(0, entity.getSelfEntityClassId());
    }
}