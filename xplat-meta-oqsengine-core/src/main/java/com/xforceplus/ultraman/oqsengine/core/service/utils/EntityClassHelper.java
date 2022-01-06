package com.xforceplus.ultraman.oqsengine.core.service.utils;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.util.Optional;

/**
 * entityClass的帮助类.
 *
 * @author dongbin
 * @version 0.1 2021/2/20 17:21
 * @since 1.8
 */
public class EntityClassHelper {

    /**
     * 根据EntityClassRef转换得到目标IEntityClass实例.
     *
     * @param metaManager    元信息管理器.
     * @param entityClassRef 元信息指针.
     * @return 元信息.
     */
    public static IEntityClass checkEntityClass(MetaManager metaManager, EntityClassRef entityClassRef) {
        Optional<IEntityClass> entityClassOp = metaManager.load(entityClassRef);
        if (!entityClassOp.isPresent()) {
            throw new IllegalArgumentException(
                String.format("Invalid meta information %d-%s.",
                    entityClassRef.getId(), entityClassRef.getCode()));
        }

        return entityClassOp.get();
    }

    /**
     * 获取并检查目标entityclass获取.
     *
     * @param metaManager 元信息.
     * @param entityClassRefs 指针.
     * @return 元信息列表.
     */
    public static IEntityClass[] checkEntityClasses(MetaManager metaManager, EntityClassRef[] entityClassRefs) {
        IEntityClass[] entityClasses = new IEntityClass[entityClassRefs.length];
        Optional<IEntityClass> entityClassOp;
        EntityClassRef ref;
        for (int i = 0; i < entityClassRefs.length; i++) {
            ref = entityClassRefs[i];
            entityClassOp = metaManager.load(ref);

            if (!entityClassOp.isPresent()) {
                throw new IllegalArgumentException(
                    String.format("Invalid meta information %d-%s.",
                        ref.getId(), ref.getCode()));
            } else {

                entityClasses[i] = entityClassOp.get();
            }
        }

        return entityClasses;
    }

    /**
     * 检查并返回目标实例的元信息.
     *
     * @param metaManager 元信息管理器.
     * @param entities 目标实体.
     * @return 元信息列表.
     */
    public static IEntityClass[] checkEntityClasses(MetaManager metaManager, IEntity[] entities) {
        EntityClassRef[] entityClassRefs = new EntityClassRef[entities.length];
        for (int i = 0; i < entities.length; i++) {
            entityClassRefs[i] = entities[i].entityClassRef();
        }
        return checkEntityClasses(metaManager, entityClassRefs);
    }
}
