package com.xforceplus.ultraman.oqsengine.storage.master;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.OqsEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.UniqueIndexValue;
import com.xforceplus.ultraman.oqsengine.storage.master.unique.impl.SimpleFieldKeyGenerator;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 2020/10/19 8:05 PM
 */
public class TestSimpleFieldKeyGenerator {

    private SimpleFieldKeyGenerator keyGenerator;
    private MetaManager metaManager;

    @BeforeEach
    public void before() {
        keyGenerator = new SimpleFieldKeyGenerator();
        metaManager = mock(MetaManager.class);
        IEntityClass entityClass = buildEntityClass(1008);
        when(metaManager.load(1008)).thenReturn(Optional.of(entityClass));
        ReflectionTestUtils.setField(keyGenerator,"metaManager",metaManager);

    }

    @Test
    public void testKeyGenerator() throws SQLException {
        IEntity entity = buildEntity(1008);
        Map<String, UniqueIndexValue> keys = keyGenerator.generator(entity);
        Assert.assertEquals(((UniqueIndexValue) keys.values().toArray()[0]).getValue(), "f2Value-f1Value");
    }

    @Test
    public void testKeyGenerator1() {
        IEntity entity = buildEntity(1008);
        BusinessKey key1 = new BusinessKey();
        key1.setFieldName("f1");
        key1.setValue("f1Value");
        BusinessKey key2 = new BusinessKey();
        key2.setFieldName("f2");
        key2.setValue("f2Value");
        List<BusinessKey> keys = new ArrayList<>();
        keys.add(key1);
        keys.add(key2);
        Map<String, UniqueIndexValue> resultKeys = keyGenerator.generator(keys, buildEntityClass(1008));
        Assert.assertEquals(((UniqueIndexValue) resultKeys.values().toArray()[0]).getValue(), "f2Value-f1Value");
    }



    private IEntity buildEntity(long baseId) {
        FieldConfig config = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U1:2,test:IDX_U2:1").build();
        FieldConfig config1 = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U1:1,test:IDX_U2:2").build();
        FieldConfig config2 = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U2:3").build();
        IEntityField f1 =
            EntityField.Builder.anEntityField().withId(100000).withName("f1").withFieldType(FieldType.STRINGS)
                .withConfig(config).build();
        IEntityField f2 =
            EntityField.Builder.anEntityField().withId(100001).withName("f2").withFieldType(FieldType.STRINGS)
                .withConfig(config1).build();
        IEntityField f3 =
            EntityField.Builder.anEntityField().withId(100003).withName("f3").withFieldType(FieldType.STRINGS).build();
        IEntityField f4 =
            EntityField.Builder.anEntityField().withId(100002).withName("f4").withFieldType(FieldType.STRINGS)
                .withConfig(config2).build();
        List<IEntityField> fields = new ArrayList<>();
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        fields.add(f4);
        return Entity.Builder.anEntity().withId(baseId).withEntityClassRef(new EntityClassRef(baseId, "test"))
            .withEntityValue(buildValue(fields)).build();
    }

    private IEntityClass buildEntityClass(long baseId) {
        FieldConfig config = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U1:2,test:IDX_U2:1").build();
        FieldConfig config1 = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U1:1,test:IDX_U2:2").build();
        FieldConfig config2 = FieldConfig.Builder.anFieldConfig().withUniqueName("test:IDX_U2:3").build();
        IEntityField f1 =
            EntityField.Builder.anEntityField().withId(100000).withName("f1").withFieldType(FieldType.STRINGS)
                .withConfig(config).build();
        IEntityField f2 =
            EntityField.Builder.anEntityField().withId(100001).withName("f2").withFieldType(FieldType.STRINGS)
                .withConfig(config1).build();
        IEntityField f3 =
            EntityField.Builder.anEntityField().withId(100003).withName("f3").withFieldType(FieldType.STRINGS).build();
        IEntityField f4 =
            EntityField.Builder.anEntityField().withId(100002).withName("f4").withFieldType(FieldType.STRINGS)
                .withConfig(config2).build();
        List<IEntityField> fields = new ArrayList<>();
        fields.add(f1);
        fields.add(f2);
        fields.add(f3);
        fields.add(f4);
        return OqsEntityClass.Builder.anEntityClass().withCode("test").withFields(fields).withId(baseId).build();
    }

    private List<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(new EntityField(fieldId, "c" + fieldId,
                ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }

    private IEntityValue buildValue(List<IEntityField> fields) {
        Collection<IValue> values = new ArrayList<>();
        values.add(new StringValue(fields.get(0), "f1Value"));
        values.add(new StringValue(fields.get(1), "f2Value"));
        values.add(new StringValue(fields.get(2), "f3Value"));
        values.add(new StringValue(fields.get(3), "f4Value"));
        IEntityValue value = EntityValue.build();
        value.addValues(values);
        return value;
    }
}
