package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.AttachmentCondition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.strategy.condition.select.AttachmentConditionBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 条件构造器工厂测试.
 *
 * @author dongbin
 * @version 0.1 2021/11/25 17:06
 * @since 1.8
 */
public class SphinxQLConditionQueryBuilderFactoryTest {

    @Test
    public void testAttachmentCondition() throws Exception {
        AttachmentCondition condition = new AttachmentCondition(EntityField.ID_ENTITY_FIELD, true, "123");
        SphinxQLConditionQueryBuilderFactory factory = new SphinxQLConditionQueryBuilderFactory();
        factory.init();

        AbstractSphinxQLConditionBuilder builder = factory.getQueryBuilder(condition, true);
        Assertions.assertEquals(AttachmentConditionBuilder.class, builder.getClass());


        condition = new AttachmentCondition(EntityField.ID_ENTITY_FIELD, false, "123");
        builder = factory.getQueryBuilder(condition, true);
        Assertions.assertEquals(AttachmentConditionBuilder.class, builder.getClass());
    }
}