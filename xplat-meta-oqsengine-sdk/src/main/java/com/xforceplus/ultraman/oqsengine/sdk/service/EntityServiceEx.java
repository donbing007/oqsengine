package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import io.vavr.control.Either;

import java.util.Map;

/**
 * extend entity service
 */
public interface EntityServiceEx {

    Either<String, IEntity> create(EntityClass entityClass, Map<String, Object> inputValue);

    Either<String, Map<String, Object>> findOneByParentId(EntityClass entityClass, EntityClass subEntityClass, long id);

}
