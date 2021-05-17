package com.xforceplus.ultraman.oqsengine.storage.master.unique;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.select.BusinessKey;

import java.util.List;
import java.util.Map;

/**
 * generate key
 */
public interface UniqueKeyGenerator {
    /**
     * build the Unique Key
     *
     * @param entity
     * @return
     */
    Map<String, UniqueIndexValue> generator(IEntity entity);

    /**
     * @param key
     * @param entityClass
     * @return
     */
    Map<String, UniqueIndexValue> generator(List<BusinessKey> key, IEntityClass entityClass);

}
