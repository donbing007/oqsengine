package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import org.apache.metamodel.UpdateableDataContext;

import java.util.List;
import java.util.Optional;

/**
 * metadata repository
 */
public interface MetadataRepository {

    BoItem getBoDetailById(String id);

    /**
     *
     * @param moduleUpResult
     * @param tenantId
     * @param appId
     */
    void save(ModuleUpResult moduleUpResult, String tenantId, String appId);

    Optional<IEntityClass> load(String tenantId, String appCode, String boId);

    Optional<IEntityClass> load(String tenantId, String appCode, String boId, String version);

    Optional<IEntityClass> loadByCode(String tenantId, String appCode, String boId, String version);

    Optional<IEntityClass> loadByCode(String tenantId, String appCode, String boCode);

    List<IEntityClass> findSubEntitiesById(String tenantId, String appCode, String boId);

    List<IEntityClass> findSubEntitiesById(String tenantId, String appCode, String boId, String version);

    List<IEntityClass> findSubEntitiesByCode(String tenantId, String appCode, String boCode);

    List<IEntityClass> findSubEntitiesByCode(String tenantId, String appCode, String boCode, String version);

    void clearAllBoIdRelated(String boId, Long moduleId, UpdateableDataContext dc);

    SimpleBoItem findOneById(String boId);

    SimpleBoItem findOneById(String boId, String version);

    List<IEntityClass> findAllEntities();

    CurrentVersion currentVersion();
}
