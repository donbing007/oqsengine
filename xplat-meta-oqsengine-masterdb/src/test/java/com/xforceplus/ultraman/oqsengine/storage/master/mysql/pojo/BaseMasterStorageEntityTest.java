package com.xforceplus.ultraman.oqsengine.storage.master.mysql.pojo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试获取真实类型ID.
 *
 * @author dongbin
 * @version 0.1 2021/05/19 15:20
 * @since 1.8
 */
public class BaseMasterStorageEntityTest {

    @Test
    public void getSelfEntityClassId() {
        BaseMasterStorageEntity entity = new BaseMasterStorageEntity();
        entity.setEntityClasses(new long[] {
            1, 2, 3, 0, 0
        });
        Assertions.assertEquals(3, entity.getSelfEntityClassId());

        entity = new BaseMasterStorageEntity();
        entity.setEntityClasses(new long[] {
            1, 2, 3, 4, 5
        });
        Assertions.assertEquals(5, entity.getSelfEntityClassId());

        entity = new BaseMasterStorageEntity();
        entity.setEntityClasses(new long[] {
            0
        });
        Assertions.assertEquals(0, entity.getSelfEntityClassId());

        entity = new BaseMasterStorageEntity();
        entity.setEntityClasses(new long[] {
            0, 0
        });
        Assertions.assertEquals(0, entity.getSelfEntityClassId());
    }
}