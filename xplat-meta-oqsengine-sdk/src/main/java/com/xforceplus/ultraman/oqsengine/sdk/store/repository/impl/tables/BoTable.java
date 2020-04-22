package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.tables;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SimpleTableDef boTableDef = new SimpleTableDef("bos", new String[]{"id", "code", "parentId", "name"});
 *         TableDataProvider boTableDataProvider = new MapTableDataProvider(boTableDef, boStore);
 */
public class BoTable implements TableLike {

    public static final String TABLE_NAME = "bos";

    public static final String ID = "id";

    public static final String CODE = "code";

    public static final String VERSION = "version";

    public static final String PARENT_ID = "parentId";

    public static final String NAME = "name";

    public static final String MODULE_ID = "moduleId";

    private List<Map<String, ?>> store = new ArrayList<>();

    public static final String[] COLUMNS = new String[]{ID, CODE, VERSION, NAME, PARENT_ID, MODULE_ID};

    @Override
    public String name() {
        return TABLE_NAME;
    }

    @Override
    public String[] columns() {
        return COLUMNS;
    }

    @Override
    public List<Map<String, ?>> getStore() {
        return store;
    }
}
