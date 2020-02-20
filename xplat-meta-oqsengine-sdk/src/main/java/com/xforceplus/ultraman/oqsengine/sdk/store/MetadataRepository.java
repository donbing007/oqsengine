package com.xforceplus.ultraman.oqsengine.sdk.store;

import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;

public interface MetadataRepository {

    BoItem getBoDetailById(String id);

    //save
    void save(ModuleUpResult moduleUpResult, String tenantId, String appId);
}
