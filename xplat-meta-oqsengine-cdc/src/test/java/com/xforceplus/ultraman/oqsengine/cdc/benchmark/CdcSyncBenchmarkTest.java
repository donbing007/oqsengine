package com.xforceplus.ultraman.oqsengine.cdc.benchmark;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.xforceplus.ultraman.oqsengine.cdc.AbstractContainer;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringsValue;

import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.command.StorageEntity;
import com.xforceplus.ultraman.oqsengine.storage.master.SQLMasterStorage;

import com.xforceplus.ultraman.oqsengine.storage.transaction.Transaction;

import org.junit.*;

import org.testcontainers.shaded.org.apache.commons.lang.time.StopWatch;


import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.EMPTY_BATCH_ID;

/**
 * desc :
 * name : CdcSyncBenchmarkTest
 *
 * @author : xujia
 * date : 2020/11/2
 * @since : 1.8
 */
public class CdcSyncBenchmarkTest extends AbstractContainer {

    int batchSize = 100;

    private IEntityField fixStringsField = new EntityField(100000, "strings", FieldType.STRINGS);
    private StringsValue fixStringsValue = new StringsValue(fixStringsField, "1,2,3,500002,测试".split(","));

    @Before
    public void before() throws Exception {
        initTransactionManager();

        initMaster();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(1L);

        initIndex();

        initData();

        // 等待加载完毕
        TimeUnit.SECONDS.sleep(2L);
    }

    private void initData() throws SQLException {
        Transaction tx = transactionManager.create();
        transactionManager.bind(tx.id());
        try {
            initData(masterStorage, batchSize);

            //将事务正常提交,并从事务管理器中销毁事务.
            tx.commit();
        } catch (Exception e) {
            //将事务正常提交,并从事务管理器中销毁事务.
            tx.rollback();
        } finally {
            transactionManager.finish(tx);
        }
    }

    @Test
    public void syncReplaceTest() {
        CanalConnector canalConnector = CanalConnectors.newSingleConnector(new InetSocketAddress("localhost",
                environment.getServicePort("canal-server_1", 11111)), "nly-v1", "root", "xplat");

        int emptyCount = 0;
        int count = 0;
        boolean isAck = false;
        try {
            canalConnector.connect();
            //监听的表，    格式为：数据库.表名,数据库.表名
            canalConnector.subscribe(".*\\..*");
            canalConnector.rollback();
            int totalEmptyCount = 120;
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            while (emptyCount < totalEmptyCount) {
                if (isAck) {
                    break;
                }
                Message message = canalConnector.getWithoutAck(batchSize);//获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == EMPTY_BATCH_ID || size == 0) {
                    emptyCount++;
                    System.out.println("empty count:" + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    count += printEntry(message.getEntries());
                    if (count >= batchSize) {
                        isAck = true;
                    }
                }
                canalConnector.ack(batchId);
            }
            stopWatch.stop();

            System.out.println("use time : " + stopWatch.getTime());
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            canalConnector.disconnect();
        }
    }

    private int printEntry(List<CanalEntry.Entry> entrys) throws SQLException {
        StopWatch stopWatch = new StopWatch();
        int count = 0;
        stopWatch.start();
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }
            CanalEntry.RowChange rowChange = null;
            try {
                rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());

            } catch (Exception e) {
            }

            CanalEntry.EventType eventType = rowChange.getEventType();


            for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                count++;
                if (eventType == CanalEntry.EventType.DELETE) {
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    replace(rowData.getAfterColumnsList());
                } else {
                    replace(rowData.getAfterColumnsList());
                }
            }
        }
        stopWatch.stop();
        System.out.println("stop watch printEntry " + stopWatch.getTime());
        return count;
    }


    private void replace(List<CanalEntry.Column> columns) throws SQLException {

        IEntityValue entityValue = new EntityValue(Long.parseLong(columns.get(1).getValue()));

        StorageEntity storageEntity = new StorageEntity(
                Long.parseLong(columns.get(0).getValue()),
                Long.parseLong(columns.get(1).getValue()),
                Long.parseLong(columns.get(7).getValue()),
                Long.parseLong(columns.get(8).getValue()),
                Long.parseLong(columns.get(2).getValue()),
                Long.parseLong(columns.get(3).getValue()), null, null);

        indexStorage.buildOrReplace(storageEntity, entityValue, true);
    }

    private static void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "   update=" + column.getUpdated());
        }
    }

    @After
    public void after() throws Exception {
        transactionManager.finish();

        dataSourcePackage.close();

    }

    // 初始化数据
    private List<IEntity> initData(SQLMasterStorage storage, int size) throws Exception {
        List<IEntity> expectedEntitys = new ArrayList<>(size);


        for (int i = 1; i <= size; i++) {
            expectedEntitys.add(buildEntity(i * size));
        }

        expectedEntitys.stream().forEach(e -> {
            try {
                storage.build(e);
            } catch (SQLException ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
        });

        return expectedEntitys;
    }

    private IEntity buildEntity(long baseId) {
        Collection<IEntityField> fields = buildRandomFields(baseId, 3);
        fields.add(fixStringsField);

        return new Entity(
                baseId,
                new EntityClass(baseId, "test", fields),
                buildRandomValue(baseId, fields)
        );
    }

    private Collection<IEntityField> buildRandomFields(long baseId, int size) {
        List<IEntityField> fields = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            long fieldId = baseId + i;
            fields.add(new EntityField(fieldId, "c" + fieldId,
                    ("c" + fieldId).hashCode() % 2 == 1 ? FieldType.LONG : FieldType.STRING));
        }

        return fields;
    }

    private IEntityValue buildRandomValue(long id, Collection<IEntityField> fields) {
        Collection<IValue> values = fields.stream().map(f -> {
            switch (f.type()) {
                case STRING:
                    return new StringValue(f, buildRandomString(30));
                case STRINGS:
                    return fixStringsValue;
                default:
                    return new LongValue(f, (long) buildRandomLong(10, 100000));
            }
        }).collect(Collectors.toList());

        EntityValue value = new EntityValue(id);
        value.addValues(values);
        return value;
    }

    private String buildRandomString(int size) {
        StringBuilder buff = new StringBuilder();
        Random rand = new Random(47);
        for (int i = 0; i < size; i++) {
            buff.append(rand.nextInt(26) + 'a');
        }
        return buff.toString();
    }

    private int buildRandomLong(int min, int max) {
        Random random = new Random();

        return random.nextInt(max) % (max - min + 1) + min;
    }
}
