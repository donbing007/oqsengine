package com.xforceplus.ultraman.oqsengine.calculation.logic.lookup.helper;

import com.xforceplus.ultraman.oqsengine.common.NumberUtils;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
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
        IEntity targetEntity = Entity.Builder.anEntity().withId(Long.MAX_VALUE).build();
        IEntityField targetField = EntityField.CREATE_TIME_FILED;
        IEntity lookupEntity = Entity.Builder.anEntity().withId(Long.MAX_VALUE - 1)
            .withEntityClassRef(EntityClassRef.Builder.anEntityClassRef().withEntityClassId(Integer.MAX_VALUE).build())
            .build();
        IEntityField lookupField = EntityField.UPDATE_TIME_FILED;

        LookupHelper.LookupLinkKey key =
            LookupHelper.buildLookupLinkKey(targetEntity, targetField, lookupEntity, lookupField);
        Assertions.assertEquals(
            String.format("%s-%s%s-%s%s-%s%s-%s%s-%s%s", LookupHelper.LINK_KEY_PREFIX,
                LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField.id()),
                LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX,
                NumberUtils.zeroFill(lookupEntity.entityClassRef().getId()),
                LookupHelper.LINK_KEY_LOOKUP_FIELD_PREFIX, NumberUtils.zeroFill(lookupField.id()),
                LookupHelper.LINK_KEY_TARGET_ENTITY_PREFIX, NumberUtils.zeroFill(targetEntity.id()),
                LookupHelper.LINK_KEY_LOOKUP_ENTITY_PREFIX, NumberUtils.zeroFill(lookupEntity.id())
            ), key.toString());
    }

    @Test
    public void testBuildIteratorKey() throws Exception {
        IEntity targetEntity = Entity.Builder.anEntity().withId(Long.MAX_VALUE).build();
        IEntityField targetField = EntityField.CREATE_TIME_FILED;
        IEntityClass lookupEntityClass = EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build();
        IEntityField lookupField = EntityField.UPDATE_TIME_FILED;

        LookupHelper.LookupLinkIterKey key =
            LookupHelper.buildIteratorPrefixLinkKey(targetField, lookupEntityClass, lookupField, targetEntity);
        Assertions.assertEquals(
            String.format("%s-%s%s-%s%s-%s%s-%s%s", LookupHelper.LINK_KEY_PREFIX,
                LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField.id()),
                LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX, NumberUtils.zeroFill(lookupEntityClass.id()),
                LookupHelper.LINK_KEY_LOOKUP_FIELD_PREFIX, NumberUtils.zeroFill(lookupField.id()),
                LookupHelper.LINK_KEY_TARGET_ENTITY_PREFIX, NumberUtils.zeroFill(targetEntity.id())
            ),
            key.toString()
        );
    }

    @Test
    public void testParseKey() throws Exception {
        long targetField = 1L;
        long targetEntityId = 100L;
        long lookupClassId = 1000L;
        long lookupFieldId = 20L;
        long lookupEntityId = 32511L;


        String key = String.format("%s-%s%s-%s%s-%s%s-%s%s-%s%s", LookupHelper.LINK_KEY_PREFIX,
            LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField),
            LookupHelper.LINK_KEY_TARGET_ENTITY_PREFIX, NumberUtils.zeroFill(targetEntityId),
            LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX,
            NumberUtils.zeroFill(lookupClassId),
            LookupHelper.LINK_KEY_LOOKUP_FIELD_PREFIX, NumberUtils.zeroFill(lookupFieldId),
            LookupHelper.LINK_KEY_LOOKUP_ENTITY_PREFIX, NumberUtils.zeroFill(lookupEntityId)
        );

        LookupHelper.LookupLinkKey linkKey = LookupHelper.parseLinkKey(key);
        Assertions.assertEquals(targetField, linkKey.getTargetFieldId());
        Assertions.assertEquals(targetEntityId, linkKey.getTargetEntityId());
        Assertions.assertEquals(lookupClassId, linkKey.getLookupClassId());
        Assertions.assertEquals(lookupFieldId, linkKey.getLookupFieldId());
        Assertions.assertEquals(lookupEntityId, linkKey.getLookupEntityId());
    }

    @Test()
    public void testParseFailKey() throws Exception {
        long targetField = 1L;
        long targetEntityId = 100L;
        long lookupClassId = 1000L;
        long lookupFieldId = 20L;
        long lookupEntityId = 32511L;


        String key = String.format("%s-%s%s-%s%s-%s%s-%s%s", LookupHelper.LINK_KEY_PREFIX,
            LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField),
            LookupHelper.LINK_KEY_TARGET_ENTITY_PREFIX, NumberUtils.zeroFill(targetEntityId),
            LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX,
            NumberUtils.zeroFill(lookupClassId),
            LookupHelper.LINK_KEY_LOOKUP_FIELD_PREFIX, NumberUtils.zeroFill(lookupFieldId)
        );

        String finalKey = key;
        Assertions.assertThrows(IllegalArgumentException.class, () -> LookupHelper.parseLinkKey(finalKey));


        key = String.format("%s-%s%s-%s%s-%s%s-%s%s-%s%s", LookupHelper.LINK_KEY_PREFIX,
            LookupHelper.LINK_KEY_TARGET_FIELD_PREFIX, NumberUtils.zeroFill(targetField),
            LookupHelper.LINK_KEY_TARGET_ENTITY_PREFIX, NumberUtils.zeroFill(targetEntityId),
            LookupHelper.LINK_KEY_LOOKUP_ENTITYCLASS_PREFIX,
            NumberUtils.zeroFill(lookupClassId),
            LookupHelper.LINK_KEY_LOOKUP_FIELD_PREFIX, "abc",
            LookupHelper.LINK_KEY_LOOKUP_ENTITY_PREFIX, NumberUtils.zeroFill(lookupEntityId));

        String finalKey1 = key;
        Assertions.assertThrows(NumberFormatException.class, () -> LookupHelper.parseLinkKey(finalKey1));
    }
}