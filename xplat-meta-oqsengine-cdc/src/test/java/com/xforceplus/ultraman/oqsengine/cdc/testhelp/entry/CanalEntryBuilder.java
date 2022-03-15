package com.xforceplus.ultraman.oqsengine.cdc.testhelp.entry;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.DynamicCanalEntryGenerator;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CanalEntryBuilder {

    public static List<CanalEntry.Entry> initAll(List<DynamicCanalEntryCase> dynamics) {
        List<CanalEntry.Entry> entries = new ArrayList<>();
        for (DynamicCanalEntryCase dynamicCanalEntryCase : dynamics) {
            entries.add(DynamicCanalEntryGenerator.buildRowDataEntry(dynamicCanalEntryCase));
            entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
        }

        return entries;
    }

    public static Tuple2<List<CanalEntry.Entry>, CanalEntry.Entry> initOverBatch(List<DynamicCanalEntryCase> dynamics) {
        List<CanalEntry.Entry> entries = new ArrayList<>();

        for (int i = 0; i < dynamics.size(); i++) {
            entries.add(DynamicCanalEntryGenerator.buildRowDataEntry(dynamics.get(i)));
            if (i < dynamics.size() - 1) {
                entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
            }
        }

        return new Tuple2<>(entries, DynamicCanalEntryGenerator.buildTransactionEndEntry());
    }
}
