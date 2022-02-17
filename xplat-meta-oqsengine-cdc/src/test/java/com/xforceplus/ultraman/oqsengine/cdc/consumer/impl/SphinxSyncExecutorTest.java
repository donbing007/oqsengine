package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.getStringFromColumn;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns.ATTRIBUTE;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractCDCTestHelper;
import com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools;
import com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class SphinxSyncExecutorTest extends AbstractCDCTestHelper {

    private long expectedId = 1001;
    private long expectedCommitId = 2;
    private long expectedTx = 11;
    private int expectedVersion = 10;
    private int expectedOqsMajor = 1;
    private String isDeleted = "false";
    private IEntityClass expectedEntityClass = EntityClassBuilder.ENTITY_CLASS_2;
    private int expectedLevel = 3;

    private Map<String, Object> expectedAttrs = new HashMap<>();
    private String expectedAttrString = "{\"1L\":73550,\"2S\":\"1\",\"3L\":0}";

    @BeforeEach
    public void before() throws Exception {
        super.init(true);

        expectedAttrs.put("1L", 73550);
        expectedAttrs.put("2S", "1");
        expectedAttrs.put("3L", 0);
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(true);
        expectedAttrs.clear();
    }

    @AfterAll
    public static void afterAll() {
        InitializationHelper.destroy();
    }

    public void checkAttrs(Map<String, Object> expected, Map<String, Object> actual) {
        Assertions.assertEquals(expected.size(), actual.size());

        expected.forEach(
            (exKey, exValue) -> {
                Assertions.assertEquals(exValue, actual.get(exKey));
            }
        );
    }

    @Test
    public void getEntityTest() throws Exception {
        SphinxSyncExecutor sphinxSyncExecutor =
            CdcInitialization.getInstance().sphinxSyncExecutor();

        Method getEntityMethod =
            sphinxSyncExecutor.getClass().getDeclaredMethod("getEntity", List.class);
        getEntityMethod.setAccessible(true);

        for (IEntityClass entityClass : EntityClassBuilder.entityClassMap.values()) {
            List<CanalEntry.Column> columns =
                CanalEntryTools.generateColumns(expectedId, expectedLevel, expectedCommitId, expectedTx, expectedVersion,
                    expectedAttrString, expectedOqsMajor, isDeleted, System.currentTimeMillis(), System.currentTimeMillis(), entityClass.id());

            long id =
                (long) getEntityMethod.invoke(sphinxSyncExecutor, columns);

            Assertions.assertEquals(entityClass.id(), id);
        }
    }

    @Test
    public void getEntityClassTest() throws Exception {
        SphinxSyncExecutor sphinxSyncExecutor =
            CdcInitialization.getInstance().sphinxSyncExecutor();

        Method getEntityClassMethod =
            sphinxSyncExecutor.getClass().getDeclaredMethod("getEntityClass", long.class, Map.class, List.class);
        getEntityClassMethod.setAccessible(true);

        List<CanalEntry.Column> columns =
            CanalEntryTools.generateColumns(expectedId, expectedLevel, expectedCommitId, expectedTx, expectedVersion,
                expectedAttrString, expectedOqsMajor, isDeleted, System.currentTimeMillis(), System.currentTimeMillis(), expectedEntityClass.id());

        Map<Long, IEntityClass> entityClassMap = new HashMap<>();
        entityClassMap.put(expectedEntityClass.id(), expectedEntityClass);

        IEntityClass entityClass = (IEntityClass)
            getEntityClassMethod.invoke(sphinxSyncExecutor, expectedId, entityClassMap, columns);

        Assertions.assertEquals(expectedEntityClass.id(), entityClass.id());
    }

    @Test
    public void attrCollectionTest() throws Exception {
        SphinxSyncExecutor sphinxSyncExecutor =
            CdcInitialization.getInstance().sphinxSyncExecutor();

        Method attrCollectionMethod =
            sphinxSyncExecutor.getClass().getDeclaredMethod("attrCollection", long.class, List.class);
        attrCollectionMethod.setAccessible(true);

        List<CanalEntry.Column> columns =
            CanalEntryTools.generateColumns(expectedId, expectedLevel, expectedCommitId, expectedTx, expectedVersion,
                expectedAttrString, expectedOqsMajor, isDeleted, System.currentTimeMillis(), System.currentTimeMillis(), expectedEntityClass.id());

        Map<String, Object> res =
            (Map<String, Object>) attrCollectionMethod.invoke(sphinxSyncExecutor, expectedId, columns);

        checkAttrs(expectedAttrs, res);
    }

    @Test
    public void prepareForUpdateDeleteTest() throws Exception {
        SphinxSyncExecutor sphinxSyncExecutor =
            CdcInitialization.getInstance().sphinxSyncExecutor();

        Method prepareForUpdateDeleteMethod =
            sphinxSyncExecutor.getClass().getDeclaredMethod("prepareForUpdateDelete",
                                        List.class, long.class, long.class, Map.class);
        prepareForUpdateDeleteMethod.setAccessible(true);

        List<CanalEntry.Column> columns =
            CanalEntryTools.generateColumns(expectedId, expectedLevel, expectedCommitId, expectedTx, expectedVersion,
                expectedAttrString, expectedOqsMajor, isDeleted,  System.currentTimeMillis(), System.currentTimeMillis(), expectedEntityClass.id());

        Map<Long, IEntityClass> entityClassMap = new HashMap<>();
        entityClassMap.put(expectedEntityClass.id(), expectedEntityClass);

        OriginalEntity entity =
            (OriginalEntity)
                prepareForUpdateDeleteMethod.invoke(sphinxSyncExecutor,
                    columns, expectedId, expectedCommitId, entityClassMap);

        Assertions.assertNotNull(entity);
        Assertions.assertEquals(expectedId, entity.getId());
        Assertions.assertEquals(expectedTx, entity.getTx());
        Assertions.assertEquals(expectedCommitId, entity.getCommitid());
        Assertions.assertEquals(expectedVersion, entity.getVersion());
        Assertions.assertEquals(expectedOqsMajor, entity.getOqsMajor());

        Assertions.assertEquals(isDeleted.equals("true"), entity.isDeleted());

        String attrStr = getStringFromColumn(columns, ATTRIBUTE);
        Assertions.assertFalse(StringUtils.isEmpty(attrStr));


        checkAttrs(expectedAttrs, entity.getAttributes());
    }

    @Test
    public void toClassKeyWithProfileTest() throws Exception {
        SphinxSyncExecutor sphinxSyncExecutor =
            CdcInitialization.getInstance().sphinxSyncExecutor();

        Method toClassKeyWithProfileMethod =
            sphinxSyncExecutor.getClass().getDeclaredMethod("toClassKeyWithProfile", long.class, String.class);
        toClassKeyWithProfileMethod.setAccessible(true);

        long id = Long.MAX_VALUE - 1;
        String profile = "JO_JO_JO_JO";

        String expected = id + "_" + profile;

        Assertions.assertEquals(expected, toClassKeyWithProfileMethod.invoke(sphinxSyncExecutor, id, profile));
    }


}
