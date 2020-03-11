package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.control.Either;

import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.*;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TRANSACTION_KEY;

public class EntityServiceExImpl implements EntityServiceEx {

    private final ContextService contextService;

    private final EntityServiceClient entityServiceClient;

    public EntityServiceExImpl(ContextService contextService, EntityServiceClient entityServiceClient) {
        this.contextService = contextService;
        this.entityServiceClient = entityServiceClient;
    }

    @Override
    public Either<String, IEntity> create(EntityClass entityClass, Map<String, Object> body) {
        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> buildBuilder = entityServiceClient.build();

        if(transId != null){
            buildBuilder.addHeader("transaction-id", transId);
        }

        OperationResult createResult = buildBuilder
                .invoke(toEntityUp(entityClass, null, body))
                .toCompletableFuture().join();

        if(createResult.getCode() == OperationResult.Code.OK){
            if(createResult.getIdsList().size() < 1 ) {
                return Either.left("未获得结果");
            }else{
//                return Either.right(createResult.getIdsList().get(0));
                IEntity entity = null;
                if(createResult.getIdsCount() == 1){
                    entity = new Entity(createResult.getIdsList().get(0), entityClass, new EntityValue(0L));
                }else if(createResult.getIdsCount() > 1){
                    entity = new Entity(createResult.getIdsList().get(0), entityClass, new EntityValue(0L)
                            , new EntityFamily(createResult.getIdsList().get(1), 0), 0);
                }else{
                    return Either.left(createResult.getMessage());
                }
                return Either.right(entity);
            }
        }else{
            //indicates
            return Either.left(createResult.getMessage());
        }
    }

    @Override
    public Either<String, Map<String, Object>> findOneByParentId(EntityClass entityClass, EntityClass subEntityClass, long id) {
        if(subEntityClass != null && subEntityClass.extendEntityClass() != null
                && entityClass != null && entityClass.id() == subEntityClass.extendEntityClass().id()){

            String transId = contextService.get(TRANSACTION_KEY);

            SingleResponseRequestBuilder<EntityUp, OperationResult> queryResultBuilder = entityServiceClient.selectOne();

            if(transId != null) {
                queryResultBuilder.addHeader("transaction-id", transId);
            }

            EntityUp entityUp = toEntityUpBuilder(entityClass, id)
                                .setSubEntityClass(toRawEntityUp(subEntityClass))
                                .build();

            OperationResult queryResult = queryResultBuilder.invoke(entityUp)
                    .toCompletableFuture().join();

            if( queryResult.getCode() == OperationResult.Code.OK ){
                if(queryResult.getTotalRow() > 0) {
                    return Either.right(toResultMap(entityClass, subEntityClass, queryResult.getQueryResultList().get(0)));
                } else {
                    return Either.left("未查询到记录");
                }
            }else{
                return Either.left(queryResult.getMessage());
            }
        }

        return Either.left("error parameters");
    }
}
