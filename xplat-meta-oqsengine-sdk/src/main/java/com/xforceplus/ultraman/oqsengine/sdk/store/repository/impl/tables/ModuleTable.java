package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.tables;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Module Table
 */
public class ModuleTable implements TableLike {

    public static final String TABLE_MODULE = "modules";

    public static final String ID = "id";

    public static final String CODE = "code";

    public static final String VERSION = "version";

    public static final String NAME = "name";

    private static final String[] columns = new String[]{ID, CODE, VERSION, NAME};

    private List<Map<String, ?>> store = new ArrayList<>();

    @Override
    public String name() {
        return TABLE_MODULE;
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
