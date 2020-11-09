package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.ByteString;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.ConsumerService;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.enums.OqsBigEntityColumns;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;


/**
 * desc :
 * name : SphinxConsumerToolsTest
 *
 * @author : xujia
 * date : 2020/11/6
 * @since : 1.8
 */
public class SphinxConsumerToolsTest extends AbstractContainer {

    private static final int attrMaxSize = 3;

    private static final long PCREF_ID = 0;


    private ConsumerService sphinxConsumerService;

    @Before
    public void before() throws SQLException, InterruptedException {
        sphinxConsumerService = initConsumerService();
    }

    @Test
    public void columnToolsTest() throws Exception {

        int batchSize = 20;

        Set<Long> expectedIds = new HashSet<>();

        List<CanalEntry.Entry> entries = initData(batchSize, expectedIds);

        Method mLong = sphinxConsumerService.getClass().getDeclaredMethod("getLongFromColumn", new Class[]{List.class, OqsBigEntityColumns.class});
        mLong.setAccessible(true);

        Method mString = sphinxConsumerService.getClass().getDeclaredMethod("getStringFromColumn", new Class[]{List.class, OqsBigEntityColumns.class});
        mString.setAccessible(true);

        Method mBoolean = sphinxConsumerService.getClass().getDeclaredMethod("getBooleanFromColumn", new Class[]{List.class, OqsBigEntityColumns.class});
        mBoolean.setAccessible(true);

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
                    Long id = (Long) mLong.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.ID});
                    Assert.assertTrue(expectedIds.contains(id));

                    Long entity = (Long) mLong.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITY});
                    Assert.assertNotNull(entity);

                    Long tx = (Long) mLong.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.TX});
                    Assert.assertNotNull(tx);

                    Long commitid = (Long) mLong.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.COMMITID});
                    Assert.assertNotNull(commitid);

                    Long pref = (Long) mLong.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.PREF});
                    Assert.assertEquals(PCREF_ID, (long) pref);

                    Long cref = (Long) mLong.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.CREF});
                    Assert.assertEquals(PCREF_ID, (long) cref);

                    Boolean deleted = (Boolean) mBoolean.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.DELETED});
                    Assert.assertNotNull(deleted);

                    String attr = (String) mString.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.ATTRIBUTE});
                    Assert.assertNotNull(attr);

                    String meta = (String) mString.invoke(sphinxConsumerService, new Object[]{rowData.getAfterColumnsList(), OqsBigEntityColumns.META});
                    Assert.assertNotNull(meta);
                }
            }
        }
    }

    private List<CanalEntry.Entry> initData(int size, Set<Long> expectedIds) {
        long tx = 0;
        List<CanalEntry.Entry> entries = new ArrayList<>();

        for (int i = 0; i < size; i ++) {
            CanalEntry.Entry.Builder builder = CanalEntry.Entry.newBuilder();
            if (0 == i || i == size / 2 + 1 ) {
                builder.setEntryType(CanalEntry.EntryType.TRANSACTIONBEGIN);
                tx ++;
                entries.add(builder.build());
                continue;
            } else if (i == size - 1 || i == size / 2 ) {
                builder.setEntryType(CanalEntry.EntryType.TRANSACTIONEND);
                entries.add(builder.build());
                continue;
            }

            builder.setEntryType(CanalEntry.EntryType.ROWDATA);

            builder.setHeader(buildHeader());

            builder.setStoreValue(buildRowChange(i, i % 2 == 0, tx, size).toByteString());

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

    private CanalEntry.RowChange buildRowChange(long id, boolean replacement, long tx, int size) {
        CanalEntry.RowChange.Builder builder = CanalEntry.RowChange.newBuilder();

        CanalEntry.EventType eventType = replacement ? CanalEntry.EventType.UPDATE : CanalEntry.EventType.INSERT;
        builder.setEventType(eventType);

        builder.addRowDatas(buildRowData(id, tx, size));

        return builder.build();
    }

    private CanalEntry.RowData buildRowData(long id, long tx, int size) {
        int attrId = Math.abs(new Random(id).nextInt());
        CanalEntry.RowData.Builder builder = CanalEntry.RowData.newBuilder();
        for (OqsBigEntityColumns v : OqsBigEntityColumns.values()) {
            CanalEntry.Column column = buildColumn(id, v, attrId, tx, size);
            if (null != column) {
                builder.addAfterColumns(column);
            }
        }

        return builder.build();
    }

    private CanalEntry.Column buildColumn(long id, OqsBigEntityColumns v, int attrId, long tx, int size) {

        switch (v) {
            case ID:
                return buildId(id, v);
            case ENTITY:
                return buildEntity(v, size);
            case PREF:
            case CREF:
                return buildPCREF(v);
            case TX:
                return buildTX(v, tx);
            case COMMITID:
                return buildCommitid(v, tx);
            case DELETED:
                return buildDeleted(v);
            case ATTRIBUTE:
                return buildAttribute(v, attrId);
            case META:
                return buildMeta(v, attrId);
        }

        return null;
    }
    private CanalEntry.Column.Builder getBuilder(OqsBigEntityColumns v) {
        CanalEntry.Column.Builder builder = CanalEntry.Column.newBuilder();
        builder.setIndex(v.ordinal());
        builder.setName(v.name().toLowerCase());
        return builder;
    }

    private CanalEntry.Column buildId(long id, OqsBigEntityColumns v) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(id));

        return builder.build();
    }

    private CanalEntry.Column buildEntity(OqsBigEntityColumns v, int batchSize) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        Random r = new Random();
        long id = Math.abs(r.nextLong()) % batchSize;
        builder.setValue(Long.toString(id));

        return builder.build();
    }

    private CanalEntry.Column buildPCREF(OqsBigEntityColumns v) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(PCREF_ID));
        return builder.build();
    }

    private CanalEntry.Column buildTX(OqsBigEntityColumns v, long tx) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(tx));
        return builder.build();
    }

    private CanalEntry.Column buildCommitid(OqsBigEntityColumns v, long tx) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(tx));
        return builder.build();
    }

    private CanalEntry.Column buildDeleted(OqsBigEntityColumns v) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue("false");
        return builder.build();
    }

    private CanalEntry.Column buildAttribute(OqsBigEntityColumns v, int attrId) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        int id = attrId % attrMaxSize;
        builder.setValue(Prepared.attrs[id]);
        return builder.build();
    }

    private CanalEntry.Column buildMeta(OqsBigEntityColumns v, int metaId) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        int id = metaId % attrMaxSize;
        builder.setValue(Prepared.metas[id]);
        return builder.build();
    }


    private static class Prepared {
        static String[] attrs = {
                "{\"8194L\":73550,\"100000S0\":\"1\",\"100000S1\":\"2\",\"8192L\":38478,\"100000S2\":\"3\",\"100000S3\":\"500002\",\"100000S4\":\"测试\",\"8193S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\"}",
                "{\"12289S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\",\"100000S0\":\"1\",\"100000S1\":\"2\",\"100000S2\":\"3\",\"12288S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\",\"100000S3\":\"500002\",\"100000S4\":\"测试\",\"12290S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\"}",
                "{\"258048S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\",\"100000S0\":\"1\",\"100000S1\":\"2\",\"100000S2\":\"3\",\"258049S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\",\"100000S3\":\"500002\",\"100000S4\":\"测试\",\"258050S\":\"121110122981141101211039910211111912211011699113114103115101103122109109106109114111101\"}"
        };
        static String[] metas = {
                "[\"8194-Long\",\"100000-Strings\",\"8192-Long\",\"8193-String\"]",
                "[\"12290-String\",\"12288-String\",\"100000-Strings\",\"12289-String\"]",
                "[\"258048-String\",\"100000-Strings\",\"258049-String\",\"258050-String\"]"
        };
    }

}
