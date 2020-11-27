package com.xforceplus.ultraman.oqsengine.pojo.dto.summary;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;

/**
 * desc :
 * name : BatchSummary
 *
 * @author : xujia
 * date : 2020/8/22
 * @since : 1.8
 */
public class BatchSummary {

    /*
        这是一个已经按照分库分表设置完数量的结构
     */
    private List<DataSourceSummary> dataSourceSummaries;
    /*
        上一次查询的执行终点，每一次新的查询必须由该对象开始
     */
    private CheckPointOffset checkPointOffset;


    public BatchSummary(List<DataSourceSummary> dataSourceSummaries) throws SQLException {
        this.dataSourceSummaries = dataSourceSummaries;

        /*
            初始化checkPoint
         */
        checkPointOffset = new CheckPointOffset(this.dataSourceSummaries.iterator());
    }

    public boolean resetCheckPoint(OffsetSnapShot offsetSnapShot) {
        Iterator<DataSourceSummary> dataSourceSummaryIterator = this.dataSourceSummaries.iterator();

        DataSourceSummary dataSourceSummary = null;

        /*
            get dataSummary iterator
         */
        while (dataSourceSummaryIterator.hasNext()) {
            dataSourceSummary = dataSourceSummaryIterator.next();
            if (dataSourceSummary.getPoolName().equalsIgnoreCase(offsetSnapShot.getDataSourceName())) {
                break;
            }
        }

        /*
            get tableSummary iterator
         */
        if (null != dataSourceSummary) {
            Iterator<TableSummary> tableSummaryIterator = dataSourceSummary.getTableSummaries().iterator();
            while (tableSummaryIterator.hasNext()) {
                if (tableSummaryIterator.next().getTableName().equals(offsetSnapShot.getTableName())) {
                    break;
                }
            }

            checkPointOffset = new CheckPointOffset(dataSourceSummaryIterator, tableSummaryIterator,
                    dataSourceSummary.getDataSource(), offsetSnapShot);

            return true;
        }

        return false;
    }


    /*
        获取当前总任务数
     */
    public int count() {
        return dataSourceSummaries.stream().mapToInt(DataSourceSummary::getTotal).sum();
    }
    /*
        所有数据库中是否还有任务？
     */
    public boolean hasNext() {
        return checkPointOffset.hasNext();
    }

    /*
        获取下个批次信息
     */
    public CheckPointOffset next() throws SQLException {
        if (checkPointOffset.hasNext()) {
            return checkPointOffset.next();
        }
        throw new SQLException("no more element");
    }

    public void offsetReset(long id) {
        if (0 < checkPointOffset.left()) {
            checkPointOffset.setOffset(id);
        }
        snapShot();
    }

    public OffsetSnapShot snapShot() {
        return checkPointOffset.offsetSnapShot();
    }


    /**
     * desc :
     * name : CheckPointOffset
     *
     * @author : xujia
     * date : 2020/8/22
     * @since : 1.8
     */
    public static class CheckPointOffset implements Offset {
        private CheckOffsetIterator checkOffsetIterator;
        private DataSource activeDataSource;                    //  当前数据库
        private OffsetSnapShot offsetSnapShot;

        public CheckPointOffset(Iterator<DataSourceSummary> dataSourceSummaryIterator,
                                Iterator<TableSummary> tableSummaryIterator,
                                DataSource dataSource, OffsetSnapShot offsetSnapShot) {
            this.activeDataSource = dataSource;
            this.offsetSnapShot = offsetSnapShot;

            this.checkOffsetIterator = new CheckOffsetIterator(dataSourceSummaryIterator, tableSummaryIterator);
        }

        public CheckPointOffset(Iterator<DataSourceSummary> dataSourceSummaryIterator)  throws SQLException {
            DataSourceSummary dataSourceSummary = dataSourceSummaryIterator.next();
            Iterator<TableSummary> tableSummaryIterator = dataSourceSummary.getTableSummaries().iterator();
            TableSummary tableSummary = tableSummaryIterator.next();

            this.checkOffsetIterator = new CheckOffsetIterator(dataSourceSummaryIterator, tableSummaryIterator);
            initOffsetSnapShot(dataSourceSummary, tableSummary);
        }

        private void initOffsetSnapShot(DataSourceSummary dataSourceSummary, TableSummary tableSummary) throws SQLException {
            if (null == offsetSnapShot && null == dataSourceSummary) {
                throw new SQLException("init offset error, must have dataSource");
            }

            String dataSourceName = null;
            if (null != dataSourceSummary) {
                activeDataSource = dataSourceSummary.getDataSource();
                dataSourceName = dataSourceSummary.getPoolName();
            } else {
                dataSourceName = offsetSnapShot.getDataSourceName();
            }

            offsetSnapShot = new OffsetSnapShot(dataSourceName, tableSummary.getTableName(), 0, tableSummary.getCount());
        }

        /*
            获取快照信息
         */
        public OffsetSnapShot offsetSnapShot() {
            return offsetSnapShot;
        }

        /*
            当前活跃数据库
         */
        public DataSource getActiveDataSource() {
            return activeDataSource;
        }

        /*
            当前库中是否还有剩余任务
         */
        public boolean hasNext() {
            return checkOffsetIterator.hasNext() || offsetSnapShot.getLeft() > 0;
        }

        public CheckPointOffset next() throws SQLException {
            if (offsetSnapShot.getLeft() > 0) {
                return this;
            } else if (checkOffsetIterator.hasNext()) {
                AbstractMap.SimpleEntry<DataSourceSummary, TableSummary> se =
                            checkOffsetIterator.next();
                initOffsetSnapShot(se.getKey(), se.getValue());
                return this;
            }
            throw new SQLException("no more element");
        }

        @Override
        public long offset() {
            return offsetSnapShot.getStartId();
        }

        @Override
        public void setOffset(long id) {
            offsetSnapShot.setStartId(id);
        }

        @Override
        public void decrease(int v) {
            offsetSnapShot.setLeft(offsetSnapShot.getLeft() - v);
        }

        @Override
        public int left() {
            return offsetSnapShot.getLeft();
        }



        /**
         * desc :
         * name : CheckOffsetIterator
         *
         * @author : xujia
         * date : 2020/8/22
         * @since : 1.8
         */
        private static class CheckOffsetIterator {
            private Iterator<DataSourceSummary> dataSourceSummaryIterator;
            private Iterator<TableSummary> tableSummaryIterator;

            public CheckOffsetIterator(Iterator<DataSourceSummary> dataSourceSummaryIterator, Iterator<TableSummary> tableSummaryIterator) {
                this.dataSourceSummaryIterator = dataSourceSummaryIterator;
                this.tableSummaryIterator = tableSummaryIterator;
            }

            public boolean hasNext() {
                return dataSourceSummaryIterator.hasNext() || tableSummaryIterator.hasNext();
            }

            public AbstractMap.SimpleEntry<DataSourceSummary, TableSummary> next() {
                if (tableSummaryIterator.hasNext()) {
                    return new AbstractMap.SimpleEntry<>(null, tableSummaryIterator.next());
                }
                DataSourceSummary dataSourceSummary = dataSourceSummaryIterator.next();
                this.tableSummaryIterator = dataSourceSummary.getTableSummaries().iterator();
                return new AbstractMap.SimpleEntry<>(dataSourceSummary, tableSummaryIterator.next());
            }
        }
    }
}
