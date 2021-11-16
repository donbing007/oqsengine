package com.xforceplus.ultraman.oqsengine.metadata.handler;

import static com.xforceplus.ultraman.oqsengine.meta.common.constant.Constant.MIN_ID;
import static com.xforceplus.ultraman.oqsengine.metadata.constant.Constant.HEALTH_CHECK_ENTITY_ID;

import com.xforceplus.ultraman.oqsengine.common.profile.OqsProfile;
import com.xforceplus.ultraman.oqsengine.metadata.StorageMetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.CacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.HealthCheckEntityClass;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.ProfileStorage;
import com.xforceplus.ultraman.oqsengine.metadata.dto.storage.RelationStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public class DefaultEntityClassFormatHandler implements EntityClassFormatHandler, Serializable {

    final Logger logger = LoggerFactory.getLogger(StorageMetaManager.class);

    @Resource
    private CacheExecutor cacheExecutor;

    @Override
    public Optional<IEntityClass> classLoad(long id, String profile) {
        try {
            if (id == HEALTH_CHECK_ENTITY_ID) {
                return Optional.of(HealthCheckEntityClass.getInstance());
            }
            Map<Long, EntityClassStorage> entityClassStorageMaps = cacheExecutor.read(id);
            return Optional.of(toEntityClass(id, profile, entityClassStorageMaps));
        } catch (Exception e) {
            logger.warn("load entityClass [{}] error, message [{}]", id, e.toString());
            return Optional.empty();
        }
    }

    @Override
    public Collection<IEntityClass> familyLoad(long id) {
        try {
            Map<Long, EntityClassStorage> entityClassStorageMaps = cacheExecutor.read(id);

            EntityClassStorage entityClassStorage = entityClassStorageMaps.get(id);

            if (null == entityClassStorage) {
                throw new SQLException(String.format("entity class [%d] not found.", id));
            }
            List<IEntityClass> entityClassList = new ArrayList<>();
            entityClassList.add(toEntityClass(id, null, entityClassStorageMaps));

            Map<String, ProfileStorage> profileStorages = entityClassStorage.getProfileStorageMap();
            if (null != profileStorages) {
                for (String key : profileStorages.keySet()) {
                    entityClassList.add(toEntityClass(id, key, entityClassStorageMaps));
                }
            }

            return entityClassList;

        } catch (Exception e) {
            logger.warn("load entityClass [{}] error, message [{}]", id, e.toString());
            return new ArrayList<>();
        }
    }

    /**
     * 生成IEntityClass.
     */
    private IEntityClass toEntityClass(long id, String profileCode,
                                       Map<Long, EntityClassStorage> entityClassStorageMaps) throws SQLException {
        EntityClassStorage entityClassStorage = entityClassStorageMaps.get(id);
        if (null == entityClassStorage) {
            throw new SQLException(String.format("entity class [%d] not found.", id));
        }

        List<Relationship> relationships = toQqsRelation(entityClassStorage.getRelations());

        List<IEntityField> entityFields = new ArrayList<>();
        if (null != entityClassStorage.getFields()) {
            entityClassStorage.getFields()
                .forEach(
                    e -> {
                        IEntityField entityField = cloneEntityField(e);
                        if (null != entityField) {
                            entityFields.add(entityField);
                        }
                    }
                );
        }

        //  加载profile
        if (null != profileCode && !profileCode.equals(OqsProfile.UN_DEFINE_PROFILE)
            && null != entityClassStorage.getProfileStorageMap()) {
            ProfileStorage profileStorage = entityClassStorage.getProfileStorageMap().get(profileCode);
            if (null != profileStorage) {
                if (null != profileStorage.getEntityFieldList()) {
                    profileStorage.getEntityFieldList().forEach(
                        ps -> {
                            IEntityField entityField = cloneEntityField(ps);
                            if (null != entityField) {
                                entityFields.add(entityField);
                            }
                        }
                    );
                }

                if (null != profileStorage.getRelationStorageList()) {
                    relationships.addAll(toQqsRelation(profileStorage.getRelationStorageList()));
                }
            }
        }

        EntityClass.Builder builder =
            EntityClass.Builder.anEntityClass()
                .withId(entityClassStorage.getId())
                .withCode(entityClassStorage.getCode())
                .withName(entityClassStorage.getName())
                .withLevel(entityClassStorage.getLevel())
                .withVersion(entityClassStorage.getVersion())
                .withRelations(relationships)
                .withProfile(profileCode)
                .withFields(entityFields);
        //   加载父类.
        if (null != entityClassStorage.getFatherId() && entityClassStorage.getFatherId() >= MIN_ID) {
            builder.withFather(toEntityClass(entityClassStorage.getFatherId(), profileCode, entityClassStorageMaps));
        }

        return builder.build();
    }

    /**
     * 加载relation.
     */
    private List<Relationship> toQqsRelation(List<RelationStorage> relationStorageList) {
        List<Relationship> relationships = new ArrayList<>();
        if (null != relationStorageList) {
            relationStorageList.forEach(
                r -> {
                    Relationship.Builder builder = Relationship.Builder.anRelationship()
                        .withId(r.getId())
                        .withCode(r.getCode())
                        .withLeftEntityClassId(r.getLeftEntityClassId())
                        .withLeftEntityClassCode(r.getLeftEntityClassCode())
                        .withRelationType(Relationship.RelationType.getInstance(r.getRelationType()))
                        .withIdentity(r.isIdentity())
                        .withStrong(r.isStrong())
                        .withRightEntityClassId(r.getRightEntityClassId())
                        .withRightEntityClassLoader(this::classLoad)
                        .withRightFamilyEntityClassLoader(this::familyLoad)
                        .withEntityField(cloneEntityField(r.getEntityField()))
                        .withBelongToOwner(r.isBelongToOwner());

                    relationships.add(builder.build());
                }
            );
        }
        return relationships;
    }

    private IEntityField cloneEntityField(IEntityField entityField) {
        if (null != entityField) {
            return EntityField.Builder.anEntityField()
                .withName(entityField.name())
                .withCnName(entityField.cnName())
                .withFieldType(entityField.type())
                .withDictId(entityField.dictId())
                .withId(entityField.id())
                .withDefaultValue(entityField.defaultValue())
                .withConfig(entityField.config().clone())
                .build();
        }
        return null;
    }
}
