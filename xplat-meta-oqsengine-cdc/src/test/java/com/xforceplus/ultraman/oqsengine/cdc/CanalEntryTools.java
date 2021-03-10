package com.xforceplus.ultraman.oqsengine.cdc;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;

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

    public static CanalEntry.Entry buildRow(long id, int levelOrdinal, long entityId, boolean replacement, long tx, long commit, String isDeleted,
                                                        int attrIndex, int oqsmajor, int version) {
        CanalEntry.Entry.Builder builder = getEntryBuildByEntryType(CanalEntry.EntryType.ROWDATA);
        builder.setStoreValue(buildRowChange(id, levelOrdinal, entityId, replacement, tx, commit, isDeleted, attrIndex, oqsmajor, version).toByteString());
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


    public static CanalEntry.RowChange buildRowChange(long id, int levelOrdinal, long entityId, boolean replacement, long tx, long commit,
                                                String isDeleted, int attrIndex, int oqsmajor, int version) {
        CanalEntry.RowChange.Builder builder = CanalEntry.RowChange.newBuilder();

        CanalEntry.EventType eventType = replacement ? CanalEntry.EventType.UPDATE : CanalEntry.EventType.INSERT;
        builder.setEventType(eventType);

        int op = OperationType.DELETE.ordinal();
        if (isDeleted.equals("0")) {
            if (replacement) {
                op = OperationType.UPDATE.ordinal();
            } else {
                op = OperationType.CREATE.ordinal();
            }
        }
        builder.addRowDatas(buildRowData(id, levelOrdinal, entityId, tx, op, commit, isDeleted, attrIndex, oqsmajor, version));

        return builder.build();
    }

    private static CanalEntry.RowData buildRowData(long id, int levelOrdinal, long entityId, long tx, int op, long commit,
                                            String isDeleted, int attrIndex, int oqsmajor, int version) {

        CanalEntry.RowData.Builder builder = CanalEntry.RowData.newBuilder();
        for (OqsBigEntityColumns v : OqsBigEntityColumns.values()) {
            CanalEntry.Column column = buildColumn(id, v, levelOrdinal, entityId, tx, op, commit, isDeleted, attrIndex, oqsmajor, version);
            if (null != column) {
                builder.addAfterColumns(column);
            }
        }

        return builder.build();
    }

    public static CanalEntry.Column buildColumn(long id, OqsBigEntityColumns v, int levelOrdinal, long entityId, long tx, int op,
                                          long commit, String isDeleted, int attrIndex, int oqsmajor, int version) {
        switch (v) {
            case ID:
                return buildId(id, v);
            case ENTITYCLASSL0:
            case ENTITYCLASSL1:
            case ENTITYCLASSL2:
            case ENTITYCLASSL3:
            case ENTITYCLASSL4:
                if (v.ordinal() == levelOrdinal) {
                    return buildEntityClass(v, entityId);
                } else {
                    return getBuilder(v).setValue(Long.toString(0)).build();
                }
            case OP:
                return buildOP(v, op);
            case TX:
                return buildTX(v, tx);
            case COMMITID:
                return buildCommitid(v, commit);
            case DELETED:
                return buildDeleted(v, isDeleted);
            case ATTRIBUTE:
                return buildAttribute(v, attrIndex);
            case CREATETIME:
            case UPDATETIME:
                return buildTime(v, System.currentTimeMillis());
            case OQSMAJOR:
                return buildOqsmajor(v, oqsmajor);
            case VERSION:
                return buildVersion(v, version);
        }

        return null;
    }

    private static CanalEntry.Column buildEntityClass(OqsBigEntityColumns v, long father) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(father));
        return builder.build();
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

    private static CanalEntry.Column buildTX(OqsBigEntityColumns v, long tx) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(tx));
        return builder.build();
    }

    private static CanalEntry.Column buildOP(OqsBigEntityColumns v, int tx) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Integer.toString(tx));
        return builder.build();
    }

    private static CanalEntry.Column buildVersion(OqsBigEntityColumns v, int version) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Integer.toString(version));
        return builder.build();
    }

    private static CanalEntry.Column buildCommitid(OqsBigEntityColumns v, long commitId) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Long.toString(commitId));
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


    private static CanalEntry.Column buildAttribute(OqsBigEntityColumns v, int attrIndex) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(Prepared.attrs[attrIndex]);
        return builder.build();
    }

    public static class Prepared {
        public static String[] attrs = {
                "{\"1L\":73550,\"2S\":\"1\",\"3L\":\"0\"}",
                "{\"1L\":55304234,\"2S\":\"2222\",\"3L\":\"1\", \"4L\":12342354353412, \"5S0\":\"1\",\"5S1\":\"2\"}",
                "{\"1L\":55304234,\"2S\":\"2222\",\"3L\":\"1\", \"4L\":12342354353412, \"5S0\":\"1\",\"5S1\":\"2\", \"6S\":\"ENUM\", \"7S0\":\"1\",\"7S1\":\"2\",\"7S2\":\"3\", \"7S3\":\"500002\",\"7S4\":\"测试\"}"
        };
    }
}
