package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.StaticMasterDataInit;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * desc :
 * name : EntityValueBuildTest
 *
 * @author : xujia
 * date : 2020/11/9
 * @since : 1.8
 */
public class EntityValueBuildTest extends AbstractContainer {

    private ConsumerService sphinxConsumerService;

    private List<IEntity> expectedEntities = new ArrayList<>();

    private StaticMasterDataInit staticMasterDataInit = new StaticMasterDataInit();

    private List<StorageEntity> storageEntities;

    @Before
    public void before() throws Exception {

        initMaster();

        sphinxConsumerService = initConsumerService();

        expectedEntities = staticMasterDataInit.initData(masterStorage, transactionManager, 10);

        initStorageEntities();
    }

    @Test
    public void compareEntityValueTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = sphinxConsumerService.getClass()
                .getDeclaredMethod("buildEntityValue", new Class[]{Long.class, String.class, String.class});
        m.setAccessible(true);

        for (int i = 0; i < storageEntities.size(); i++) {
            StorageEntity se = storageEntities.get(i);
            IEntityValue entityValue = (IEntityValue) m.invoke(sphinxConsumerService, new Object[]{se.getId(), se.getMeta(), se.getAttribute()});
            Assert.assertNotNull(entityValue);

            Assert.assertEquals(entityValue.values().size(),
                    expectedEntities.get(i).entityValue().values().stream().filter(v -> v.getField().config().isSearchable()).count());

            final int index = i;
            entityValue.values().forEach(
                    value -> {
                         long fieldId = value.getField().id();
                         Object v = value.getValue();
                         Optional<IValue> f =
                                 expectedEntities.get(index).entityValue().values().stream().filter(ev -> ev.getField().id() == fieldId).findFirst();

                         Assert.assertTrue(f.isPresent());

                         Assert.assertEquals(v, f.get().getValue());
                    }
            );
        }
    }

    private void initStorageEntities() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method m1 = masterStorage.getClass()
                .getDeclaredMethod("toJson", new Class[]{IEntityValue.class});
        m1.setAccessible(true);

        Method m2 = masterStorage.getClass()
                .getDeclaredMethod("buildSearchAbleSyncMeta", new Class[]{IEntityClass.class});
        m2.setAccessible(true);

        storageEntities = new ArrayList<>();
        for (IEntity e : expectedEntities) {
            StorageEntity storageEntity = new StorageEntity();
            storageEntity.setId(e.id());
            storageEntity.setAttribute(
                    (String) m1.invoke(masterStorage, new Object[]{e.entityValue()}));

            storageEntity.setMeta(
                    (String) m2.invoke(masterStorage, new Object[]{e.entityClass()}));

            storageEntities.add(storageEntity);
        }
    }
}
