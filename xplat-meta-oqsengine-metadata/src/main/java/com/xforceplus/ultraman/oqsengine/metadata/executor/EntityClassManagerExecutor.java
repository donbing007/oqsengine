package com.xforceplus.ultraman.oqsengine.metadata.executor;

import com.xforceplus.ultraman.oqsengine.meta.common.exception.MetaSyncClientException;
import com.xforceplus.ultraman.oqsengine.meta.common.proto.EntityClassSyncRspProto;
import com.xforceplus.ultraman.oqsengine.meta.provider.outter.SyncExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.metadata.cache.ICacheExecutor;
import com.xforceplus.ultraman.oqsengine.metadata.dto.EntityClassStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.meta.common.exception.Code.BUSINESS_HANDLER_ERROR;
import static com.xforceplus.ultraman.oqsengine.metadata.utils.EntityClassStorageConvert.fromProtoBuffer;

/**
 * desc :
 * name : EntityClassManagerExecutor
 *
 * @author : xujia
 * date : 2021/2/9
 * @since : 1.8
 */
public class EntityClassManagerExecutor implements SyncExecutor, MetaManager {

    @Resource
    private ICacheExecutor cacheExecutor;

    @Override
    public boolean sync(String appId, int version, EntityClassSyncRspProto entityClassSyncRspProto) {

        // step1 prepare
        int expiredVersion = -1;
        if (cacheExecutor.prepare(appId, version)) {
            try {
                expiredVersion = cacheExecutor.version(appId);

                // step2 convert to storage
                List<EntityClassStorage> entityClassStorageList = convert(version, entityClassSyncRspProto);

                // step3 update new Hash in redis
                if (!cacheExecutor.save(appId, version, entityClassStorageList)) {
                    throw new MetaSyncClientException(
                            String.format("save batches failed, appId : [%s], version : [%d]", appId, version), false
                    );
                }

                // todo add expiredVersion to expiredList

                return true;
            } catch (Exception e) {
                return false;
            } finally {
                cacheExecutor.endPrepare(appId);
            }

        }



        return false;
    }

    @Override
    public int version(String appId) {

        return cacheExecutor.version(appId);
    }

    @Override
    public IEntityClass load(long id) {
        Map<Long, EntityClassStorage> entityClassStorageMaps = cacheExecutor.read(id);
        return toEntityClass(entityClassStorageMaps);
    }

    @Override
    public int need(String appId) {

        int version = version(appId);
        if (version <= 0) {
            //  todo await unit get version
        }
        return version;
    }

    /**
     * 将protoBuf转为EntityClassStorage列表
     * @param version
     * @param entityClassSyncRspProto
     * @return
     */
    private List<EntityClassStorage> convert(int version, EntityClassSyncRspProto entityClassSyncRspProto) {
        Map<Long, EntityClassStorage> temp = entityClassSyncRspProto.getEntityClassesList().stream().map(
                ecs -> {
                    EntityClassStorage e = fromProtoBuffer(ecs, EntityClassStorage.class);
                    e.setVersion(version);
                    return e;
                }
        ).collect(Collectors.toMap(EntityClassStorage::getId, s1 -> s1,  (s1, s2) -> s1));

        return temp.values().stream().peek(
                v -> {
                    Long fatherId = v.getFatherId();
                    if (null != fatherId) {
                        while (null != fatherId) {
                            EntityClassStorage entityClassStorage = temp.get(v.getFatherId());
                            if (null == entityClassStorage) {
                                throw new MetaSyncClientException(
                                        String.format("father entityClass : [%d] missed.", v.getFatherId()), BUSINESS_HANDLER_ERROR.ordinal());
                            }
                            v.addAncestors(v.getFatherId());
                            fatherId = entityClassStorage.getFatherId();
                        }
                    }
                }
        ).collect(Collectors.toList());
    }

    private IEntityClass toEntityClass(Map<Long, EntityClassStorage> entityClassStorageMaps) {
        return null;
    }
}
