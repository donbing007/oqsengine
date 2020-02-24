package com.xforceplus.ultraman.oqsengine.common.datasource;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源包装.
 * @author dongbin
 * @version 0.1 2020/2/23 16:35
 * @since 1.8
 */
public class DataSourcePackage {

    private List<DataSource> master;
    private List<DataSource> indexWriter;
    private List<DataSource> indexSearch;

    public DataSourcePackage(List<DataSource> master, List<DataSource> indexWriter, List<DataSource> indexSearch) {
        this.master = master;
        this.indexWriter = indexWriter;
        this.indexSearch = indexSearch;
    }

    public List<DataSource> getMaster() {
        return master;
    }

    public List<DataSource> getIndexWriter() {
        return indexWriter;
    }

    public List<DataSource> getIndexSearch() {
        return indexSearch;
    }
}
