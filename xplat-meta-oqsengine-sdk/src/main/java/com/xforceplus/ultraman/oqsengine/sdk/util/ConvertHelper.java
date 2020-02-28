package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import org.apache.metamodel.data.Row;

import static com.xforceplus.ultraman.oqsengine.sdk.util.OptionalHelper.ofEmpty;

/**
 * helper to handler several object
 */
public class ConvertHelper {


    /**
     * Row => field
     * @param row
     * @return
     */
    public static Field toEntityClassField(Row row){

        Long id = RowUtils.getRowValue(row, "id")
                .map(String::valueOf)
                .map(Long::valueOf)
                .orElse(-1l);
        String name = RowUtils.getRowValue(row, "code").map(String::valueOf).orElse("");
        FieldType fieldType = RowUtils.getRowValue(row, "fieldType").map(String::valueOf)
                .map(FieldType::valueOf).orElse(FieldType.STRING);

        Boolean searchable = RowUtils.getRowValue(row, "searchable")
                .map(String::valueOf)
                .map(Boolean::valueOf).orElse(false);

        Long max = RowUtils.getRowValue(row, "maxLength")
                .map(String::valueOf)
                .map(Long::valueOf).orElse(-1L);

        FieldConfig fieldConfig = FieldConfig
                .build()
                .searchable(searchable)
                .max(max);

        Field field =
                new Field(id, name, fieldType, fieldConfig);
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
