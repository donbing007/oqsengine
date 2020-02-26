package com.xforceplus.ultraman.oqsengine.sdk.util;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import org.apache.metamodel.data.Row;

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
        String name = RowUtils.getRowValue(row, "name").map(String::valueOf).orElse("");
        FieldType fieldType = RowUtils.getRowValue(row, "fieldType").map(String::valueOf).map(FieldType::valueOf).orElse(FieldType.STRING);
        Field field =
                new Field(id, name, fieldType);
        return field;
    }


    public static Field toEntityClassFieldFromRel(Row row, String boCode) {
        Long id = RowUtils.getRowValue(row, "id")
                .map(String::valueOf)
                .map(Long::valueOf)
                .orElse(-1l);

        //TODO current is id
        //fixed
        String name = boCode.concat(".id");
        FieldType fieldType = FieldType.LONG;

        Field field =
                new Field(id, name, fieldType);
        return field;
    }

    public static Relation toEntityClassRelation(Row relRow) {
        return null;
    }
}
