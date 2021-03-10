package com.xforceplus.ultraman.oqsengine.cdc.consumer.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.ByteString;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.enums.OqsBigEntityColumns;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;
import java.util.*;

import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRow;
import static com.xforceplus.ultraman.oqsengine.cdc.CanalEntryTools.buildRowChange;
import static com.xforceplus.ultraman.oqsengine.cdc.EntityClassBuilder.entityClass2;
import static com.xforceplus.ultraman.oqsengine.cdc.consumer.tools.BinLogParseUtils.*;


/**
 * desc :
 * name : SphinxConsumerToolsTest
 *
 * @author : xujia
 * date : 2020/11/6
 * @since : 1.8
 */
public class SphinxConsumerToolsTest {

    @Test
    public void columnToolsTest() throws Exception {

        int batchSize = 20;

        Set<Long> expectedIds = new HashSet<>();

        List<CanalEntry.Entry> entries = initData(batchSize, expectedIds);

        for (CanalEntry.Entry e : entries) {
            if (e.getEntryType().equals(CanalEntry.EntryType.ROWDATA)) {
                CanalEntry.RowChange rowChange = null;
                ByteString byteString = e.getStoreValue();
                try {
                    rowChange = CanalEntry.RowChange.parseFrom(byteString);

                } catch (Exception ex) {
                    throw new SQLException(String.format("parse entry value failed, [%s], [%s]", byteString, ex));
                }

                for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                    Long id = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ID);
                    Assert.assertTrue(expectedIds.contains(id));

                    Long entity = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ENTITYCLASS2);
                    Assert.assertNotNull(entity);

                    Long tx = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.TX);
                    Assert.assertNotNull(tx);

                    Long commitid = getLongFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.COMMITID);
                    Assert.assertNotNull(commitid);

                    Boolean deleted = getBooleanFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.DELETED);
                    Assert.assertNotNull(deleted);

                    String attr = getStringFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.ATTRIBUTE);
                    Assert.assertNotNull(attr);

                    Integer op = getIntegerFromColumn(rowData.getAfterColumnsList(), OqsBigEntityColumns.OP);
                    Assert.assertNotNull(op);

                }
            }
        }
    }

    private List<CanalEntry.Entry> initData(int size, Set<Long> expectedIds) {
        long tx = 0;
        List<CanalEntry.Entry> entries = new ArrayList<>();

        for (int i = 0; i < size; i ++) {
            CanalEntry.Entry.Builder builder = CanalEntry.Entry.newBuilder();
            if (0 == i || i == size / 2 + 1 ) {
                builder.setEntryType(CanalEntry.EntryType.TRANSACTIONBEGIN);
                tx ++;
                entries.add(builder.build());
                continue;
            } else if (i == size - 1 || i == size / 2 ) {
                builder.setEntryType(CanalEntry.EntryType.TRANSACTIONEND);
                entries.add(builder.build());
                continue;
            }

            builder.setEntryType(CanalEntry.EntryType.ROWDATA);

            builder.setHeader(buildHeader());

            builder.setStoreValue(buildRowChange(i, 2, entityClass2.id(), i % 2 == 0, tx, 1, "0", 2, OqsVersion.MAJOR).toByteString());

            entries.add(builder.build());

            expectedIds.add(Long.parseLong(i + ""));
        }

        return entries;
    }

    private CanalEntry.Header buildHeader() {
        CanalEntry.Header.Builder builder = CanalEntry.Header.newBuilder();
        builder.setExecuteTime(System.currentTimeMillis() - 1024);
        return builder.build();
    }
}
