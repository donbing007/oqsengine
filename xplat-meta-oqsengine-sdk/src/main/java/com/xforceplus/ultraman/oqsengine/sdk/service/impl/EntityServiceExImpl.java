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
import com.xforceplus.ultraman.oqsengine.sdk.handler.EntityMetaHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.ContextService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import io.vavr.control.Either;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.*;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TRANSACTION_KEY;

public class EntityServiceExImpl implements EntityServiceEx {

    private final ContextService contextService;

    private final EntityServiceClient entityServiceClient;

    public EntityServiceExImpl(ContextService contextService, EntityServiceClient entityServiceClient) {
        this.contextService = contextService;
        this.entityServiceClient = entityServiceClient;
    }

    @Autowired
    private PageBoMapLocalStore pageBoMapLocalStore;

    @Autowired
    private EntityMetaHandler entityMetaHandler;

    @Override
    public Either<String, IEntity> create(EntityClass entityClass, Map<String, Object> body) {
        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> buildBuilder = entityServiceClient.build();

        if(transId != null){
            buildBuilder.addHeader("transaction-id", transId);
        }

        //处理系统字段的逻辑-add by wz
        body = entityMetaHandler.insertFill(entityClass,body);

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

    @Override
    public List<UltPageBoItem> findPageBos(String pageCode, String tenantId) {
        DataSet ds = null;
        if(!StringUtils.isEmpty(pageCode)) {

            List<Row> trows = new ArrayList<>();
            if (!StringUtils.isEmpty(tenantId)){
                ds = pageBoMapLocalStore.query().selectAll()
                        .where("code")
                        .eq(pageCode)
                        .and("tenantId")
                        .eq(tenantId)
                        .execute();
                trows = ds.toRows();
            }
            if (ds!=null && trows!=null && trows.size() > 0){
                ResponseList<UltPageBoItem> items = trows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));
                return items;
            }else {
                ds = pageBoMapLocalStore.query().selectAll()
                        .where("code")
                        .eq(pageCode)
                        .execute();
                List<Row> rows = ds.toRows();
                ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));
                return items;
            }
        }else {
            return null;
        }
    }

    private UltPageBoItem toUltPageBos(Row row){
        UltPageBoItem ultPageBoItem = new UltPageBoItem();
        ultPageBoItem.setId(Long.parseLong(RowUtils.getRowValue(row, "settingId").map(Object::toString).orElse("")));
        ultPageBoItem.setPageId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultPageBoItem.setBoCode(RowUtils.getRowValue(row, "boCode").map(Object::toString).orElse(""));
        if (!"".equals(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse(""))){
            ultPageBoItem.setTenantId(Long.parseLong(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse("")));
        }
        ultPageBoItem.setTenantName(RowUtils.getRowValue(row, "tenantName").map(Object::toString).orElse(""));
        ultPageBoItem.setBoName(RowUtils.getRowValue(row, "boName").map(Object::toString).orElse(""));
        ultPageBoItem.setRemark(RowUtils.getRowValue(row, "remark").map(Object::toString).orElse(""));
        ultPageBoItem.setCode(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        return ultPageBoItem;
    }
}
