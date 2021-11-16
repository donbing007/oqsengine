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

    /**
     * 主库连接池的获取序号.
     * 兼容老版本的主库连接获取逻辑,所以主库仍然接受列表类型的DataSource实例.
     * 实际只有第一个元素有效.
     */
    private static final int MASTER_DATASOURCE_INDEX = 0;

    private List<DataSource> master;
    private List<DataSource> indexWriter;
    private List<DataSource> indexSearch;

    /**
     * 数据源包装实例.
     *
     * @param master 主库存数据源列表.
     * @param indexWriter 索引写数据源列表.
     * @param indexSearch 索引搜索数据源列表.
     //* @param devOps devops数据源.
     //* @param changelog changelog数据源.
     */
    public DataSourcePackage(List<DataSource> master, List<DataSource> indexWriter,
                             List<DataSource> indexSearch) {
        this.master = master;
        this.indexWriter = indexWriter;
        this.indexSearch = indexSearch;
    }

    public List<DataSource> getMaster() {
        return master;
    }

    public DataSource getFirstMaster() {
        return this.master.get(MASTER_DATASOURCE_INDEX);
    }

    public List<DataSource> getIndexWriter() {
        return indexWriter;
    }

    public List<DataSource> getIndexSearch() {
        return indexSearch;
    }

    public DataSource getDevOps() {
        return getFirstMaster();
    }

    public DataSource getChangelog() {
        return getFirstMaster();
    }

    public DataSource getSegment() {
        return getFirstMaster();
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
    }

    private void doClose(List<DataSource> master) {
        for (DataSource ds : master) {
            ((HikariDataSource) ds).close();
        }
    }
}
