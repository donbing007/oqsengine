package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.executor;

import com.xforceplus.ultraman.oqsengine.common.executor.Executor;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.define.FieldDefine;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;

/**
 * 清理执行器,这是特别的实现.会忽略所有事务.
 * 动作不可撤销,执行过程中不保证所有数据整体消失会出现部份数据存在部份数据不存在.
 * 不过最终如果执行正确,将会所有符合条件的数据被物理清理.
 *
 * @author dongbin
 * @version 0.1 2021/3/8 16:52
 * @since 1.8
 */
public class CleanExecutor implements Executor<Long, Long> {

    private static String sql;

    static {
        StringBuilder buff = new StringBuilder();
        buff.append("DELETE FROM %s")
            .append(" WHERE ")
            .append(FieldDefine.MAINTAIN_ID).append(" != ?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ?")
            .append(" AND ")
            .append("MATCH('(@").append(FieldDefine.ENTITYCLASSF).append(" =").append("\"%d\")')");
        sql = buff.toString();
    }

    private long entityClassId;
    private long start;
    private long end;
    private List<DataSource> ds;
    private List<String> indexNames;

    @Override
    public Long execute(Long maintainId) throws SQLException {
        long total = 0;
        for (DataSource d : ds) {
            try (Connection conn = d.getConnection()) {
                for (String indexName : indexNames) {
                    String sql = buildSQL(indexName, entityClassId);
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setLong(1, maintainId);
                        ps.setLong(2, start);
                        ps.setLong(3, end);

                        total += ps.executeUpdate();
                    }
                }
            }
        }

        return total;
    }

    private String buildSQL(String indexName, long entityClassId) {
        return String.format(sql, indexName, entityClassId);
    }

    /**
     * builder.
     */
    public static final class Builder {
        private long entityClassId;
        private long start;
        private long end;
        private List<DataSource> ds;
        private List<String> indexNames;

        private Builder() {
        }

        public static Builder anCleanExecutor() {
            return new Builder();
        }

        public Builder withEntityClass(long entityClassId) {
            this.entityClassId = entityClassId;
            return this;
        }

        public Builder withStart(long start) {
            this.start = start;
            return this;
        }

        public Builder withEnd(long end) {
            this.end = end;
            return this;
        }

        public Builder withDs(List<DataSource> ds) {
            this.ds = ds;
            return this;
        }

        public Builder withIndexNames(List<String> indexNames) {
            this.indexNames = indexNames;
            return this;
        }

        /**
         * 构造实例.
         *
         * @return 实例.
         */
        public CleanExecutor build() {
            CleanExecutor cleanExecutor = new CleanExecutor();
            cleanExecutor.end = this.end;
            cleanExecutor.entityClassId = this.entityClassId;
            cleanExecutor.ds = this.ds;
            cleanExecutor.indexNames = this.indexNames;
            cleanExecutor.start = this.start;
            return cleanExecutor;
        }
    }
}
