package com.xforceplus.ultraman.oqsengine.sdk.store;

import com.google.common.collect.Maps;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.pojo.MapTableDataProvider;
import org.apache.metamodel.pojo.PojoDataContext;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.MutableTable;
import org.apache.metamodel.util.SimpleTableDef;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MetaStoreTest {


    @Test
    public void testRead(){


        List<Map<String, ?>> maps = new ArrayList<>();

        SimpleTableDef tableDef = new SimpleTableDef("test", new String[]{"foo", "bar"});
        MapTableDataProvider tableDataProvider = new MapTableDataProvider(tableDef, maps);
        MutableTable table = tableDef.toTable();
        PojoDataContext dc = new PojoDataContext("helloworld", tableDataProvider);

        IntStream.range(0, 100).mapToObj(x -> new Thread(() -> {
            Map<String, Object> record2 = Maps.newHashMap();
            record2.put("foo", 111);
            record2.put("bar", 222);
            dc.insert("test", record2);
        })).forEach(Thread::start);

        List<Thread> deletes = IntStream.range(0, 100).mapToObj(x -> {
            return new Thread(() -> {
                dc.executeUpdate(callback -> {
                    callback.deleteFrom(table).where("foo").eq(111).execute();
                }).getDeletedRows().ifPresent(System.out::println);
            });
        }).collect(Collectors.toList());;


        List<Thread> querys = IntStream.range(0, 100).mapToObj(x -> new Thread(() -> {
            Column column = table.getColumnByName("foo");
            DataSet ds =  dc.query().from(table).selectAll().execute();
            while(ds.next()){
                System.out.println(ds.getRow().getValue(column));
            }
        })).collect(Collectors.toList());


//        saves.forEach(Thread::start);
        deletes.forEach(Thread::start);
        querys.forEach(Thread::start);

        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
