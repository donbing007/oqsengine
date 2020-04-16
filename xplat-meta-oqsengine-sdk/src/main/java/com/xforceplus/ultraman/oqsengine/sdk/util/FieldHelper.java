package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import org.apache.metamodel.data.Row;

/**
 * TODO
 * helper to handler several object
 */
public class FieldHelper {

    /**
     * TODO splitter
     * Row => field
     *
     * @param row
     * @return
     */
    public static Field toEntityClassField(Row row) {

        Long id = RowUtils.getRowValue(row, "id")
            .map(String::valueOf)
            .map(Long::valueOf)
            .orElse(-1L);
        String name = RowUtils.getRowValue(row, "code").map(String::valueOf).orElse("");
        FieldType fieldType = RowUtils.getRowValue(row, "fieldType")
            .map(String::valueOf)
            .map(FieldType::fromRawType)
            .orElse(FieldType.STRING);

        Boolean searchable = RowUtils.getRowValue(row, "searchable")
            .map(String::valueOf)
            .map(Boolean::valueOf).orElse(false);

        Boolean identifier = RowUtils.getRowValue(row, "identifier")
                .map(String::valueOf)
                .map(Boolean::valueOf).orElse(false);

        Boolean required = RowUtils.getRowValue(row, "required")
            .map(String::valueOf)
            .map(Boolean::valueOf).orElse(false);

        Long max = RowUtils.getRowValue(row, "maxLength")
            .flatMap(OptionalHelper::ofEmptyStr)
            .map(Long::valueOf).orElse(-1L);

        Integer precision = RowUtils.getRowValue(row, "precision")
            .flatMap(OptionalHelper::ofEmptyStr)
            .map(Integer::valueOf).orElse(0);

        String defaultValue = RowUtils.getRowValue(row, "defaultValue")
            .map(String::valueOf).orElse("");

        String dictId = RowUtils.getRowValue(row, "dictId")
            .map(String::valueOf).orElse("");

        String validateRule = RowUtils.getRowValue(row, "validateRule")
                .map(String::valueOf).orElse("");

        FieldConfig fieldConfig = FieldConfig
            .build()
            .searchable(searchable)
            .max(max)
            .required(required)
            .precision(precision)
            .identifie(identifier)
            .validateRegexString(validateRule);
        String cnName = RowUtils.getRowValue(row, "name").map(String::valueOf).orElse("");

//        Field field =
//            new Field(id, name, fieldType, fieldConfig, dictId, defaultValue);
        Field field =
                new Field(id, name, cnName, fieldType, fieldConfig, dictId, defaultValue);
        return field;
    }


    public static Field toEntityClassFieldFromRel(Row row, String boCode) {
        Long id = RowUtils.getRowValue(row, "id")
            .map(String::valueOf)
            .map(Long::valueOf)
            .orElse(-1L);

        //TODO current is id
        //fixed
        String name = boCode.concat(".id");
        FieldType fieldType = FieldType.LONG;

        FieldConfig fieldConfig = FieldConfig
            .build()
            .searchable(true);
        Field field =
            new Field(id, name, fieldType, fieldConfig);
        return field;
    }
}
