package com.xforceplus.ultraman.oqsengine.calculation.helper;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * lookup key帮助工具测试.
 *
 * @author dongbin
 * @version 0.1 2021/08/09 10:25
 * @since 1.8
 */
public class LookupHelperTest {

    @Test
    public void testBuildLookupKey() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity().withId(Long.MAX_VALUE).build();
        IEntityField targetField = EntityField.CREATE_TIME_FILED;
        IEntity lookupEntity = Entity.Builder.anEntity().withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(Integer.MAX_VALUE).build())
            .build();

        String key = LookupHelper.buildLookupLinkKey(targetEntity, targetField, lookupEntity);
        Assert.assertEquals(
            String.format("l-%s-%s-%s-%s",
                NumberUtils.zeroFill(targetField.id()),
                NumberUtils.zeroFill(targetEntity.id()),
                NumberUtils.zeroFill(lookupEntity.entityClassRef().getId()),
                NumberUtils.zeroFill(lookupEntity.id())), key);
    }
}