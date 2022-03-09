package com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.meta.MockStaticColumns;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.Tuple2;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class StaticCanalEntryGenerator {
    private static final String MOCK_STATIC_TABLE = "oqs_business";

    /**
     * 创建数据行.
     */
    public static CanalEntry.Entry buildRowDataEntry(StaticCanalEntryCase caseEntry) {
        CanalEntry.Entry.Builder builder = CanalEntryGenerator.buildRowData(MOCK_STATIC_TABLE);
        builder.setStoreValue(buildRowChange(caseEntry).toByteString());

        return builder.build();
    }

    public static CanalEntry.RowChange buildRowChange(StaticCanalEntryCase caseEntry) {
        CanalEntry.RowChange.Builder builder = CanalEntry.RowChange.newBuilder();

        CanalEntry.EventType eventType = CanalEntry.EventType.UPDATE;
        builder.setEventType(eventType);

        builder.addRowDatas(buildRowData(caseEntry));

        return builder.build();
    }


    private static CanalEntry.RowData buildRowData(StaticCanalEntryCase caseEntry) {
        CanalEntry.RowData.Builder builder = CanalEntry.RowData.newBuilder();
        for (IEntityField f : MockStaticColumns.columns) {
            CanalEntry.Column column = buildColumn(caseEntry, f.name());
            if (null != column) {
                builder.addAfterColumns(column);
            }
        }

        return builder.build();
    }

    private static CanalEntry.Column buildColumn(StaticCanalEntryCase caseEntry, String key) {
        Tuple2<IEntityField, Object> v = caseEntry.getContext().get(key);

        CanalEntry.Column.Builder builder = getBuilder(key);
        builder.setValue(toStringValue(v));
        return builder.build();
    }

    private static CanalEntry.Column.Builder getBuilder(String name) {
        CanalEntry.Column.Builder builder = CanalEntry.Column.newBuilder();
        builder.setName(name.toLowerCase());
        return builder;
    }

    private static String toStringValue(Tuple2<IEntityField, Object> tuple2) {
        switch (tuple2._1().type()) {
            case LONG:
                return Long.toString((Long) tuple2._2());
            case BOOLEAN:
                return Boolean.toString((Boolean) tuple2._2());
            default: {
                return (String) tuple2._2();
            }
        }
    }
}
