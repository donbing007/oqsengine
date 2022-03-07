package com.xforceplus.ultraman.oqsengine.cdc.testhelp;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CanalEntryGenerator {
    private static final String MOCK_DYNAMIC_TABLE = "oqsbigentity";
    private static final String MOCK_STATIC_TABLE = "oqs_business";

    /**
     * 创建数据行.
     */
    public static CanalEntry.Entry buildRowDataEntry(CanalEntryCase caseEntry, boolean isDynamic) {
        CanalEntry.Entry.Builder builder = buildRowData(isDynamic ? MOCK_DYNAMIC_TABLE : MOCK_STATIC_TABLE);
        builder.setStoreValue(buildRowChange(caseEntry).toByteString());

        return builder.build();
    }

    /**
     * 创建数据行.
     */
    public static CanalEntry.Entry buildRowDataEntry(CanalEntryCase caseEntry, String tableName) {
        CanalEntry.Entry.Builder builder = buildRowData(tableName);
        builder.setStoreValue(buildRowChange(caseEntry).toByteString());

        return builder.build();
    }

    /**
     * 设置记录头.
     */
    private static CanalEntry.Entry.Builder buildRowData(String tableName) {
        CanalEntry.Entry.Builder builder = CanalEntry.Entry.newBuilder();
        builder.setEntryType(CanalEntry.EntryType.ROWDATA);

        CanalEntry.Header.Builder headerBuilder = CanalEntry.Header.newBuilder().setExecuteTime(System.currentTimeMillis());
        headerBuilder.setTableName(tableName);

        builder.setHeader(headerBuilder.build());

        return builder;
    }

    /**
     * 创建数据行改变.
     */
    public static CanalEntry.RowChange buildRowChange(CanalEntryCase caseEntry) {
        CanalEntry.RowChange.Builder builder = CanalEntry.RowChange.newBuilder();

        CanalEntry.EventType eventType = caseEntry.isReplacement() ?
            CanalEntry.EventType.UPDATE : CanalEntry.EventType.INSERT;
        builder.setEventType(eventType);


        builder.addRowDatas(buildRowData(caseEntry));

        return builder.build();
    }


    private static CanalEntry.RowData buildRowData(CanalEntryCase caseEntry) {
        CanalEntry.RowData.Builder builder = CanalEntry.RowData.newBuilder();
        for (OqsBigEntityColumns v : OqsBigEntityColumns.values()) {
            CanalEntry.Column column = buildColumn(caseEntry, v);
            if (null != column) {
                builder.addAfterColumns(column);
            }
        }

        return builder.build();
    }


    /**
     * 创建字段.
     */
    public static CanalEntry.Column buildColumn(CanalEntryCase caseEntry, OqsBigEntityColumns v) {
        switch (v) {
            case ID:
                return buildId(caseEntry.getId(), v);
            case ENTITYCLASSL0:
            case ENTITYCLASSL1:
            case ENTITYCLASSL2:
            case ENTITYCLASSL3:
            case ENTITYCLASSL4:
                if (v.ordinal() == caseEntry.getLevelOrdinal()) {
                    return buildEntityClass(v, caseEntry.getEntityId());
                } else {
                    return getBuilder(v).setValue(Long.toString(0)).build();
                }
            case ENTITYCLASSVER:
                return buildVersion(v, 1);
            case OP:
                return buildOP(v, caseEntry.getOp());
            case TX:
                return buildTX(v, caseEntry.getTx());
            case COMMITID:
                return buildCommitid(v, caseEntry.getCommitId());
            case DELETED:
                return buildDeleted(v,  caseEntry.isDeleted() ? "1" : "0");
            case ATTRIBUTE:
                return buildAttribute(v, caseEntry.getAttr());
            case CREATETIME:
                return buildTime(v, caseEntry.getCreate());
            case UPDATETIME:
                return buildTime(v, caseEntry.getUpdate());
            case OQSMAJOR:
                return buildOqsmajor(v, caseEntry.getOqsmajor());
            case VERSION:
                return buildVersion(v, caseEntry.getVersion());
            case PROFILE:
                return buildProfile(v, caseEntry.getProfile());
            default:
                return null;
        }
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


    private static CanalEntry.Column buildAttribute(OqsBigEntityColumns v, String attrs) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(attrs);
        return builder.build();
    }

    private static CanalEntry.Column buildProfile(OqsBigEntityColumns v, String profile) {
        CanalEntry.Column.Builder builder = getBuilder(v);
        builder.setValue(profile);
        return builder.build();
    }
}
