package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.ValueUp;

import java.util.List;
import java.util.Map;

/**
 * TODO apply on query?
 * @author admin
 */
public interface HandleValueService {

    List<ValueUp> handlerValue(EntityClass entityClass, Map<String, Object> body, String phase);

}
