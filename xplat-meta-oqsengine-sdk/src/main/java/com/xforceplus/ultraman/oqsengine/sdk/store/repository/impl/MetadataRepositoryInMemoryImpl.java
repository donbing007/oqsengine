package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;


import com.xforceplus.ultraman.metadata.grpc.Api;
import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.Field;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.sdk.store.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ApiItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.FieldItem;
import org.apache.metamodel.UpdateCallback;
import org.apache.metamodel.UpdateScript;
import org.apache.metamodel.UpdateableDataContext;
import org.apache.metamodel.data.DataSet;
import org.apache.metamodel.data.Row;
import org.apache.metamodel.insert.InsertInto;
import org.apache.metamodel.pojo.MapTableDataProvider;
import org.apache.metamodel.pojo.PojoDataContext;
import org.apache.metamodel.pojo.TableDataProvider;
import org.apache.metamodel.schema.Table;
import org.apache.metamodel.util.SimpleTableDef;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils.getRowValue;


@Service
/**
 * TODO abstract this class with pojo
 */
public class MetadataRepositoryInMemoryImpl implements MetadataRepository {

    private List<Map<String, ?>> boStore = new ArrayList<>();

    private List<Map<String, ?>> apiStore = new ArrayList<>();

    private List<Map<String, ?>> fieldStore = new ArrayList<>();

    private UpdateableDataContext dc;

    public MetadataRepositoryInMemoryImpl() {


        SimpleTableDef boTableDef = new SimpleTableDef("bos", new String[]{"parentId", "id", "code"});
        TableDataProvider boTableDataProvider = new MapTableDataProvider(boTableDef, boStore);

        SimpleTableDef ApiTableDef = new SimpleTableDef("apis", new String[]{"boId", "url", "method", "code"});
        TableDataProvider apiTableDataProvider = new MapTableDataProvider(ApiTableDef, apiStore);

        SimpleTableDef fieldTableDef = new SimpleTableDef("fields", new String[]{"boId"
                , "id"
                , "code", "displayType", "editable", "enumCode", "maxLength", "name", "required", "type", "searchable"});
        TableDataProvider fieldTableDataProvider = new MapTableDataProvider(fieldTableDef, fieldStore);

        dc = new PojoDataContext("metadata", boTableDataProvider, apiTableDataProvider, fieldTableDataProvider);

    }

    private Map<String, ApiItem> toApiItemMap(DataSet apis){
        Map<String, ApiItem> map = new HashMap<>();
        while(apis.next()){
            Row row = apis.getRow();
            String code = getRowValue(row, "code").map(String::valueOf).orElse("");
            String url = getRowValue(row, "url").map(String::valueOf).orElse("");
            String method = getRowValue(row, "method").map(String::valueOf).orElse("");
            ApiItem apiItem = new ApiItem(url, method);
            map.put(code, apiItem);
        }

        return map;
    }


    private List<FieldItem> toFieldItemList(DataSet fields){
        List<FieldItem> items = new ArrayList<>();

        while(fields.next()){
            Row row = fields.getRow();
            FieldItem fieldItem = new FieldItem();
            fieldItem.setCode(getRowValue(row, "code").map(String::valueOf).orElse(""));
            fieldItem.setDisplayType(getRowValue(row, "displayType").map(String::valueOf).orElse(""));
            fieldItem.setEditable(getRowValue(row, "editable").map(String::valueOf).orElse(""));
            fieldItem.setEnumCode(getRowValue(row, "enumCode").map(String::valueOf).orElse(""));
            fieldItem.setMaxLength(getRowValue(row, "maxLength").map(String::valueOf).orElse(""));
            fieldItem.setName(getRowValue(row, "name").map(String::valueOf).orElse(""));
            fieldItem.setRequired(getRowValue(row, "required").map(String::valueOf).orElse(""));
            fieldItem.setType(getRowValue(row, "type").map(String::valueOf).orElse(""));
            fieldItem.setSearchable(getRowValue(row, "searchable").map(String::valueOf).orElse(""));

            //TODO
            fieldItem.setRelationshipEntity(null);
            items.add(fieldItem);
        }

        return items;
    }

    @Override
    public BoItem getBoDetailById(String id) {

        DataSet boDetails = dc.query().from("bos").selectAll().where("id").eq(id).execute();

        if(boDetails.next()){
            DataSet apis = dc.query()
                    .from("apis")
                    .selectAll().where("boId").eq(id).execute();

            Map<String, ApiItem> apiItemMap = toApiItemMap(apis);

            DataSet fields = dc.query()
                    .from("fields")
                    .selectAll()
                    .where("boId").eq(id).execute();

            List<FieldItem> fieldItemList = toFieldItemList(fields);

            BoItem boItem = new BoItem();
            boItem.setApi(apiItemMap);
            boItem.setFields(fieldItemList);
            //TODO not set
            boItem.setSubEntities(Collections.emptyList());

            return boItem;
        }
        return null;
    }

    @Override
    public void save(ModuleUpResult moduleUpResult, String tenantId, String appId) {
        moduleUpResult.getBoUpsList().forEach(boUp ->  {

            clearAllBoIdRelated(boUp.getId());
            //save bo
            boUp.getApisList().stream().forEach(api -> {
                insert("apis", api, boUp.getId());

            });

            boUp.getFieldsList().forEach(field -> {
                insert("fields", field, boUp.getId());
            });

            insert("bos", boUp);
        });
    }

    synchronized private void clearAllBoIdRelated(String boId){
        dc.executeUpdate(new UpdateScript() {
            public void run(UpdateCallback callback) {
                callback.deleteFrom(getTable("bos")).where("id").eq(boId).execute();
                callback.deleteFrom(getTable("apis")).where("boId").eq(boId).execute();
                callback.deleteFrom(getTable("fields")).where("boId").eq(boId).execute();
            };
        });
    }

    private Table getTable(String tableName){
        return dc.getTableByQualifiedLabel("metadata." + tableName);
    }

    //TODO nested not
    synchronized private void insert(String tableName, BoUp boUp){
        InsertInto insert = new InsertInto(getTable(tableName))
                .value("id", boUp.getId())
                .value("code", boUp.getCode());
        dc.executeUpdate(insert);
    }

    synchronized private void insert(String tableName, Field field, String boId){
        InsertInto insert = new InsertInto(dc.getTableByQualifiedLabel("metadata." + tableName ))
                .value("boId", boId)
                .value("code", field.getCode())
                .value("displayType", field.getDisplayType())
                .value("editable", field.getDisplayType())
                .value("enumCode", field.getEnumCode())
                .value("maxLength", field.getMaxLength())
                .value("name", field.getName())
                .value("required", field.getRequired())
                .value("type", field.getFieldType())
                .value("searchable", field.getSearchable());
        dc.executeUpdate(insert);
    }

    synchronized private void insert(String tableName, Api api, String boId){
        InsertInto insert = new InsertInto(dc.getTableByQualifiedLabel("metadata." + tableName ))
                .value("boId", boId)
                .value("url", api.getUrl())
                .value("code", api.getCode())
                .value("method", api.getMethod());

        dc.executeUpdate(insert);
    }
}
