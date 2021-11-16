package com.xforceplus.ultraman.oqsengine.storage.master.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

/**
 * Created by justin.xu on 05/2021
 */
public class EntityClassRefHelper {

    public static EntityClassRef fullEntityClassRef(IEntityClass entityClass, String profile) {
        return new EntityClassRef(entityClass.id(), entityClass.code(), profile);
    }
}
