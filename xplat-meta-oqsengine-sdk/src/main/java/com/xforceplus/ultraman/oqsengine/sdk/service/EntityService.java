package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * user api
 */
public interface EntityService {

    Optional<EntityClass> load(String boId);

    Optional<EntityClass> load(String boId, String version);

    Optional<EntityClass> loadByCode(String bocode);

    Optional<EntityClass> loadByCode(String bocode, String version);

    <T> Either<String, T> transactionalExecute(Callable<T> supplier);

    Either<String, Map<String, Object>> findOne(IEntityClass entityClass, long id);

    <T> Either<String, T> retryExecute(String key, Supplier<Either<String, T>> supplier);

    Either<String, Integer> deleteOne(IEntityClass entityClass, Long id);

    Either<String, Integer> updateById(IEntityClass entityClass, Long id, Map<String, Object> body);

    Either<String, Integer> updateByCondition(IEntityClass entityClass, ConditionQueryRequest condition, Map<String, Object> body);

    Either<String, Integer> replaceById(IEntityClass entityClass, Long id, Map<String, Object> body);

    Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByCondition(IEntityClass entityClass, ConditionQueryRequest condition);

    Either<String, Tuple2<Integer, List<Record>>> findRecordsByCondition(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition);

    Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByConditionWithIds(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition);

    Either<String, Long> create(IEntityClass entityClass, Map<String, Object> body);

    Integer count(IEntityClass entityClass, ConditionQueryRequest condition);

    List<EntityClass> loadSonByCode(String bocode, String tenantId);

    List<EntityClass> loadSonByCode(String bocode, String tenantId, String version);

    List<EntityClass> getEntityClasss();
}
