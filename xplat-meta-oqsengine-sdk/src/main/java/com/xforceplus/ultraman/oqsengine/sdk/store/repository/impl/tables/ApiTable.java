package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.tables;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SimpleTableDef ApiTableDef'
 *  new SimpleTableDef("apis", new String[]{"boId", "url", "method", "code"});
 */
public class ApiTable implements TableLike {

    public static final String TABLE_NAME = "apis";

    public static final String BO_ID = "boId";

    public static final String URL = "url";

    public static final String METHOD = "method";

    public static final String CODE = "code";

    private List<Map<String, ?>> store = new ArrayList<>();

    public static final String[] columns = new String[]{BO_ID, URL, METHOD, CODE};

    @Override
    public String name() {
        return TABLE_NAME;
    }

    @Override
    public String[] columns() {
        return columns;
    }

    @Override
    public List<Map<String, ?>> getStore() {
        return store;
    }
}
