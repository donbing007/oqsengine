package com.xforceplus.ultraman.oqsengine.sdk.store;

import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.delete.DeleteFrom;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.pojo.MapTableDataProvider;
import org.apache.metamodel.pojo.PojoDataContext;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.query.*;
import org.apache.metamodel.query.builder.TableFromBuilder;
import org.apache.metamodel.schema.Column;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.update.Update;
import org.apache.metamodel.util.SimpleTableDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * map local
 */
public class MapLocalStore {

    private Logger logger = LoggerFactory.getLogger(MapLocalStore.class);

    private String schema;

    private String tableName;

    private String[] columns;

    private String[] allColumns;

    private final SimpleTableDef tableDef;

    protected final PojoDataContext dc;

    private final TableDataProvider<?> tableDataProvider;

    private final Table table;

    private final List<Map<String, ?>> maps = new ArrayList<>();

    private CompiledQuery pkQuery = null;

    private Comparator<Object> versionComparator;

    //union pk
    private String[] pks;

    private boolean hasVersion;

    private String versioned = "version";

    private Column versionedColumn;

    //TODO
    private Integer maxVersion = 3;

    public MapLocalStore(String schema, String tableName
            , String[] columns, String[] pkColumns
            , boolean hasVersion, Comparator<Object> versionComparator) {
        this.columns = columns;
        this.schema = schema;
        this.tableName = tableName;
        this.pks = pkColumns;
        this.hasVersion = hasVersion;
        this.versionComparator = versionComparator;

        //combine two string
        Integer length = (pks == null ? 0 : pks.length) + columns.length;
        this.allColumns = new String[length];

        for (int i = 0; i < allColumns.length; i++) {
            if (pks != null && pks.length > 0 && i < pks.length) {
                allColumns[i] = pks[i];
            } else {
                allColumns[i] = columns[i - (pks == null ? 0 : pks.length)];
            }
        }

        tableDef = new SimpleTableDef(tableName, allColumns);
        tableDataProvider = new MapTableDataProvider(tableDef, maps);
        table = tableDef.toTable();
        dc = new PojoDataContext(schema, tableDataProvider);


        if (hasVersion) {
            versionedColumn = table.getColumnByName(versioned);
        }

        if (pks != null) {
            //build up filter
            List<FilterItem> filterItems = Stream.of(pks).map(pk -> new SelectItem(dc
                    .getColumnByQualifiedLabel(schema + "." + tableName + "." + pk)))
                    .map(pk -> new FilterItem(pk, OperatorType.EQUALS_TO, new QueryParameter()))
                    .collect(Collectors.toList());

            Query query = dc.query().from(tableName)
                    .selectAll()
                    .where(filterItems).toQuery();

            pkQuery = dc.compileQuery(query);
        }
    }

    //synchronized maybe is enough
    public void save(Map<String, Object> record) {

        if (hasPk(record)) {
            if (pks != null) {
                //has pks
                List<Row> pkRow = getSamePkRecord(record);
                if (!pkRow.isEmpty()) {
                    //cannot insert directly
                    if (hasVersion && hasVersion(record)) {

                        //check if version is same

                        if (pkRow.stream().map(x -> x.getValue(versionedColumn)).anyMatch(x -> x.equals(record.get(versioned)))) {
                            //Same version found
                            logger.debug("Same version found {}");
                            return;
                        }

                        //insert by version
                        if (pkRow.size() < maxVersion) {
                            insert(record);
                        } else {
                            Row toBeDeleted = pkRow.stream().max((x, y) -> {
                                Object xVersion = x.getValue(versionedColumn);
                                Object yVersion = y.getValue(versionedColumn);
                                return versionComparator.compare(xVersion, yVersion);
                            }).get();

                            //TODO logger
                            System.out.println(toBeDeleted);

                            dc.executeUpdate(new DeleteFrom(table)
                                    .where(rowToFilterItem(toBeDeleted)));

                            insert(record);
                        }
                    } else {
                        //no version
                        update(record);
                    }
                } else {
                    insert(record);
                }
            } else {
                insert(record);
            }
        } else {
            insert(record);
        }
    }


    public Table getTable() {
        return table;
    }

    //insert directly
    //TODO
     private synchronized void insert(Map<String, Object> record) {

        InsertInto insert = new InsertInto(table);

        //selective update
        for (int i = 0; i < allColumns.length; i++) {
            if (record.get(allColumns[i]) != null) {
                insert.value(allColumns[i], record.get(allColumns[i]));
            }
        }

        dc.executeUpdate(insert);
    }

    public void update(Map<String, Object> record, Map<String, Object> condition) {

        List<FilterItem> filterItems = condition.entrySet().stream().map(entry ->
        {
            SelectItem selectItem = new SelectItem(table.getColumnByName(entry.getKey()));
            return new FilterItem(selectItem, OperatorType.EQUALS_TO, entry.getValue());
        }).collect(Collectors.toList());

        Update update = new Update(table)
                .where(filterItems);

        //selective update
        for (int i = 0; i < columns.length; i++) {
            if (record.get(columns[i]) != null) {
                update.value(columns[i], record.get(columns[i]));
            }
        }

        dc.executeUpdate(update);
    }

    private void update(Map<String, Object> record) {

        List<FilterItem> filterItems = Stream.of(pks).map(pk -> new SelectItem(table.getColumnByName(pk)))
                .map(pk -> new FilterItem(pk, OperatorType.EQUALS_TO, record.get(pk.getColumn().getName())))
                .collect(Collectors.toList());

        Update update = new Update(table)
                .where(filterItems);


        //selective update
        for (int i = 0; i < columns.length; i++) {
            if (record.get(columns[i]) != null) {
                update.value(columns[i], record.get(columns[i]));
            }
        }

        dc.executeUpdate(update);
    }

    private Boolean hasVersion(Map<String, Object> record) {
        return record.containsKey(versioned);
    }

    private Boolean hasPk(Map<String, Object> record) {
        if (pks != null) {
            return Stream.of(pks).allMatch(record::containsKey);
        }

        return true;
    }

    public TableFromBuilder query() {
        return dc.query().from(tableName);
    }

    //pks is not null
    private List<Row> getSamePkRecord(Map<String, Object> record) {
        DataSet result = dc.executeQuery(pkQuery, getPk(record));
        return result.toRows();
    }

    private List<FilterItem> rowToFilterItem(Row row) {

        return row.getSelectItems().stream().map(x -> new FilterItem(new SelectItem(table.getColumnByName(x.getColumn().getName()))
                , OperatorType.EQUALS_TO, row.getValue(x.getColumn()))).collect(Collectors.toList());
    }


    private List<FilterItem> mapToFilterItem(Map<String, Object> row) {

        List<FilterItem> list = new ArrayList<>();

        for (int i = 0; i < allColumns.length; i++) {

            Column column = table.getColumnByName(allColumns[i]);

            if (row.get(allColumns[i]) != null) {
                list.add(new FilterItem(new SelectItem(column), OperatorType.EQUALS_TO, row.get(allColumns[i])));
            }
        }

        return list;
    }

    private Object[] getPk(Map<String, Object> record) {
        Object[] pkValues = new Object[pks.length];
        for (int i = 0; i < pks.length; i++) {
            pkValues[i] = record.get(pks[i]);
        }

        return pkValues;
    }

    public SelectItem getRowColumn(Row row, String columnName) {
        Optional<SelectItem> opItem = row.getSelectItems()
                .stream().filter(x -> x.getColumn().getName().equals(columnName)).findAny();

        //Todo throw exception ?
        return opItem.get();
    }

    public Optional<Object> getRowValue(Row row, String columnName) {
        Optional<SelectItem> opItem = row.getSelectItems()
                .stream().filter(x -> x.getColumn().getName().equals(columnName)).findAny();

        return opItem.map(row::getValue);
    }
}
