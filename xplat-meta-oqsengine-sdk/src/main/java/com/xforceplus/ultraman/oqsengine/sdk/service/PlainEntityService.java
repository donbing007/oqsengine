package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.vo.DataCollection;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface PlainEntityService {

    IEntityClass load(String boId);

    IEntityClass load(String boId, String version);

    IEntityClass loadByCode(String bocode);

    IEntityClass loadByCode(String bocode, String version);

    <T> T transactionalExecute(Callable<T> supplier);

    Map<String, Object> findOne(IEntityClass entityClass, long id);

    <T> T retryExecute(String key, Supplier<Either<String, T>> supplier);

    Integer deleteOne(IEntityClass entityClass, Long id);

    Integer updateById(IEntityClass entityClass, Long id, Map<String, Object> body);

    Integer updateByCondition(IEntityClass entityClass, ConditionQueryRequest condition, Map<String, Object> body);

    Integer replaceById(IEntityClass entityClass, Long id, Map<String, Object> body);

    DataCollection<Map<String, Object>> findByCondition(IEntityClass entityClass, ConditionQueryRequest condition);

    DataCollection<Record> findRecordsByCondition(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition);

    DataCollection<Map<String, Object>>  findByConditionWithIds(IEntityClass entityClass, List<Long> ids, ConditionQueryRequest condition);

    Long create(IEntityClass entityClass, Map<String, Object> body);

    Integer count(IEntityClass entityClass, ConditionQueryRequest condition);

    List<IEntityClass> loadSonByCode(String bocode, String tenantId);

    List<IEntityClass> loadSonByCode(String bocode, String tenantId, String version);

    List<IEntityClass> getEntityClasss();
}
