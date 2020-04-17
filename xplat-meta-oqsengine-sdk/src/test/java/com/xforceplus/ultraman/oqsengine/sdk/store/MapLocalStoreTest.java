package com.xforceplus.ultraman.oqsengine.sdk.store;

import org.apache.metamodel.data.Row;
import org.junit.Test;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;


public class MapLocalStoreTest {

    @Test
    public void nonVersionTest(){
        /**
         * String schema, String tableName
         * String[] columns, String[] pkColumns
         * ,boolean hasVersion, Comparator<Object> versionComparator
         */
        MapLocalStore mapLocalStore = new MapLocalStore("test"
                , "test", new String[]{"column1" , "column2"}
                , new String[]{}, false, null);

        Map<String, Object> map = new HashMap<>();
        map.put("column1", 123);
        map.put("column2", 1234);
        mapLocalStore.save(map);


        List<Row> rows = mapLocalStore.query().selectAll().execute().toRows();


        Object abc = RowUtils.getRowValue(rows.get(0), "column1").get();

        assertEquals((int)abc, 123);
    }


    @Test
    public void versionTest(){

        /**
         * String schema, String tableName
         * String[] columns, String[] pkColumns
         * ,boolean hasVersion, Comparator<Object> versionComparator
         */
        MapLocalStore mapLocalStore = new MapLocalStore("test"
                , "test", new String[]{"column1" , "column2", "version"}
                , new String[]{"id"}, true, Comparator.comparingInt(x -> (int)x));

        Map<String, Object> map = new HashMap<>();
        map.put("id", 1);
        map.put("column1", 123);
        map.put("column2", 1234);
        map.put("version", 1234);
        mapLocalStore.save(map);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("id", 1);
        map2.put("column1", 124);
        map2.put("column2", 124);
        map2.put("version", 1235);
        mapLocalStore.save(map2);

        Map<String, Object> map3 = new HashMap<>();
        map3.put("id", 1);
        map3.put("column1", 125);
        map3.put("column2", 124);
        map3.put("version", 1236);
        mapLocalStore.save(map3);

        List<Row> rows = mapLocalStore.query().selectAll().execute().toRows();

        assertEquals(3, rows.size());

        Object abc = RowUtils.getRowValue(rows.get(0), "column1").get();
        assertEquals((int)abc, 123);

        Object abc2 = RowUtils.getRowValue(rows.get(1), "column1").get();
        assertEquals((int)abc2, 124);

        Object abc3 = RowUtils.getRowValue(rows.get(2), "column1").get();
        assertEquals((int)abc3, 125);
    }
}
