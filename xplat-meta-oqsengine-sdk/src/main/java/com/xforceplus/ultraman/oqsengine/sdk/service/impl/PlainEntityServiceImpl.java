package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.PlainEntityService;
import com.xforceplus.ultraman.oqsengine.sdk.util.GetResult;
import com.xforceplus.ultraman.oqsengine.sdk.vo.DataCollection;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * plain entity service
 */
public class PlainEntityServiceImpl implements PlainEntityService {

    private EntityService entityService;

    private static final String MISSING_ENTITY = "Entity %s is missing";

    private static final String MISSING_ENTITY_VER = "Entity %s is missing with version %s";

    public PlainEntityServiceImpl(EntityService entityService) {
        this.entityService = entityService;
    }

    @Value("${xplat.oqsengine.sdk.cas.retry.auto:true}")
    private boolean autoRetry = true;

    @Value("${xplat.oqsengine.sdk.cas.retry-delete.auto:false}")
    private boolean autoRetryOnDelete = false;

    @Override
    public IEntityClass load(String boId) {
        return GetResult.get(entityService.load(boId)
                , String.format(MISSING_ENTITY, boId));
    }

    @Override
    public IEntityClass load(String boId, String version) {
        return GetResult.get(entityService.load(boId, version)
                , String.format(MISSING_ENTITY_VER, boId, version));
    }

    @Override
    public IEntityClass loadByCode(String bocode) {
        return GetResult.get(entityService.loadByCode(bocode)
                , String.format(MISSING_ENTITY, bocode));
    }

    @Override
    public IEntityClass loadByCode(String bocode, String version) {
        return GetResult.get(entityService.loadByCode(bocode, version)
                , String.format(MISSING_ENTITY_VER, bocode, version));
    }

    @Override
    public <T> T transactionalExecute(Callable<T> supplier) {
        return GetResult.get(entityService.transactionalExecute(supplier));
    }

    @Override
    public Map<String, Object> findOne(IEntityClass entityClass, long id) {
        return GetResult.get(entityService.findOne(entityClass, id));
    }

    @Override
    public <T> T retryExecute(String key, Supplier<Either<String, T>> supplier) {
        return GetResult.get(entityService.retryExecute(key, supplier));
    }

    @Override
    public Integer deleteOne(IEntityClass entityClass, Long id) {
        if (autoRetryOnDelete) {
            String conflictKey = entityClass.code() + id;
            return retryExecute(conflictKey, () -> entityService.deleteOne(entityClass, id));
        } else {
            return GetResult.get(entityService.deleteOne(entityClass, id));
        }
    }

    @Override
    public Integer updateById(IEntityClass entityClass, Long id, Map<String, Object> body) {
        if (autoRetry) {
            String conflictKey = entityClass.code() + id;
            return retryExecute(conflictKey, () -> entityService.updateById(entityClass, id, body));
        } else {
            return GetResult.get(entityService.updateById(entityClass, id, body));
        }
    }

    @Override
    public Integer updateByCondition(IEntityClass entityClass, ConditionQueryRequest condition, Map<String, Object> body) {
        return GetResult.get(entityService.updateByCondition(entityClass, condition, body));
    }

    @Override
    public Integer replaceById(IEntityClass entityClass, Long id, Map<String, Object> body) {

        if (autoRetry) {
            String conflictKey = entityClass.code() + id;
            return retryExecute(conflictKey, () -> entityService.replaceById(entityClass, id, body));
        } else {
            return GetResult.get(entityService.replaceById(entityClass, id, body));
        }
    }

    @Override
    public DataCollection<Map<String, Object>> findByCondition(IEntityClass entityClass, ConditionQueryRequest condition) {
        return GetResult.getList(entityService.findByCondition(entityClass, condition));
    }

    @Override
    public DataCollection<Record> findRecordsByCondition(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition) {
        return GetResult.getList(entityService.findRecordsByCondition(entityClass, ids, condition));
    }

    @Override
    public DataCollection<Map<String, Object>> findByConditionWithIds(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition) {
        return GetResult.getList(entityService.findByConditionWithIds(entityClass, ids, condition));
    }

    @Override
    public Long create(IEntityClass entityClass, Map<String, Object> body) {
        return GetResult.get(entityService.create(entityClass, body));
    }

    @Override
    public Integer count(IEntityClass entityClass, ConditionQueryRequest condition) {
        return entityService.count(entityClass, condition);
    }

    @Override
    public List<IEntityClass> loadSonByCode(String bocode, String tenantId) {
        return entityService.loadSonByCode(bocode, tenantId);
    }

    @Override
    public List<IEntityClass> loadSonByCode(String bocode, String tenantId, String version) {
        return entityService.loadSonByCode(bocode, tenantId, version);
    }

    @Override
    public List<IEntityClass> getEntityClasss() {
        return entityService.getEntityClasss();
    }
}
