package com.xforceplus.ultraman.oqsengine.common.datasource;

import com.zaxxer.hikari.HikariDataSource;
import java.util.List;
import javax.sql.DataSource;

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

    /**
     * 数据源包装实例.
     *
     * @param master 主库存数据源列表.
     * @param indexWriter 索引写数据源列表.
     * @param indexSearch 索引搜索数据源列表.
     * @param devOps devops数据源.
     * @param changelog changelog数据源.
     */
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

    /**
     * 关闭所有数据源.
     */
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
