package com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import io.vavr.Tuple2;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class StaticCanalEntryCase extends AbstractCanalEntryCase {
    private Map<String, Tuple2<IEntityField, Object>> context;

    private StaticCanalEntryCase() {
        context = new LinkedHashMap<>();
    }

    public static StaticCanalEntryCase anCase() {
        return new StaticCanalEntryCase();
    }

    public StaticCanalEntryCase withId(IEntityField entityField, long id) {
        context.put(entityField.name(), new Tuple2<>(entityField, id));
        super.id = id;
        return this;
    }

    public StaticCanalEntryCase withLongCol(IEntityField entityField, long long_col) {
        context.put(entityField.name(), new Tuple2<>(entityField, long_col));
        return this;
    }

    public StaticCanalEntryCase withStringCol(IEntityField entityField, String string_col) {
        context.put(entityField.name(), new Tuple2<>(entityField, string_col));
        return this;
    }

    public StaticCanalEntryCase withBoolCol(IEntityField entityField, boolean bool_col) {
        context.put(entityField.name(), new Tuple2<>(entityField, bool_col));
        return this;
    }

    public StaticCanalEntryCase withDateTimeCol(IEntityField entityField, String dateTime_col) {
        context.put(entityField.name(), new Tuple2<>(entityField, dateTime_col));
        return this;
    }

    public StaticCanalEntryCase withDecimalCol(IEntityField entityField, String decimal_col) {
        context.put(entityField.name(), new Tuple2<>(entityField, decimal_col));
        return this;
    }

    public StaticCanalEntryCase withEnumCol(IEntityField entityField, String enumCol) {
        context.put(entityField.name(), new Tuple2<>(entityField, enumCol));
        return this;
    }

    public Map<String, Tuple2<IEntityField, Object>> getContext() {
        return context;
    }
}
