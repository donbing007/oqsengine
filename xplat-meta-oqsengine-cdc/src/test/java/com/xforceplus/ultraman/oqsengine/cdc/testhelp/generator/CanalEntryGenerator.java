package com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator;

import com.alibaba.otter.canal.protocol.CanalEntry;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CanalEntryGenerator {

    /**
     * 设置记录头.
     */
    public static CanalEntry.Entry.Builder buildRowData(String tableName) {
        CanalEntry.Entry.Builder builder = CanalEntry.Entry.newBuilder();
        builder.setEntryType(CanalEntry.EntryType.ROWDATA);

        CanalEntry.Header.Builder headerBuilder =
            CanalEntry.Header.newBuilder().setExecuteTime(System.currentTimeMillis());

        headerBuilder.setTableName(tableName);

        builder.setHeader(headerBuilder.build());

        return builder;
    }
}
