package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.cdc.CDCAbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;
import com.xforceplus.ultraman.oqsengine.storage.master.define.StorageEntity;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.ContainerStarter;
import org.junit.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.xforceplus.ultraman.oqsengine.cdc.EntityGenerateToolBar.stringsField;


/**
 * desc :
 * name : EntityValueBuildTest
 *
 * @author : xujia
 * date : 2020/11/9
 * @since : 1.8
 */
public class EntityValueBuildTest extends CDCAbstractContainer {

    private ConsumerService sphinxConsumerService;

    private IEntity[] expectedEntities;

    private List<StorageEntity> storageEntities;

    private static final long partitionId = 100000;

    @BeforeClass
    public static void beforeClass() {
        ContainerStarter.startMysql();
        ContainerStarter.startManticore();
        ContainerStarter.startRedis();
        ContainerStarter.startCannal();
    }

    @Before
    public void before() throws Exception {

        sphinxConsumerService = initAll();

        expectedEntities = EntityGenerateToolBar.generateFixedEntities(partitionId, 0);

        initStorageEntities();
    }

    @After
    public void after() {
        closeAll();
    }

    @Test
    public void compareEntityValueTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = sphinxSyncExecutor.getClass()
                .getDeclaredMethod("buildEntityValue", new Class[]{Long.class, String.class, String.class});
        m.setAccessible(true);

        for (int i = 0; i < storageEntities.size(); i++) {
            StorageEntity se = storageEntities.get(i);
            IEntityValue entityValue = (IEntityValue) m.invoke(sphinxSyncExecutor, new Object[]{se.getId(), se.getMeta(), se.getAttribute()});
            Assert.assertNotNull(entityValue);

            Assert.assertEquals(entityValue.values().size(),
                    expectedEntities[i].entityValue().values().stream().filter(v -> v.getField().config().isSearchable()).count());

            final int index = i;
            entityValue.values().forEach(
                    value -> {
                         long fieldId = value.getField().id();
                         Object v = value.getValue();
                         Optional<IValue> f =
                                 expectedEntities[index].entityValue().values().stream().filter(ev -> ev.getField().id() == fieldId).findFirst();

                         Assert.assertTrue(f.isPresent());
                         if(v instanceof String[]) {

                             String[] vs =  (String[]) doNewStyleToLogicValue(stringsField, ((String[]) v)[0]).getValue();
                             String[] fTemp = (String[]) f.get().getValue();

                             Assert.assertEquals(vs.length, fTemp.length);
                             for (int j = 0; j < vs.length; j++) {
                                 Assert.assertEquals(vs[j], fTemp[j]);
                             }
                         } else {
                             Assert.assertEquals(v, f.get().getValue());
                         }
                    }
            );
        }
    }

    private IValue doNewStyleToLogicValue(IEntityField field, String value) {
        List<String> list = new ArrayList<>();
        StringBuffer buff = new StringBuffer();
        boolean watch = false;
        for (char v : value.toCharArray()) {
            if (v == '[') {
                watch = true;
            } else if (v == ']' && watch) {
                list.add(buff.toString());
                buff.delete(0, buff.length());
                watch = false;
            } else {
                buff.append(v);
            }
        }

        return new StringsValue(field, list.toArray(new String[0]));
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
                ((JSONObject) m1.invoke(masterStorage, new Object[]{e.entityValue()})).toJSONString());

            storageEntity.setMeta(
                ((JSONArray) m2.invoke(masterStorage, new Object[]{e.entityClass()})).toJSONString());

            storageEntities.add(storageEntity);
        }
    }
}
