package com.xforceplus.ultraman.oqsengine.common.datasource;

import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源包装.
 *
 * @author dongbin
 * @version 0.1 2020/2/23 16:35
 * @since 1.8
 */
public class DataSourcePackage {

    private List<DataSource> master;
    private List<DataSource> indexWriter;
    private List<DataSource> indexSearch;
    private DataSource devOps;
    private DataSource changelog;
    private DataSource segment;

    public DataSourcePackage(List<DataSource> master, List<DataSource> indexWriter,
                             List<DataSource> indexSearch, DataSource devOps, DataSource changelog, DataSource segment) {
        this.master = master;
        this.indexWriter = indexWriter;
        this.indexSearch = indexSearch;
        this.devOps = devOps;
        this.changelog = changelog;
        this.segment = segment;
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

    public DataSource getDevOps() {
        return devOps;
    }

    public DataSource getChangelog() {
        return changelog;
    }

    public DataSource getSegment() {
        return segment;
    }

    public void close() {
        if (master != null) {
            doClose(master);
        }

        if (indexWriter != null) {
            doClose(indexWriter);
        }

        if (indexSearch != null) {
            doClose(indexSearch);
        }

        if (null != devOps) {
            ((HikariDataSource) devOps).close();
        }

        if (changelog != null) {
            ((HikariDataSource) changelog).close();
        }
    }

    private void doClose(List<DataSource> master) {
        for (DataSource ds : master) {
            ((HikariDataSource) ds).close();
        }
    }
}
