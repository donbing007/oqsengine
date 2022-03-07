package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.CanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.CanalEntryGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.DynamicCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class BinLogParseUtilsTest {

    static CanalEntryCase expected = DynamicCanalEntryRepo.CASE_NORMAL_2;
    static List<CanalEntry.Column> columns;

    @BeforeAll
    public static void before() throws InvalidProtocolBufferException {
        CanalEntry.Entry entry = CanalEntryGenerator.buildRowDataEntry(expected, true);

        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

        columns = rowChange.getRowDatasList().get(0).getAfterColumnsList();
    }

    @Test
    public void oqsBigEntityColumnTest() {
        expected.assertionColumns(columns);
    }

    @Test
    public void getLongFromColumnTest() {
        Assertions.assertEquals(expected.getId(), BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ID));
        Assertions.assertEquals(expected.getId(), BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ID));
        Assertions.assertEquals(expected.getCommitId(), BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.COMMITID));
        Assertions.assertEquals(expected.getCreate(), BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.CREATETIME));
    }

    @Test
    public void getStringFromColumnTest() {
        Assertions.assertEquals(expected.getAttr(), BinLogParseUtils.getStringFromColumn(columns, OqsBigEntityColumns.ATTRIBUTE));
        Assertions.assertEquals(expected.getProfile(), BinLogParseUtils.getStringFromColumn(columns, OqsBigEntityColumns.PROFILE));
    }

    @Test
    public void getIntegerFromColumnTest() {
        Assertions.assertEquals(expected.getVersion(), BinLogParseUtils.getIntegerFromColumn(columns, OqsBigEntityColumns.VERSION));
        Assertions.assertEquals(expected.getOp(), BinLogParseUtils.getIntegerFromColumn(columns, OqsBigEntityColumns.OP));
        Assertions.assertEquals(expected.getOqsmajor(), BinLogParseUtils.getIntegerFromColumn(columns, OqsBigEntityColumns.OQSMAJOR));
    }

    @Test
    public void getBooleanFromColumnTest() {
        Assertions.assertEquals(expected.isDeleted(), BinLogParseUtils.getBooleanFromColumn(columns, OqsBigEntityColumns.DELETED));
    }
}
