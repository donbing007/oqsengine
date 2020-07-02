package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.grpc.javadsl.SingleResponseRequestBuilder;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityFamily;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityValue;
import com.xforceplus.ultraman.oqsengine.sdk.EntityServiceClient;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.ValueUp;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityCreated;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleResultValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.DictMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.PageBoMapLocalStore;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseList;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.UltPageBoItem;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.sdk.util.EntityClassToGrpcConverter.*;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;

/**
 * entity service ex
 */
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
    private DictMapLocalStore dictMapLocalStore;

    @Autowired
    private HandleValueService handleValueService;

    @Autowired
    private HandleResultValueService handleResultValueService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Override
    public Either<String, IEntity> create(IEntityClass entityClass, Map<String, Object> body) {
        String transId = contextService.get(TRANSACTION_KEY);

        SingleResponseRequestBuilder<EntityUp, OperationResult> buildBuilder = entityServiceClient.build();

        if (transId != null) {
            buildBuilder.addHeader("transaction-id", transId);
        }
        List<ValueUp> valueUps = handleValueService.handlerValue(entityClass, body, OperationType.CREATE);

        OperationResult createResult = buildBuilder
            .invoke(toEntityUp(entityClass, null, valueUps))
            .toCompletableFuture().join();

        if (createResult.getCode() == OperationResult.Code.OK) {
            if (createResult.getIdsList().size() < 1) {
                return Either.left("未获得结果");
            } else {
                IEntity entity = null;
                if (createResult.getIdsCount() == 1) {
                    Long id = createResult.getIdsList().get(0);
                    entity = new Entity(id, entityClass, new EntityValue(0L));

                    publisher.publishEvent(buildCreatedEvent(entityClass, id, null, body));
                } else if (createResult.getIdsCount() > 1) {

                    Long id = createResult.getIdsList().get(0);

                    Long parentId = createResult.getIdsList().get(1);

                    entity = new Entity(createResult.getIdsList().get(0), entityClass, new EntityValue(0L)
                        , new EntityFamily(createResult.getIdsList().get(1), 0), 0);

                    publisher.publishEvent(buildCreatedEvent(entityClass, parentId, id, body));
                } else {
                    return Either.left(createResult.getMessage());
                }
                return Either.right(entity);
            }
        } else {
            //indicates
            return Either.left(createResult.getMessage());
        }
    }

    private Map<String, String> getContext() {
        Map<String, String> map = new HashMap<>();
        map.put(TENANTID_KEY.name(), contextService.get(TENANTID_KEY));
        map.put(TENANTCODE_KEY.name(), contextService.get(TENANTCODE_KEY));
        map.put(USERNAME.name(), contextService.get(USERNAME));
        map.put(USER_DISPLAYNAME.name(), contextService.get(USER_DISPLAYNAME));
        return map;
    }

    private EntityCreated buildCreatedEvent(IEntityClass entityClass, Long id, Long childId, Map<String, Object> data) {
        String code = entityClass.code();
        String parentCode = null;
        Map<String, String> context = getContext();
        if (entityClass.extendEntityClass() != null) {
            parentCode = entityClass.extendEntityClass().code();
        }

        return new EntityCreated(parentCode, code, id, childId, data, context);
    }

    @Override
    public Either<String, Map<String, Object>> findOneByParentId(IEntityClass entityClass, IEntityClass subEntityClass, long id) {
        if (subEntityClass != null && subEntityClass.extendEntityClass() != null
            && entityClass != null && entityClass.id() == subEntityClass.extendEntityClass().id()) {

            String transId = contextService.get(TRANSACTION_KEY);

            SingleResponseRequestBuilder<EntityUp, OperationResult> queryResultBuilder = entityServiceClient.selectOne();

            if (transId != null) {
                queryResultBuilder.addHeader("transaction-id", transId);
            }

            EntityUp entityUp = toEntityUpBuilder(entityClass, id)
                .setSubEntityClass(toRawEntityUp(subEntityClass))
                .build();

            OperationResult queryResult = queryResultBuilder.invoke(entityUp)
                .toCompletableFuture().join();

            if (queryResult.getCode() == OperationResult.Code.OK) {
                if (queryResult.getTotalRow() > 0) {
                    return Either.right(handleResultValueService.toRecord(subEntityClass, queryResult.getQueryResultList().get(0)).toMap(null));
                } else {
                    return Either.left("未查询到记录");
                }
            } else {
                return Either.left(queryResult.getMessage());
            }
        }

        return Either.left("error parameters");
    }

    @Override
    public List<UltPageBoItem> findPageBos(String pageCode, String tenantCode) {
        DataSet ds = null;
        if (!StringUtils.isEmpty(pageCode)) {

            List<Row> trows = new ArrayList<>();
            if (!StringUtils.isEmpty(tenantCode)) {
                ds = pageBoMapLocalStore.query().selectAll()
                    .where("code")
                    .eq(pageCode)
                    .and("tenantCode")
                    .eq(tenantCode)
                    .and("envStatus")
                    .eq("UP")
                    .execute();
                trows = ds.toRows();
            }
            if (ds != null && trows != null && trows.size() > 0) {
                ResponseList<UltPageBoItem> items = trows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));
                return items;
            } else {
                ds = pageBoMapLocalStore.query().selectAll()
                    .where("code")
                    .eq(pageCode)
                    .and("envStatus")
                    .eq("UP")
                    .and("tenantCode").isNull().or("tenantCode").eq("")
                    .execute();
                List<Row> rows = ds.toRows();
                ResponseList<UltPageBoItem> items = rows.stream().map(this::toUltPageBos).collect(Collectors.toCollection(ResponseList::new));
                return items;
            }
        } else {
            return null;
        }
    }

    @Override
    public List<DictItem> findDictItems(String enumId, String enumCode) {
        String tenantCode = contextService.get(TENANTCODE_KEY);
        DataSet ds = null;
        List<Row> rows = new ArrayList<Row>();
        if (StringUtils.isEmpty(enumCode)) {
            if (!StringUtils.isEmpty(tenantCode)) {
                //找到字典的code信息
                ds = dictMapLocalStore.query().selectAll()
                        .where("publishDictId")
                        .eq(enumId)
                        .execute();
                rows = ds.toRows();
                if (rows.size() > 0) {
                    String dictCode = RowUtils.getRowValue(rows.get(0), "dictCode")
                            .map(Object::toString).orElse("");
                    ds = dictMapLocalStore.query().selectAll()
                            .where("dictCode")
                            .eq(dictCode)
                            .and("tenantCode")
                            .eq(tenantCode)
                            .execute();
                }
            }
            if (ds != null) {
                rows = ds.toRows();
            }

            if (!(rows != null && rows.size() > 0)) {
                ds = dictMapLocalStore.query().selectAll()
                        .where("publishDictId")
                        .eq(enumId)
                        .execute();
                rows = ds.toRows();
            }
        } else {
            if (!StringUtils.isEmpty(tenantCode)) {
                if (!StringUtils.isEmpty(tenantCode)) {
                    //找到字典的code信息
                    ds = dictMapLocalStore.query().selectAll()
                            .where("publishDictId")
                            .eq(enumId)
                            .execute();
                    rows = ds.toRows();
                    if (rows.size() > 0) {
                        String dictCode = RowUtils.getRowValue(rows.get(0), "dictCode")
                                .map(Object::toString).orElse("");
                        ds = dictMapLocalStore.query().selectAll()
                                .where("dictCode")
                                .eq(dictCode)
                                .and("tenantCode")
                                .eq(tenantCode)
                                .and("code")
                                .eq(enumCode)
                                .execute();
                    }
                }
            }

            if (ds != null) {
                rows = ds.toRows();
            }

            if (!(rows != null && rows.size() > 0)) {
                ds = dictMapLocalStore.query().selectAll()
                        .where("publishDictId")
                        .eq(enumId)
                        .and("code")
                        .eq(enumCode)
                        .execute();
                rows = ds.toRows();
            }
        }
        List<DictItem> items = rows.stream().map(this::toDictItem).collect(Collectors.toCollection(ResponseList::new));
        return getMaxVersionList(items);
    }

    @Override
    public List<DictItem> findDictItemsByCode(String code, String enumCode) {

        String tenantCode = contextService.get(TENANTCODE_KEY);

        DataSet ds = null;
        List<Row> rows = new ArrayList<Row>();
        if (StringUtils.isEmpty(enumCode)) {
            if (!StringUtils.isEmpty(tenantCode)){
                ds = dictMapLocalStore.query().selectAll()
                        .where("dictCode")
                        .eq(code)
                        .and("tenantCode")
                        .eq(tenantCode)
                        .execute();
            }

            if (ds != null) {
                rows = ds.toRows();
            }

            if (!(rows != null && rows.size() > 0)) {
                ds = dictMapLocalStore.query().selectAll()
                        .where("dictCode")
                        .eq(code)
                        .execute();
                rows = ds.toRows();
            }
        } else {
            if (!StringUtils.isEmpty(tenantCode)) {
                ds = dictMapLocalStore.query().selectAll()
                        .where("dictCode")
                        .eq(code)
                        .and("tenantCode")
                        .eq(tenantCode)
                        .and("code")
                        .eq(enumCode)
                        .execute();
            }

            if (ds != null) {
                rows = ds.toRows();
            }

            if (!(rows != null && rows.size() > 0)) {
                ds = dictMapLocalStore.query().selectAll()
                        .where("dictCode")
                        .eq(code)
                        .and("code")
                        .eq(enumCode)
                        .execute();
                rows = ds.toRows();
            }
        }
        List<DictItem> items = rows.stream().map(this::toDictItem).collect(Collectors.toCollection(ResponseList::new));
        return getMaxVersionList(items);
    }

    private UltPageBoItem toUltPageBos(Row row) {
        UltPageBoItem ultPageBoItem = new UltPageBoItem();
        ultPageBoItem.setId(Long.parseLong(RowUtils.getRowValue(row, "settingId").map(Object::toString).orElse("")));
        ultPageBoItem.setPageId(Long.parseLong(RowUtils.getRowValue(row, "id").map(Object::toString).orElse("")));
        ultPageBoItem.setBoCode(RowUtils.getRowValue(row, "boCode").map(Object::toString).orElse(""));
        if (!"".equals(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse(""))) {
            ultPageBoItem.setTenantId(Long.parseLong(RowUtils.getRowValue(row, "tenantId").map(Object::toString).orElse("")));
        }

        if (!"".equals(RowUtils.getRowValue(row, "tenantCode").map(Object::toString).orElse(""))) {
            ultPageBoItem.setTenantCode(RowUtils.getRowValue(row, "tenantCode").map(Object::toString).orElse(""));
        }

        ultPageBoItem.setTenantName(RowUtils.getRowValue(row, "tenantName").map(Object::toString).orElse(""));
        ultPageBoItem.setBoName(RowUtils.getRowValue(row, "boName").map(Object::toString).orElse(""));
        ultPageBoItem.setRemark(RowUtils.getRowValue(row, "remark").map(Object::toString).orElse(""));
        ultPageBoItem.setCode(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        ultPageBoItem.setEnvStatus(RowUtils.getRowValue(row, "envStatus").map(Object::toString).orElse(""));
        return ultPageBoItem;
    }

    private DictItem toDictItem(Row row) {
        DictItem dictItem = new DictItem();
        dictItem.setText(RowUtils.getRowValue(row, "name").map(Object::toString).orElse(""));
        dictItem.setValue(RowUtils.getRowValue(row, "code").map(Object::toString).orElse(""));
        dictItem.setVersion(RowUtils.getRowValue(row, "version").map(Object::toString).orElse(""));
        return dictItem;
    }

    /**
     * 返回最大版本的字典数据
     * @param dictItems
     * @return
     */
    private List<DictItem> getMaxVersionList(List<DictItem> dictItems){
        List<DictItem> dictItemList = new ResponseList<>();
        if (dictItems.size() > 0){
            String maxVersion = dictItems.get(0).getVersion();
            for (int i = 0; i < dictItems.size(); i++) {
                if (compare(maxVersion, dictItems.get(i).getVersion()) < 0) {
                    dictItemList.clear();
                    dictItemList.add(dictItems.get(i));
                    maxVersion = dictItems.get(i).getVersion();
                } else {
                    if (maxVersion.equals(dictItems.get(i).getVersion())){
                        dictItemList.add(dictItems.get(i));
                    }
                }
            }
        }
        return dictItemList;
    }

    /**
     * 版本对比方法
     * @param v1
     * @param v2
     * @return
     */
    public int compare(String v1, String v2) {
        if (org.apache.commons.lang3.StringUtils.isEmpty(v1) && org.apache.commons.lang3.StringUtils.isEmpty(v2)) {
            return 0;
        } else if (org.apache.commons.lang3.StringUtils.isEmpty(v1)) {
            return -1;
        } else if (org.apache.commons.lang3.StringUtils.isEmpty(v2)) {
            return 1;
        }

        int [] varr1 = getVersionArray(v1);

        int [] varr2 = getVersionArray(v2);

        int lim = Math.min(varr1.length, varr2.length);

        int k = 0;
        while (k < lim) {
            if (varr1[k] == varr2[k]) {
                k++;
                continue;
            }
            return varr1[k] > varr2[k] ? 1 : -1;
        }

        if (varr1.length == varr2.length) {
            return 0;
        }

        return varr1.length > varr2.length ? 1 : -1;
    }

    public int [] getVersionArray(String v) {
        String [] sarray = org.apache.commons.lang3.StringUtils.split(v, ".");

        int [] varray = new int[sarray.length];

        int k = 0;

        while (k < sarray.length) {
            varray[k] = Integer.parseInt(sarray[k]);
            k++;
        }

        return varray;
    }
}
