package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.tables;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * relation table
 *  （"rels"
 *                 , new String[]{
 *                 "id"
 *                 , "boId"
 *                 //onetomany manytoone onetoone
 *                 , "relType"
 *                 , "identity"
 *                 , "joinBoId"
 *                 , "relName"
 *         });
 */
public class RelationTable implements TableLike {


    public static final String TABLE_NAME = "rels";

    public static final String ID = "id";

    public static final String BO_ID = "boId";

    public static final String REL_TYPE = "relType";

    public static final String IDENTITY = "identity";

    public static final String JOIN_BO_ID = "joinBoId";

    public static final String REL_NAME = "relName";

    public static final String[] COLUMNS
            = new String[]{ ID, BO_ID, REL_TYPE, IDENTITY, JOIN_BO_ID, REL_NAME };

    private List<Map<String, ?>> store = new ArrayList<>();

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
