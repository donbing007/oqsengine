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

    Optional<IEntityClass> load(String boId);

    Optional<IEntityClass> load(String boId, String version);

    Optional<IEntityClass> loadByCode(String bocode);

    Optional<IEntityClass> loadByCode(String bocode, String version);

    <T> Either<String, T> transactionalExecute(Callable<T> supplier);

    Either<String, Map<String, Object>> findOne(IEntityClass entityClass, long id);

    <T> Either<String, T> retryExecute(String key, Supplier<Either<String, T>> supplier);

//    /**
//     * force delete
//     * @param entityClass
//     * @param id
//     * @return
//     */
//    Either<String, Integer> forceDeleteOne(IEntityClass entityClass, Long id);

    /**
     * TODO
     * optimise delete
     * @param entityClass
     * @param id
     * @return
     */
    Either<String, Integer> deleteOne(IEntityClass entityClass, Long id);

    Either<String, Integer> updateById(IEntityClass entityClass, Long id, Map<String, Object> body);

    Either<String, Integer> updateByCondition(IEntityClass entityClass, ConditionQueryRequest condition, Map<String, Object> body);

    Either<String, Integer> replaceById(IEntityClass entityClass, Long id, Map<String, Object> body);

    Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByCondition(IEntityClass entityClass, ConditionQueryRequest condition);

    Either<String, Tuple2<Integer, List<Record>>> findRecordsByCondition(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition);

    Either<String, Tuple2<Integer, List<Map<String, Object>>>> findByConditionWithIds(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition);

    Either<String, Long> create(IEntityClass entityClass, Map<String, Object> body);

    Integer count(IEntityClass entityClass, ConditionQueryRequest condition);

    List<IEntityClass> loadSonByCode(String bocode, String tenantId);

    List<IEntityClass> loadSonByCode(String bocode, String tenantId, String version);

    List<IEntityClass> getEntityClasss();
}
