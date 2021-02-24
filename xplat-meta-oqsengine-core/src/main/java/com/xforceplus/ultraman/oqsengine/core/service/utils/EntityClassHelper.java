package com.xforceplus.ultraman.oqsengine.core.service.utils;

import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import java.sql.SQLException;
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
     * @throws SQLException
     */
    public static IEntityClass checkEntityClass(MetaManager metaManager, EntityClassRef entityClassRef) {
        Optional<IEntityClass> entityClassOptional = metaManager.load(entityClassRef.entityClassId());
        if (!entityClassOptional.isPresent()) {
            throw new IllegalArgumentException(
                String.format("Invalid meta information %d-%s.",
                    entityClassRef.entityClassId(), entityClassRef.entityClassCode()));
        }

        return entityClassOptional.get();
    }
}
