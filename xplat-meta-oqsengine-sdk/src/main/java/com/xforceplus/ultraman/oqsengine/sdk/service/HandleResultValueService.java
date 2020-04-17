package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;

import java.util.Map;

/**
 * grpc converter
 */
public interface HandleResultValueService {

    Record toRecord(EntityClass entityClass, EntityUp up);

}
