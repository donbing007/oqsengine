package com.xforceplus.ultraman.oqsengine.calculation.helper;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.EntityClass;
import org.junit.jupiter.api.Assertions;
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
        IEntityField targetField = EntityField.CREATE_TIME_FILED;
        IEntity lookupEntity = Entity.Builder.anEntity().withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(Integer.MAX_VALUE).build())
            .build();

        String key = LookupHelper.buildLookupLinkKey(targetField, lookupEntity);
        Assertions.assertEquals(
            String.format("%s-%s%s-%s%s-%s%s", LookupHelper.LINK_KEY_PREFIX,
                LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX,
                NumberUtils.zeroFill(lookupEntity.entityClassRef().getId()),
                LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField.id()),
                LookupHelper.LINK_KEY_LOOKUP_ENTITY_PREFIX, NumberUtils.zeroFill(lookupEntity.id())
            ), key);
    }

    @Test
    public void testBuildIteratorKey() throws Exception {
        IEntityField targetField = EntityField.CREATE_TIME_FILED;
        IEntityClass lookupEntityClass = EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build();

        String key = LookupHelper.buildIteratorPrefixLinkKey(targetField, lookupEntityClass);
        Assertions.assertEquals(
            String.format(
                "%s-%s%s-%s%s",
                LookupHelper.LINK_KEY_PREFIX,
                LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX, NumberUtils.zeroFill(lookupEntityClass.id()),
                LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField.id())
            ),
            key
        );
    }
}