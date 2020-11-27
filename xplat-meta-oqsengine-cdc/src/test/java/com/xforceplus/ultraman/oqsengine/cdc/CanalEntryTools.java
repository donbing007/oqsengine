package com.xforceplus.ultraman.oqsengine.cdc;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;

import java.util.Random;

/**
 * desc :
 * name : CanalEntryTools
 *
 * @author : xujia
 * date : 2020/11/9
 * @since : 1.8
 */
public class CanalEntryTools {

    public static CanalEntry.Entry buildRow(long id, boolean replacement, long tx, long commit, String isDeleted,
                                                        long entityId, int attrIndex, long pref, long cref, int oqsmajor) {
        CanalEntry.Entry.Builder builder = getEntryBuildByEntryType(CanalEntry.EntryType.ROWDATA);
        builder.setStoreValue(buildRowChange(id, replacement, tx, commit, isDeleted, entityId, attrIndex, pref, cref, oqsmajor).toByteString());
        return builder.build();
    }

    public static CanalEntry.Entry buildTransactionStart() {
        return getEntryBuildByEntryType(CanalEntry.EntryType.TRANSACTIONBEGIN).build();
    }

    public static CanalEntry.Entry buildTransactionEnd() {
        return getEntryBuildByEntryType(CanalEntry.EntryType.TRANSACTIONEND).build();
    }

    private static CanalEntry.Entry.Builder getEntryBuildByEntryType(CanalEntry.EntryType entryType) {
        CanalEntry.Entry.Builder builder = CanalEntry.Entry.newBuilder();
        builder.setEntryType(entryType);
        builder.setHeader(CanalEntry.Header.newBuilder().setExecuteTime(System.currentTimeMillis()).build());

        return builder;
    }


    private static CanalEntry.RowChange buildRowChange(long id, boolean replacement, long tx, long commit,
                                                String isDeleted, long entityId, int attrIndex, long pref, long cref, int oqsmajor) {
        CanalEntry.RowChange.Builder builder = CanalEntry.RowChange.newBuilder();

        CanalEntry.EventType eventType = replacement ? CanalEntry.EventType.UPDATE : CanalEntry.EventType.INSERT;
        builder.setEventType(eventType);

        builder.addRowDatas(buildRowData(id, tx, commit, isDeleted, entityId, attrIndex, pref, cref, oqsmajor));

        return builder.build();
    }

    private static CanalEntry.RowData buildRowData(long id, long tx, long commit,
                                            String isDeleted, long entityId, int attrIndex, long pref, long cref, int oqsmajor) {
        int attrId = Math.abs(new Random(id).nextInt());
        CanalEntry.RowData.Builder builder = CanalEntry.RowData.newBuilder();
        for (OqsBigEntityColumns v : OqsBigEntityColumns.values()) {
            CanalEntry.Column column = buildColumn(id, v, attrId, tx, commit, isDeleted, entityId, attrIndex, pref, cref, oqsmajor);
            if (null != column) {
                builder.addAfterColumns(column);
            }
        }

        return builder.build();
    }

    private static CanalEntry.Column buildColumn(long id, OqsBigEntityColumns v, int attrId, long tx,
                                          long commit, String isDeleted, long entityId, int attrIndex, long pref, long cref, int oqsmajor) {
        switch (v) {
            case ID:
                return buildId(id, v);
            case ENTITY:
                return buildEntity(v, entityId);
            case PREF:
                return buildPREF(v, pref);
            case CREF:
                return buildCREF(v, cref);
            case TX:
                return buildTX(v, tx);
            case COMMITID:
                return buildCommitid(v, commit);
            case DELETED:
                return buildDeleted(v, isDeleted);
            case ATTRIBUTE:
                return buildAttribute(v, attrId, attrIndex);
            case META:
                return buildMeta(v, attrId, attrIndex);
            case TIME:
                return buildTime(v, System.currentTimeMillis());
            case OQSMAJOR:
                return buildOqsmajor(v, oqsmajor);
        }

        return null;
    }
    private static CanalEntry.Column.Builder getBuilder(OqsBigEntityColumns v) {
        CanalEntry.Column.Builder builder = CanalEntry.Column.newBuilder();
        builder.setIndex(v.ordinal());
        builder.setName(v.name().toLowerCase());
        return builder;
    }

    private static CanalEntry.Column buildId(long id, OqsBigEntityColumns v) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(id));

        return builder.build();
    }

    private static CanalEntry.Column buildEntity(OqsBigEntityColumns v, long entityId) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        Random r = new Random();
        builder.setValue(Long.toString(entityId));

        return builder.build();
    }

    private static CanalEntry.Column buildPREF(OqsBigEntityColumns v, long id) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(id));
        return builder.build();
    }

    private static CanalEntry.Column buildCREF(OqsBigEntityColumns v, long id) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(id));
        return builder.build();
    }

    private static CanalEntry.Column buildTX(OqsBigEntityColumns v, long tx) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(tx));
        return builder.build();
    }

    private static CanalEntry.Column buildCommitid(OqsBigEntityColumns v, long tx) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(tx));
        return builder.build();
    }

    private static CanalEntry.Column buildDeleted(OqsBigEntityColumns v, String isDeleted) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(isDeleted);
        return builder.build();
    }

    private static CanalEntry.Column buildTime(OqsBigEntityColumns v, long time) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(time));
        return builder.build();
    }

    private static CanalEntry.Column buildOqsmajor(OqsBigEntityColumns v, int oqsmajor) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Integer.toString(oqsmajor));
        return builder.build();
    }


    private static CanalEntry.Column buildAttribute(OqsBigEntityColumns v, int attrId, int attrIndex) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Prepared.attrs[attrIndex]);
        return builder.build();
    }

    private static CanalEntry.Column buildMeta(OqsBigEntityColumns v, int metaId, int attrIndex) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Prepared.metas[attrIndex]);
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
