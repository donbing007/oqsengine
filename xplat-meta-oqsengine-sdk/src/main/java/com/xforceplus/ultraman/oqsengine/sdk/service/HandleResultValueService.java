package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;

import java.util.Map;

/**
 * grpc converter
 */
public interface HandleResultValueService {

    Map<String, Object> toMap(IEntityClass entityClass
                            , EntityUp up, IEntityClass subEntityClass);


}
