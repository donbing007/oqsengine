package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.xforceplus.ultraman.oqsengine.common.serializable.utils.JacksonDefaultMapper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帮助类.
 *
 * @author xujia 2021/3/15
 * @since 1.8
 */
public class OriginalEntityUtils {

    /**
     * 属性字符串表示解析为实际对象列表.
     *
     * @param attrStr 属性的字符串表示.
     * @return 解析结果.
     * @throws JsonProcessingException JSON解析失败.
     */
    public static Map<String, Object> attributesToMap(String attrStr) throws JsonProcessingException {
        return JacksonDefaultMapper.OBJECT_MAPPER.readValue(attrStr, Map.class);
    }

    /**
     * 构造 OriginalEntity 实例列表.
     *
     * @param metaManager 元信息管理器.
     * @param orgStr 原始JSON对象数据.
     * @return 解析结果列表.
     * @throws JsonProcessingException JSON错误.
     */
    public static List<OqsEngineEntity> toOriginalEntity(MetaManager metaManager, String orgStr)
        throws JsonProcessingException {
        try {
            List<RawOriginalEntity> rawOriginalEntities =
                JacksonDefaultMapper.OBJECT_MAPPER.readValue(orgStr,
                    JacksonDefaultMapper.OBJECT_MAPPER.getTypeFactory()
                        .constructParametricType(List.class, RawOriginalEntity.class));

            return rawOriginalEntities.stream().map(entity ->
                RawOriginalEntity.toOriginalEntity(metaManager, entity)
            ).collect(Collectors.toList());
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 将 OriginalEntity 列表转换为JSON表示字符串.
     *
     * @param originalEntities 原始对象列表.
     * @return JSON字符串表示.
     * @throws JsonProcessingException JSON解析异常.
     */
    public static String toOriginalEntityStr(List<OqsEngineEntity> originalEntities) throws JsonProcessingException {
        try {
            return JacksonDefaultMapper.OBJECT_MAPPER.writeValueAsString(
                originalEntities.stream()
                    .map(RawOriginalEntity::toRawOriginalEntity)
                    .collect(Collectors.toList())
            );
        } catch (Exception e) {
            throw e;
        }
    }

    private static class RawOriginalEntity {
        private boolean deleted;
        private int op;
        private int version;
        private int oqsMajor;
        private long id;
        private long createTime;
        private long updateTime;
        private long tx;
        private long commitid;
        private long entityId;
        private Map<String, Object> attributes;
        private long maintainid;

        public static OqsEngineEntity toOriginalEntity(MetaManager metaManager, RawOriginalEntity rawOriginalEntity) {
            Optional<IEntityClass> entityClassOp = metaManager.load(rawOriginalEntity.getEntityId(), "");
            return entityClassOp.map(entityClass -> OqsEngineEntity.Builder
                .anOriginalEntity()
                    .withDeleted(rawOriginalEntity.isDeleted())
                    .withOp(rawOriginalEntity.getOp())
                    .withVersion(rawOriginalEntity.getVersion())
                    .withOqsMajor(rawOriginalEntity.getOqsMajor())
                    .withId(rawOriginalEntity.getId())
                    .withCreateTime(rawOriginalEntity.getCreateTime())
                    .withUpdateTime(rawOriginalEntity.getUpdateTime())
                    .withTx(rawOriginalEntity.getTx())
                    .withCommitid(rawOriginalEntity.getCommitid())
                    .withEntityClass(entityClass)
                    .withAttributes(rawOriginalEntity.getAttributes())
                .withMaintainid(rawOriginalEntity.getMaintainid())
                .build())
                .orElse(null);
        }

        public static RawOriginalEntity toRawOriginalEntity(OqsEngineEntity oqsEngineEntity) {
            RawOriginalEntity rawOriginalEntity = new RawOriginalEntity();

            rawOriginalEntity.setDeleted(oqsEngineEntity.isDeleted());
            rawOriginalEntity.setOp(oqsEngineEntity.getOp());
            rawOriginalEntity.setVersion(oqsEngineEntity.getVersion());
            rawOriginalEntity.setOqsMajor(oqsEngineEntity.getOqsMajor());
            rawOriginalEntity.setId(oqsEngineEntity.getId());
            rawOriginalEntity.setCreateTime(oqsEngineEntity.getCreateTime());
            rawOriginalEntity.setUpdateTime(oqsEngineEntity.getUpdateTime());
            rawOriginalEntity.setTx(oqsEngineEntity.getTx());
            rawOriginalEntity.setCommitid(oqsEngineEntity.getCommitid());
            rawOriginalEntity.setAttributes(oqsEngineEntity.getAttributes());
            rawOriginalEntity.setEntityId(oqsEngineEntity.getEntityClass().id());
            rawOriginalEntity.setMaintainid(oqsEngineEntity.getMaintainid());

            return rawOriginalEntity;
        }

        public boolean isDeleted() {
            return deleted;
        }

        public void setDeleted(boolean deleted) {
            this.deleted = deleted;
        }

        public int getOp() {
            return op;
        }

        public void setOp(int op) {
            this.op = op;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public int getOqsMajor() {
            return oqsMajor;
        }

        public void setOqsMajor(int oqsMajor) {
            this.oqsMajor = oqsMajor;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }

        public void setAttributes(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public long getMaintainid() {
            return maintainid;
        }

        public void setMaintainid(long maintainid) {
            this.maintainid = maintainid;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public long getTx() {
            return tx;
        }

        public void setTx(long tx) {
            this.tx = tx;
        }

        public long getCommitid() {
            return commitid;
        }

        public void setCommitid(long commitid) {
            this.commitid = commitid;
        }

        public long getEntityId() {
            return entityId;
        }

        public void setEntityId(long entityId) {
            this.entityId = entityId;
        }
    }
}
