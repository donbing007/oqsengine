package com.xforceplus.ultraman.oqsengine.sdk.store;

import org.apache.metamodel.data.Row;
import org.apache.metamodel.query.SelectItem;

import java.util.Optional;

/**
 * row utils
 */
public class RowUtils {

    public static Optional<Object> getRowValue(Row row, String columnName) {
        Optional<SelectItem> opItem = row.getSelectItems()
            .stream().filter(x -> x.getColumn().getName().equals(columnName)).findAny();

        return opItem.map(row::getValue);
    }
}
