package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.tables;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.TableLike;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * "fields", new String[]{"boId"
*    , "id"
*    , "code", "displayType", "editable", "enumCode", "maxLength", "name", "required", "fieldType"
*    , "searchable", "dictId", "defaultValue", "precision", "identifier", "validateRule"});
 */
public class FieldTable implements TableLike {

    public static final String TABLE_NAME = "fields";

    public static final String ID = "id";

    public static final String BO_ID = "boId";

    public static final String CODE = "code";

    public static final String DISPLAY_TYPE = "displayType";

    public static final String EDITABLE = "editable";

    public static final String ENUM_CODE = "enumCode";

    public static final String MAX_LENGTH = "maxLength";

    public static final String NAME = "name";

    public static final String REQUIRED = "required";

    public static final String FIELD_TYPE = "fieldType";

    public static final String SEARCHABLE = "searchable";

    public static final String DICT_ID = "dictId";

    public static final String DEFAULT_VALUE = "defaultValue";

    public static final String PRECISION = "precision";

    public static final String IDENTIFIER = "identifier";

    public static final String VALIDATE_RULE = "validateRule";

    public static final String[] COLUMNS = new String[]{
              ID
            , BO_ID
            , CODE
            , DISPLAY_TYPE, EDITABLE, ENUM_CODE, MAX_LENGTH
            , NAME , REQUIRED, FIELD_TYPE, SEARCHABLE
            , DICT_ID , DEFAULT_VALUE, PRECISION
            , IDENTIFIER , VALIDATE_RULE
    };

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
