package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.ByteString;
import com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools;
import com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRow;
import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRowChange;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.*;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;


/**
 * desc :
 * name : SphinxConsumerToolsTest
 *
 * @author : xujia
 * date : 2020/11/6
 * @since : 1.8
 */
public class SphinxConsumerToolsTest {

    private Method testGetEntityClass;
    private Method testAttrCollection;
    private Method testPrepareForUpdateDelete;

    private SphinxSyncExecutor sphinxSyncExecutor;

    private void initGetEntityClass() throws NoSuchMethodException {
        testGetEntityClass = sphinxSyncExecutor.getClass()
                .getDeclaredMethod("getEntityClass", new Class[]{long.class, List.class});
        testGetEntityClass.setAccessible(true);
    }

    private void initAttrCollection() throws NoSuchMethodException {
        testAttrCollection = sphinxSyncExecutor.getClass()
                .getDeclaredMethod("attrCollection", new Class[]{long.class, List.class});
        testAttrCollection.setAccessible(true);
    }

    private void initPrepareForUpdateDelete() throws NoSuchMethodException {
        testPrepareForUpdateDelete = sphinxSyncExecutor.getClass()
                .getDeclaredMethod("prepareForUpdateDelete", new Class[]{List.class, long.class, long.class});
        testPrepareForUpdateDelete.setAccessible(true);
    }

    public void init() throws NoSuchMethodException {
        EntityClassBuilder metaManager = new EntityClassBuilder();
        sphinxSyncExecutor = new SphinxSyncExecutor();
        ReflectionTestUtils.setField(sphinxSyncExecutor, "metaManager", metaManager);

        initGetEntityClass();
        initAttrCollection();
        initPrepareForUpdateDelete();
    }

    @Test
    public void rawEntryTest() throws Exception {
        init();
        for (int i = 0; i < entityClassMap.size(); i++) {
            long id = i + 1;
            long commitId = 1;
            CanalEntryTools.Case cas = new CanalEntryTools.Case(id, commitId);
            cas.withLevelOrdinal(1 + i)
                    .withEntityId(Long.MAX_VALUE - i)
                    .withReplacement(true)
                    .withTx(1)
                    .withDeleted(false)
                    .withAttr(i)
                    .withQqsmajor(1)
                    .withVersion(1)
                    .withCreate(System.currentTimeMillis())
                    .withUpdate(System.currentTimeMillis());

            CanalEntry.Entry entry = buildRow(cas, false);

            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                assertGetEntityClass(rowData.getAfterColumnsList());

                assertAttributes(rowData.getAfterColumnsList(), i);

                assertPrepareForUpdateDelete(rowData.getAfterColumnsList(), cas);
            }
        }
    }

    @Test
    public void columnToolsTest() throws Exception {

        int batchSize = 20;

        Set<Long> expectedIds = new HashSet<>();

        List<CanalEntry.Entry> entries = initData(batchSize, expectedIds);

        for (CanalEntry.Entry e : entries) {
            if (e.getEntryType().equals(CanalEntry.EntryType.ROWDATA)) {
                CanalEntry.RowChange rowChange = null;
                ByteString byteString = e.getStoreValue();
                try {
                    rowChange = CanalEntry.RowChange.parseFrom(byteString);

                } catch (Exception ex) {
                    throw new SQLException(String.format("parse entry value failed, [%s], [%s]", byteString, ex));
                }

                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    long id = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ID);
                    Assert.assertTrue(expectedIds.contains(id));

                    Boolean deleted = getBooleanFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.DELETED);
                    Assert.assertNotNull(deleted);

                    int op = getIntegerFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.OP);
                    Assert.assertTrue(op > 0);

                    int version = getIntegerFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.VERSION);
                    Assert.assertTrue(version > 0);

                    int oqsMajor = getIntegerFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.OQSMAJOR);
                    Assert.assertTrue(oqsMajor > 0);

                    long create = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.CREATETIME);
                    Assert.assertTrue(create > 0);

                    long update = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.UPDATETIME);
                    Assert.assertTrue(update > 0);

                    long tx = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.TX);
                    Assert.assertTrue(tx > 0);

                    long commitid = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.COMMITID);
                    Assert.assertTrue(commitid > 0);

                    long entity = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITYCLASSL0);
                    Assert.assertEquals(0, entity);

                    entity = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITYCLASSL1);
                    Assert.assertEquals(0, entity);

                    entity = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITYCLASSL2);
                    Assert.assertTrue(entity > 0);

                    entity = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITYCLASSL3);
                    Assert.assertEquals(0, entity);

                    entity = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITYCLASSL4);
                    Assert.assertEquals(0, entity);

                    String attr = getStringFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ATTRIBUTE);
                    Assert.assertTrue(null != attr && !attr.isEmpty());
                }
            }
        }
    }

    private void assertGetEntityClass(List<CanalEntry.Column> columns) throws Exception {
        IEntityClass entityClass = (IEntityClass) testGetEntityClass.invoke(sphinxSyncExecutor, getLongFromColumn(columns, OqsBigEntityColumns.ID), columns);
        Assert.assertNotNull(entityClass);
    }

    @SuppressWarnings("unchecked")
    private void assertAttributes(List<CanalEntry.Column> columns, int index) throws Exception {
        List<Object> objects = (List<Object>) testAttrCollection.invoke(sphinxSyncExecutor, getLongFromColumn(columns, OqsBigEntityColumns.ID), columns);
        Assert.assertTrue(null != objects && !objects.isEmpty() && objects.size() % 2 == 0);

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> kv = objectMapper.readValue(CanalEntryTools.Prepared.attrs[index], Map.class);


        kv.forEach(
                (k, v) -> {
                    boolean findKey = false;
                    for (int i = 0; i < objects.size(); i++) {
                        Object object = objects.get(i);
                        if (i % 2 == 0) {
                            Assert.assertEquals(String.class, object.getClass());
                            if (object.equals(k)) {
                                findKey = true;
                                Assert.assertEquals(v, objects.get(i + 1));
                                break;
                            }
                        }
                    }
                    Assert.assertTrue(findKey);
                }
        );
    }

    private void assertPrepareForUpdateDelete(List<CanalEntry.Column> columns, CanalEntryTools.Case caseEntry) throws InvocationTargetException, IllegalAccessException, SQLException {
        OriginalEntity originalEntity = (OriginalEntity) testPrepareForUpdateDelete.invoke(sphinxSyncExecutor, columns,
                getLongFromColumn(columns, OqsBigEntityColumns.ID), getLongFromColumn(columns, OqsBigEntityColumns.COMMITID));
        Assert.assertNotNull(originalEntity);
        assertByCaseEntry(caseEntry, originalEntity);
    }

    private void assertByCaseEntry(CanalEntryTools.Case caseEntry, OriginalEntity originalEntity) {
        Assert.assertEquals(caseEntry.getId(), originalEntity.getId());
        Assert.assertEquals(caseEntry.getOp(), originalEntity.getOp());
        Assert.assertEquals(caseEntry.isDeleted(), originalEntity.isDeleted());
        Assert.assertEquals(caseEntry.getVersion(), originalEntity.getVersion());
        Assert.assertEquals(caseEntry.getOqsmajor(), originalEntity.getOqsMajor());
        Assert.assertEquals(caseEntry.getCreate(), originalEntity.getCreateTime());
        Assert.assertEquals(caseEntry.getUpdate(), originalEntity.getUpdateTime());
        Assert.assertEquals(caseEntry.getTx(), originalEntity.getTx());
        Assert.assertEquals(caseEntry.getCommitId(), originalEntity.getCommitid());
        Assert.assertEquals(caseEntry.getEntityId(), originalEntity.getEntityClass().id());
    }


    private List<CanalEntry.Entry> initData(int size, Set<Long> expectedIds) {
        long tx = 0;
        List<CanalEntry.Entry> entries = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            CanalEntry.Entry.Builder builder = CanalEntry.Entry.newBuilder();
            if (0 == i || i == size / 2 + 1) {
                builder.setEntryType(CanalEntry.EntryType.TRANSACTIONBEGIN);
                tx++;
                entries.add(builder.build());
                continue;
            } else if (i == size - 1 || i == size / 2) {
                builder.setEntryType(CanalEntry.EntryType.TRANSACTIONEND);
                entries.add(builder.build());
                continue;
            }

            builder.setEntryType(CanalEntry.EntryType.ROWDATA);

            builder.setHeader(buildHeader());

            builder.setStoreValue(buildRowChange(i, 3, entityClass2.id(), i % 2 == 0, tx, 1, "0", 2, OqsVersion.MAJOR, 1, false).toByteString());

            entries.add(builder.build());

            expectedIds.add(Long.parseLong(i + ""));
        }

        return entries;
    }

    private CanalEntry.Header buildHeader() {
        CanalEntry.Header.Builder builder = CanalEntry.Header.newBuilder();
        builder.setExecuteTime(System.currentTimeMillis() - 1024);
        return builder.build();
    }
}
