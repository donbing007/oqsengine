package com.xforceplus.ultraman.oqsengine.common.selector;


import com.xforceplus.ultraman.oqsengine.common.hash.Time33Hash;

import java.util.ArrayList;
import java.util.List;

/**
 * 数字后辍的字符串选择器.
 *
 * base = "table_"
 * size = 3
 * 那么结果将会是"table_0","table_1","table_2"范围之内.
 *
 * @author dongbin
 * @version 0.1 2020/2/16 19:18
 * @since 1.8
 */
public class SuffixNumberHashSelector implements Selector<String> {

    private String base;
    private int size;

    public SuffixNumberHashSelector(String base, int size) {
        this.base = base;
        this.size = size;
    }

    @Override
    public String select(String key) {
        int code = Time33Hash.build().hash(key);
        return base + (Math.abs(code % size));
    }

    @Override
    public List<String> selects() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(base + i);
        }
        return list;
    }
}
