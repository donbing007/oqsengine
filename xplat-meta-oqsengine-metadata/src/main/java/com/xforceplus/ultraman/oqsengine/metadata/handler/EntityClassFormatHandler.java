package com.xforceplus.ultraman.oqsengine.metadata.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public interface EntityClassFormatHandler extends Serializable {

    /**
     * 获取一个entityClass.
     */
    Optional<IEntityClass> classLoad(long id, String profile);

    /**
     * 获取当前entityClassId下的所有EntityClassWithProfile.
     */
    Collection<IEntityClass> familyLoad(long id);
}
