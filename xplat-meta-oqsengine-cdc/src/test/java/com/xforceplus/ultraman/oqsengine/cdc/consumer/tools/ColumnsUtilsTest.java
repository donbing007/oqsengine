package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.StaticCanalEntryGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.StaticCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.Tuple2;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class ColumnsUtilsTest {

    static Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> expected = StaticCanalEntryRepo.CASE_STATIC_ALL_IN_ONE;
    static List<CanalEntry.Column> columns;

    @BeforeAll
    public static void before() throws InvalidProtocolBufferException {
        CanalEntry.Entry entry = StaticCanalEntryGenerator.buildRowDataEntry(expected._2());

        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

        columns = rowChange.getRowDatasList().get(0).getAfterColumnsList();
    }

    /**
     * 这个测试中目前的dateTime类型仅支持 DateTimeFormatter.ISO_DATE_TIME类型的这3种格式.
     * 比如 ：
     *      '2011-12-03T10:15:30'.
     *      '2011-12-03T10:15:30+01:00'.
     *      '2011-12-03T10:15:30+01:00'.
     *
     */
    @Test
    public void columnsCheck() {
        for (Map.Entry<String, Tuple2<IEntityField, Object>> entry : expected._2().getContext().entrySet()) {
            Object actual = ColumnsUtils.execute(columns, entry.getKey(), entry.getValue()._1().type());

            check(entry.getValue()._2(), actual, entry.getValue()._1());
        }
    }

    public static void check(Object expected, Object actual, IEntityField entityField) {
        switch (entityField.type()) {
            case BOOLEAN:
                Assertions.assertEquals(expected, (Long) actual == 1);
                break;
            case DATETIME:
                Assertions.assertEquals(ColumnsUtils.toEpochMilli((String) expected), actual);
                break;
            default: {
                Assertions.assertEquals(expected, actual);
            }
        }
    }
}
