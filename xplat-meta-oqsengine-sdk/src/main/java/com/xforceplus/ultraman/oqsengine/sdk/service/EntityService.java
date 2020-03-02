package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * user api
 */
public interface EntityService  {

    Optional<EntityClass> load(String boId);

    Optional<EntityClass> loadByCode(String bocode);

    <T> Either<String, T> transactionalExecute(Callable<T> supplier);

    Either<String, Map<String, Object>> findOne(EntityClass entityClass, long id);

    Either<String, Integer> deleteOne(EntityClass entityClass, Long id);

    Either<String, Integer> updateById(EntityClass entityClass, Long id, Map<String, Object> body);

    Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByCondition(EntityClass entityClass, ConditionQueryRequest condition);

    Either<String, Long> create(EntityClass entityClass, Map<String, Object> body);
}
