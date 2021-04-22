package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.AnyEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * desc :
 * name : OriginalEntityUtils
 *
 * @author : xujia
 * date : 2021/3/15
 * @since : 1.8
 */
public class OriginalEntityUtils {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public static List<Object> attributesToList(String attrStr) throws JsonProcessingException {
        List<Object> attributes = new ArrayList<>();
        Map<String, Object> keyValues = jsonMapper.readValue(attrStr, Map.class);
        keyValues.forEach(
                (k, v) -> {
                    attributes.add(k);
                    attributes.add(v);
                }
        );
        return attributes;
    }

    public static List<OriginalEntity> toOriginalEntity(MetaManager metaManager, String orgStr) throws JsonProcessingException {
        try {
            List<RawOriginalEntity> rawOriginalEntities =
                    jsonMapper.readValue(orgStr, jsonMapper.getTypeFactory().constructParametricType(List.class, RawOriginalEntity.class));

            return rawOriginalEntities.stream().map(entity -> {
                return RawOriginalEntity.toOriginalEntity(metaManager, entity);
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw e;
        }
    }

    public static String toOriginalEntityStr(List<OriginalEntity> originalEntities) throws JsonProcessingException {
        try {
            return jsonMapper.writeValueAsString(
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
        private Object[] attributes;
        private long maintainid;

        public static OriginalEntity toOriginalEntity(MetaManager metaManager, RawOriginalEntity rawOriginalEntity) {
            Optional<IEntityClass> entityClassOp = metaManager.load(rawOriginalEntity.getEntityId());
            return entityClassOp.map(entityClass -> OriginalEntity.Builder
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
                    .withAttributes(Arrays.asList(rawOriginalEntity.getAttributes()))
                    .withMaintainid(rawOriginalEntity.getMaintainid())
                    .build())
                    .orElse(null);
        }

        public static RawOriginalEntity toRawOriginalEntity(OriginalEntity originalEntity) {
            RawOriginalEntity rawOriginalEntity = new RawOriginalEntity();

            rawOriginalEntity.setDeleted(originalEntity.isDeleted());
            rawOriginalEntity.setOp(originalEntity.getOp());
            rawOriginalEntity.setVersion(originalEntity.getVersion());
            rawOriginalEntity.setOqsMajor(originalEntity.getOqsMajor());
            rawOriginalEntity.setId(originalEntity.getId());
            rawOriginalEntity.setCreateTime(originalEntity.getCreateTime());
            rawOriginalEntity.setUpdateTime(originalEntity.getUpdateTime());
            rawOriginalEntity.setTx(originalEntity.getTx());
            rawOriginalEntity.setCommitid(originalEntity.getCommitid());
            rawOriginalEntity.setAttributes(originalEntity.getAttributes());
            rawOriginalEntity.setEntityId(originalEntity.getEntityClass().id());
            rawOriginalEntity.setMaintainid(originalEntity.getMaintainid());

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

        public Object[] getAttributes() {
            return attributes;
        }

        public void setAttributes(Object[] attributes) {
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
