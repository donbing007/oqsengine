package com.xforceplus.ultraman.oqsengine.cdc.consumer.tools;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools;
import com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder;
import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class BinLogParseUtilsTest {

    private long expectedId = 1001;
    private long expectedCommitId = 2;
    private long expectedTx = 11;
    private int expectedVersion = 10;
    private int expectedOqsMajor = 1;
    private String isDeleted = "false";
    private IEntityClass expectedEntityClass = EntityClassBuilder.ENTITY_CLASS_2;
    private int expectedLevel = 3;

    private String expectedAttrString = "{\"1L\":73550,\"2S\":\"1\",\"3L\":0}";

    private List<CanalEntry.Column> columns;

    public BinLogParseUtilsTest() throws InvalidProtocolBufferException {
        columns = CanalEntryTools.generateColumns(expectedId, expectedLevel, expectedCommitId, expectedTx,
                        expectedVersion, expectedAttrString, expectedOqsMajor,
                        isDeleted, expectedEntityClass.id(), System.currentTimeMillis(), System.currentTimeMillis());
    }

    @Test
    public void getLongFromColumnTest() throws SQLException {
        Assertions.assertEquals(0,
            BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ENTITYCLASSL0));

        Assertions.assertEquals(0,
            BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ENTITYCLASSL1));

        Assertions.assertEquals(EntityClassBuilder.ENTITY_CLASS_2.id(),
            BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ENTITYCLASSL2));

        Assertions.assertEquals(0,
            BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ENTITYCLASSL3));

        Assertions.assertEquals(0,
            BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.ENTITYCLASSL4));
    }

    @Test
    public void getIntegerFromColumnTest() throws SQLException {
        Assertions.assertEquals(expectedVersion,
            BinLogParseUtils.getLongFromColumn(columns, OqsBigEntityColumns.VERSION));
    }

    @Test
    public void getStringFromColumnTest() throws SQLException {
        Assertions.assertEquals(expectedAttrString,
            BinLogParseUtils.getStringFromColumn(columns, OqsBigEntityColumns.ATTRIBUTE));
    }

    @Test
    public void getBooleanFromColumnTest() {
        Assertions.assertEquals(isDeleted.equals("true"),
            BinLogParseUtils.getBooleanFromColumn(columns, OqsBigEntityColumns.DELETED));
    }

    @Test
    public void stringToBooleanTest() {
        String expectedStringTrue = "true";
        String expectedStringNumberTrue = "100";

        Assertions.assertTrue(BinLogParseUtils.stringToBoolean(expectedStringTrue));
        Assertions.assertTrue(BinLogParseUtils.stringToBoolean(expectedStringNumberTrue));

        String expectedStringFalse = "false";
        String expectedStringNumberFalseZero = "0";
        String expectedStringNumberFalse = "-1";

        Assertions.assertFalse(BinLogParseUtils.stringToBoolean(expectedStringFalse));
        Assertions.assertFalse(BinLogParseUtils.stringToBoolean(expectedStringNumberFalseZero));
        Assertions.assertFalse(BinLogParseUtils.stringToBoolean(expectedStringNumberFalse));
    }

    @Test
    public void getStringWithoutNullCheckTest() {
        String res = BinLogParseUtils.getStringWithoutNullCheck(columns, OqsBigEntityColumns.PROFILE);
        Assertions.assertTrue(StringUtils.isEmpty(res));

        res = BinLogParseUtils.getStringWithoutNullCheck(columns, OqsBigEntityColumns.ATTRIBUTE);
        Assertions.assertFalse(StringUtils.isEmpty(res));
    }

    @Test
    public void getColumnWithoutNullTest() throws SQLException {
        Assertions.assertNotNull(BinLogParseUtils.getColumnWithoutNull(columns, OqsBigEntityColumns.ATTRIBUTE));
    }
}
