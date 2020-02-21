package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class EntityService {

    @Autowired
    private MetadataRepository metadataRepository;

    public Optional<EntityClass> load(String tenantId, String appCode, String boId){
        return metadataRepository.load(tenantId, appCode, boId);
    }

    public Map<String, String> findOne(EntityClass entityClass, Condition condition) {
        return null;
    }
}
