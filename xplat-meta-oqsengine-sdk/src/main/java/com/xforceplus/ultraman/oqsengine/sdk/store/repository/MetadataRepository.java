package com.xforceplus.ultraman.oqsengine.sdk.store.repository;

import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;

import java.util.Optional;

public interface MetadataRepository {

    BoItem getBoDetailById(String id);

    //save
    void save(ModuleUpResult moduleUpResult, String tenantId, String appId);

    Optional<EntityClass> load(String tenantId, String appCode, String boId);

    Optional<EntityClass> loadByCode(String tenantId, String appCode, String boCode);
}
