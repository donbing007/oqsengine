package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.common.StringUtils;
import com.xforceplus.ultraman.oqsengine.common.iterator.DataIterator;
import com.xforceplus.ultraman.oqsengine.common.lifecycle.Lifecycle;
import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.master.define.FieldDefine;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;

/**
 * 指定对象更新范围的迭代器.可以不断迭代指定范围内的数据直到没有为止.
 * <pre>
 *     try (EntityUpdateTimeRangeIterator iterator = //构造) {
 *        OqsEngineEntity entity;
 *          while(iterator.hasNext()) {
 *            entity = iterator.next();
 *            // 处理实例.
 *          }
 *     }
 * </pre>
 * 实际底层利用了JDBC的流读取,所以一定要保证使用后进行清理.<br>
 * <pre>EntityUpdateTimeRangeIterator.destroy();</pre>
 * 或者像上述例中,利用AutoCloseable进行关闭.
 *
 * @author dongbin
 * @version 0.1 2022/8/18 16:21
 * @since 1.8
 */
public class EntityUpdateTimeRangeIterator implements DataIterator<OqsEngineEntity>, Lifecycle, AutoCloseable {

    private static final int DEFAULT_BUFFER_SIZE = 1000;
    private int buffSize;
    private long startTime;
    private long endTime;
    private String tableName;
    private IEntityClass entityClass;
    private DataSource dataSource;
    private List<OqsEngineEntity> buffer;

    private MetaManager metaManager;

    private Connection connection;
    private PreparedStatement ps;
    private ResultSet rs;

    @Override
    public void init() throws Exception {
        buffer = new ArrayList<>(buffSize);

        connection = dataSource.getConnection();
        String sql = buildSQL();
        ps = connection.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        int pos = 1;
        ps.setLong(pos++, startTime);
        ps.setLong(pos++, endTime);
        ps.setBoolean(pos++, false);
        ps.setString(pos++, entityClass.profile());

        rs = ps.executeQuery();
    }

    @Override
    public void destroy() throws Exception {
        if (rs != null) {
            rs.close();
        }
        if (ps != null) {
            ps.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public boolean hasNext() {
        if (buffer.isEmpty()) {
            load();
        }

        return !buffer.isEmpty();
    }

    @Override
    public OqsEngineEntity next() {
        if (hasNext()) {
            return buffer.remove(0);
        } else {
            return null;
        }
    }

    @Override
    public void close() throws Exception {
        destroy();
    }

    private void load() throws RuntimeException {
        try {

            OqsEngineEntity entity;
            while (rs.next()) {
                entity = buildFormResultSet(rs);
                if (entity != null) {
                    this.buffer.add(entity);
                }

                if (this.buffer.size() == buffSize) {
                    break;
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private OqsEngineEntity buildFormResultSet(ResultSet rs) throws Exception {
        Optional<IEntityClass> entityClassOp = parseEntityClass(rs, metaManager);
        if (!entityClassOp.isPresent()) {
            // 不存的元信息,表示已经不存在了,不需要操作.
            return null;
        }

        return OqsEngineEntity.Builder.anOriginalEntity()
            .withDeleted(false)
            .withId(rs.getLong(FieldDefine.ID))
            .withOp(rs.getInt(FieldDefine.OP))
            .withTx(rs.getLong(FieldDefine.TX))
            .withCommitid(rs.getLong(FieldDefine.COMMITID))
            .withCreateTime(rs.getLong(FieldDefine.CREATE_TIME))
            .withUpdateTime(rs.getLong(FieldDefine.UPDATE_TIME))
            .withVersion(rs.getInt(FieldDefine.VERSION))
            .withEntityClass(entityClassOp.get())
            .withAttributes(OriginalEntityUtils.attributesToMap(rs.getString(FieldDefine.ATTRIBUTE)))

            .build();
    }

    private Optional<IEntityClass> parseEntityClass(ResultSet rs, MetaManager metaManager) throws SQLException {
        long[] entityClassIds = new long[FieldDefine.ENTITYCLASS_LEVEL_LIST.length];
        for (int i = 0; i < entityClassIds.length; i++) {
            entityClassIds[i] = rs.getLong(FieldDefine.ENTITYCLASS_LEVEL_LIST[i]);
        }

        long selfEntityClassId = 0;
        for (long id : entityClassIds) {
            if (id > 0) {
                selfEntityClassId = id;
            } else {
                break;
            }
        }

        String profile = rs.getString(FieldDefine.PROFILE);
        if (profile == null) {
            profile = OqsProfile.UN_DEFINE_PROFILE;
        }

        return metaManager.load(selfEntityClassId, profile);
    }

    private String buildSQL() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(String.join(",",
                FieldDefine.ID,
                FieldDefine.ENTITYCLASS_LEVEL_0,
                FieldDefine.ENTITYCLASS_LEVEL_1,
                FieldDefine.ENTITYCLASS_LEVEL_2,
                FieldDefine.ENTITYCLASS_LEVEL_3,
                FieldDefine.ENTITYCLASS_LEVEL_4,
                FieldDefine.PROFILE,
                FieldDefine.VERSION,
                FieldDefine.CREATE_TIME,
                FieldDefine.UPDATE_TIME,
                FieldDefine.ATTRIBUTE,
                FieldDefine.OP,
                FieldDefine.TX,
                FieldDefine.COMMITID
            )
        );

        sql.append(" FROM ")
            .append(this.tableName)
            .append(" WHERE ")
            .append(FieldDefine.UPDATE_TIME).append(" >= ").append("?")
            .append(" AND ")
            .append(FieldDefine.UPDATE_TIME).append(" <= ").append("?")
            .append(" AND ")
            .append(FieldDefine.DELETED).append(" = ").append("?")
            .append(" AND ")
            .append(FieldDefine.PROFILE).append(" = ").append("?")
            .append(" AND ")
            .append(EntityClassHelper.buildEntityClassQuerySql(entityClass));
        return sql.toString();
    }

    /**
     * 构造器.
     */
    public static final class Builder {
        private int buffSize;
        private long startTime;
        private long endTime;
        private String tableName;
        private IEntityClass entityClass;
        private DataSource dataSource;
        private MetaManager metaManager;

        private Builder() {
            buffSize = EntityUpdateTimeRangeIterator.DEFAULT_BUFFER_SIZE;
            startTime = -1;
            endTime = -1;
        }

        public static Builder anEntityIterator() {
            return new Builder();
        }

        public Builder witherBuffSize(int size) {
            this.buffSize = size;
            return this;
        }

        public Builder witherTableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        public Builder withStartTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder withEndTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

        public Builder withEntityClass(IEntityClass entityClass) {
            this.entityClass = entityClass;
            return this;
        }

        public Builder withDataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }

        public Builder withMetaManager(MetaManager metaManager) {
            this.metaManager = metaManager;
            return this;
        }

        /**
         * 构造对象时间范围迭代器.<br>
         * 以下值为必须.<br>
         * <ul>
         *     <li>dataSource</li>
         *     <li>entityClass</li>
         *     <li>startTime</li>
         *     <li>endTime</li>
         *     <li>tableName</li>
         *     <li>metaManager</li>
         * </ul>
         *
         * @return 迭代器实例.
         */
        public EntityUpdateTimeRangeIterator build() {
            if (entityClass == null) {
                throw new RuntimeException("Invalid meta information.");
            }

            if (startTime < 0) {
                throw new RuntimeException("Invalid start time.");
            }

            if (endTime < 0) {
                throw new RuntimeException("Invalid end time.");
            }

            if (startTime > endTime) {
                throw new RuntimeException(
                    String.format("The start time cannot be later than the end time.[%d, %d]", startTime, endTime));
            }

            if (dataSource == null) {
                throw new RuntimeException("Invalid data source.");
            }

            if (metaManager == null) {
                throw new RuntimeException("Invalid meta manager.");
            }

            if (StringUtils.isEmpty(tableName)) {
                throw new RuntimeException("Invalid table name.");
            }

            EntityUpdateTimeRangeIterator entityIterator = new EntityUpdateTimeRangeIterator();
            entityIterator.buffSize = this.buffSize;
            entityIterator.startTime = this.startTime;
            entityIterator.endTime = this.endTime;
            entityIterator.dataSource = this.dataSource;
            entityIterator.entityClass = this.entityClass;
            entityIterator.tableName = this.tableName;
            entityIterator.metaManager = this.metaManager;

            try {
                entityIterator.init();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return entityIterator;
        }
    }
}
