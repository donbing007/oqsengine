package com.xforceplus.ultraman.oqsengine.cdc.consumer.dto;

import com.xforceplus.ultraman.oqsengine.pojo.cdc.dto.RawEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * desc :.
 * name : RawEntryTest
 *
 * @author : xujia 2020/11/5
 * @since : 1.8
 */
public class RawEntryTest {

    private Map<Long, RawEntry> rawEntries;

    private List<RawEntry> rawLists;

    private int size = 10;

    private int expectedSize = 4;

    /**
     * 测试初始化.
     */
    @BeforeEach
    public void before() {
        rawEntries = new LinkedHashMap<>();
        rawLists = new ArrayList<>(size);
        initData(size);
    }

    @Test
    public void testHashMapRecover() {
        for (int i = 0; i < size; i++) {
            rawEntries.put(Long.valueOf(i % expectedSize + ""), rawLists.get(i));
        }

        Assertions.assertEquals(expectedSize, rawEntries.size());
    }

    private void initData(int size) {
        for (int i = 0; i < size; i++) {
            rawLists.add(buildRawEntry(i));
        }
    }

    private RawEntry buildRawEntry(int i) {
        return new RawEntry("test", 1, i, 1L, System.currentTimeMillis(), new ArrayList<>());
    }
}
