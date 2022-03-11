package com.xforceplus.ultraman.oqsengine.cdc.testhelp.entry;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.DynamicCanalEntryGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.StaticCanalEntryGenerator;
import io.vavr.Tuple2;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class CanalEntryBuilder {

    public static List<CanalEntry.Entry> initAll(List<DynamicCanalEntryCase> dynamics, List<Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase>> statics) {
        List<CanalEntry.Entry> entries = new ArrayList<>();
        for (DynamicCanalEntryCase dynamicCanalEntryCase : dynamics) {
            entries.add(DynamicCanalEntryGenerator.buildRowDataEntry(dynamicCanalEntryCase));
            entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
        }

        for (Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> tuple : statics) {
            entries.add(DynamicCanalEntryGenerator.buildRowDataEntry(tuple._1()));
            entries.add(StaticCanalEntryGenerator.buildRowDataEntry(tuple._2()));
            entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
        }

        return entries;
    }

    public static List<CanalEntry.Entry> initExceptLastStatic(List<DynamicCanalEntryCase> dynamics, List<Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase>> statics) {
        List<CanalEntry.Entry> entries = new ArrayList<>();
        for (DynamicCanalEntryCase entryCase : dynamics) {
            entries.add(DynamicCanalEntryGenerator.buildRowDataEntry(entryCase));
            entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
        }

        for (int i = 0; i < statics.size(); i++) {
            entries.add(DynamicCanalEntryGenerator.buildRowDataEntry(statics.get(i)._1()));
            if (i < statics.size() - 1) {
                entries.add(StaticCanalEntryGenerator.buildRowDataEntry(statics.get(i)._2()));
                entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
            }
        }

        return entries;
    }

    public static List<CanalEntry.Entry> initOverBatchStatic(List<Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase>> statics) {
        List<CanalEntry.Entry> entries = new ArrayList<>();
        for (int i = 0; i < statics.size(); i++) {
            if (i == statics.size() - 1) {
                entries.add(StaticCanalEntryGenerator.buildRowDataEntry(statics.get(i)._2()));
                entries.add(DynamicCanalEntryGenerator.buildTransactionEndEntry());
            }
        }
        return entries;
    }
}
