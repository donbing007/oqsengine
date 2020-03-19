package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;


import com.xforceplus.ultraman.metadata.grpc.Api;
import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.Field;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.SimpleBoItem;
import com.xforceplus.ultraman.oqsengine.sdk.util.FieldHelper;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ApiItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.FieldItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.SoloItem;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.metamodel.UpdateSummary;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils.getRowValue;
import static com.xforceplus.ultraman.oqsengine.sdk.util.FieldHelper.toEntityClassFieldFromRel;

/**
 * TODO abstract this class with pojo
 */
public class MetadataRepositoryInMemoryImpl implements MetadataRepository {

    private List<Map<String, ?>> boStore = new ArrayList<>();

    private List<Map<String, ?>> apiStore = new ArrayList<>();

    private List<Map<String, ?>> fieldStore = new ArrayList<>();

    private List<Map<String, ?>> RelationStore = new ArrayList<>();

    private UpdateableDataContext dc;

    public MetadataRepositoryInMemoryImpl() {

        //TODO typed column name

        SimpleTableDef boTableDef = new SimpleTableDef("bos", new String[]{"id", "code", "parentId"});
        TableDataProvider boTableDataProvider = new MapTableDataProvider(boTableDef, boStore);

        SimpleTableDef ApiTableDef = new SimpleTableDef("apis", new String[]{"boId", "url", "method", "code"});
        TableDataProvider apiTableDataProvider = new MapTableDataProvider(ApiTableDef, apiStore);

        SimpleTableDef fieldTableDef = new SimpleTableDef("fields", new String[]{"boId"
                , "id"
                , "code", "displayType", "editable", "enumCode", "maxLength", "name", "required", "fieldType", "searchable", "precision"});
        TableDataProvider fieldTableDataProvider = new MapTableDataProvider(fieldTableDef, fieldStore);

        /**
         * relation table
         */
        SimpleTableDef relationTableDef = new SimpleTableDef("rels"
                , new String[]{
                      "id"
                    , "boId"
                    //onetomany manytoone onetoone
                    , "relType"
                    , "identity"
                    , "joinBoId"
                });

        TableDataProvider relationTableDataProvider = new MapTableDataProvider(relationTableDef, RelationStore);

        dc = new PojoDataContext("metadata", boTableDataProvider
                , apiTableDataProvider
                , fieldTableDataProvider
                , relationTableDataProvider);

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
            fieldItem.setType(getRowValue(row, "fieldType").map(String::valueOf).orElse(""));
            fieldItem.setSearchable(getRowValue(row, "searchable").map(String::valueOf).orElse(""));
            fieldItem.setPrecision(getRowValue(row, "precision").map(String::valueOf).orElse(""));
            //TODO
            fieldItem.setRelationshipEntity(null);
            items.add(fieldItem);
        }

        return items;
    }

    @Override
    public BoItem getBoDetailById(String id) {

        DataSet boDetails = dc.query().from("bos")
                .selectAll()
                .where("id").eq(id)
                .execute();

        if(boDetails.next()){
            Row ds = boDetails.getRow();
            DataSet apis = dc.query()
                    .from("apis")
                    .selectAll().where("boId").eq(id).execute();

            Map<String, ApiItem> apiItemMap = toApiItemMap(apis);

            DataSet fields = dc.query()
                    .from("fields")
                    .selectAll()
                    .where("boId").eq(id).execute();

            List<FieldItem> fieldItemList = toFieldItemList(fields);

            //deal with rel
            DataSet rels = dc.query()
                    .from("rels")
                    .selectAll()
                    .where("boId").eq(id).execute();

            List<Row> rows = rels.toRows();

            List<String> relIds = rows
                    .stream()
                    .map(x -> RowUtils.getRowValue(x, "joinBoId")
                            .map(String::valueOf).orElse(""))
                    .collect(Collectors.toList());

            List<FieldItem> relField = this.loadRelationField(rows, row -> {

                String joinBoId = RowUtils.getRowValue(row, "joinBoId")
                                          .map(String::valueOf)
                                          .orElse("");

                DataSet boDs = dc.query().from("bos")
                        .selectAll()
                        .where("id").eq(joinBoId)
                        .execute();

                if(boDs.next()){

                    Row bo = boDs.getRow();
                    String boCode = RowUtils.getRowValue(bo, "code")
                            .map(String::valueOf)
                            .orElse("");

                    SoloItem soloItem = new SoloItem();
                    soloItem.setId(Long.valueOf(joinBoId));

                    return new FieldItem(
                              boCode.concat(".id")
                            , boCode.concat(".id")
                            , FieldType.LONG.getType()
                            , ""
                            , "false"
                            , "true"
                            , "false"
                            , null
                            , null
                            , "0"
                            , soloItem);
                }
                return null;
            });

            List<FieldItem> fieldTotalItems = new LinkedList<>();
            fieldTotalItems.addAll(fieldItemList);
            fieldTotalItems.addAll(relField);

            BoItem boItem = new BoItem();
            boItem.setApi(apiItemMap);
            boItem.setFields(fieldTotalItems);
            boItem.setParentEntityId(
                    RowUtils.getRowValue(ds, "parentId")
                    .map(String::valueOf).orElse(""));
            boItem.setSubEntities(relIds);



            return boItem;
        }
        return null;
    }

    /**
     *
     * @param moduleUpResult
     * @param tenantId
     * @param appId
     */
    @Override
    public void save(ModuleUpResult moduleUpResult, String tenantId, String appId) {

        moduleUpResult.getBoUpsList().forEach(boUp ->  {

            //TODO
            clearAllBoIdRelated(boUp.getId());

            //insert bo
            insertBo(boUp);
        });
    }


    /**
     * no more parent
     * no relation
     * no relation fields
     * @return
     */
    private Optional<IEntityClass> loadParentEntityClass(String boId){

        DataSet boDs = dc.query()
                .from("bos")
                .selectAll().where("id").eq(boId)
                .execute();
        if(boDs.next()){
            Row row = boDs.getRow();

            String code = RowUtils.getRowValue(row, "code").map(String::valueOf).orElse("");
            return Optional.of(new EntityClass(Long.valueOf(boId), code, Collections.emptyList()
                    , Collections.emptyList(), null, loadFields(boId)));
        }

        return Optional.empty();
    }

    /**
     * load related entity class
     * @param boId
     * @return
     */
    private Optional<Tuple2<Relation, IEntityClass>> loadRelationEntityClass(String boId, Row relRow, String mainBoCode){

        String relationType = RowUtils.getRowValue(relRow, "relType")
                            .map(String::valueOf)
                            .orElse("");
        Long joinBoId = RowUtils.getRowValue(relRow, "joinBoId")
                            .map(String::valueOf)
                            .map(Long::valueOf)
                            .orElse(0L);

        return findOneById("bos", boId).map(row -> {
            Optional<IEntityClass> parentEntityClass = RowUtils
                    .getRowValue(row, "parentId")
                    .map(String::valueOf)
                    .flatMap(this::loadParentEntityClass);

            String subCode = RowUtils.getRowValue(row, "code").map(String::valueOf).orElse("");

            List<IEntityField> listFields = new LinkedList<>();

            //assemble relation Field
            com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field field;
            Relation relation;
            if(relationType.equalsIgnoreCase("onetoone")
                    || relationType.equalsIgnoreCase("manytoone")){
                //Field is from main id
                field = toEntityClassFieldFromRel(relRow, subCode);
                relation = new Relation(subCode, joinBoId, relationType, true, field);

            }else{
                //relation is onetomany
                field = toEntityClassFieldFromRel(relRow, mainBoCode);
                relation = new Relation(mainBoCode, joinBoId, relationType, true, field);
                listFields.add(field);
            }

            listFields.addAll(loadFields(boId));
            //assemble entity class
            IEntityClass entityClass = new EntityClass(Long.valueOf(boId)
                    , subCode
                    , Collections.emptyList()
                    , Collections.emptyList()
                    , parentEntityClass.orElse(null)
                    , listFields);

            return Tuple.of(relation, entityClass);
        });
    }


    @Override
    public Optional<EntityClass> loadByCode(String tenantId, String appCode, String boCode) {

        DataSet boDs = dc.query()
                .from("bos")
                .selectAll()
                .where("code").eq(boCode)
                .execute();

        if(boDs.next()) {
            return toEntityClass(boDs.getRow());
        }else{
            return Optional.empty();
        }
    }

    @Override
    public List<EntityClass> findSubEntitiesById(String tenantId, String appId, String parentId) {

        DataSet boDs = dc.query()
                .from("bos")
                .selectAll()
                .where("parentId")
                    .eq(parentId)
                .execute();

        List<Row> rows = boDs.toRows();

        return rows.stream().map(this::toEntityClass)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());
    }


    @Override
    public List<EntityClass> findSubEntitiesByCode(String tenantId, String appId, String parentCode) {

        DataSet boDs = dc.query()
                .from("bos")
                .selectAll()
                .where("code")
                .eq(parentCode)
                .execute();

        if(boDs.next()){
            String id = RowUtils.getRowValue(boDs.getRow(), "id").map(String::valueOf).orElse("");
            return findSubEntitiesById(tenantId, appId, id);
        }

        return Collections.emptyList();
    }

    /**
     * TODO nested object
     * @param tenantId
     * @param appCode
     * @param boId
     * @return
     */
    @Override
    public Optional<EntityClass> load(String tenantId, String appCode, String boId) {

        DataSet boDs = dc.query()
                .from("bos")
                .selectAll().where("id").eq(boId)
                .execute();
        if(boDs.next()) {
            return toEntityClass(boDs.getRow());
        }else{
            return Optional.empty();
        }
    }

    private Optional<EntityClass> toEntityClass(Row row){
            String code = RowUtils.getRowValue(row, "code").map(String::valueOf).orElse("");
            String boId = RowUtils.getRowValue(row, "id").map(String::valueOf).orElse("0");

            List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField> fields = loadFields(boId);

            //build up parentClass

            String parentId = RowUtils.getRowValue(row, "parentId").map(String::valueOf).orElse("");

            Optional<IEntityClass> parentEntityClassOp = loadParentEntityClass(parentId);

            //deal relation Classes
            DataSet relDs = dc.query()
                    .from("rels")
                    .selectAll().where("boId")
                    .eq(boId)
                    .execute();

            List<Row> relsRows = relDs.toRows();

            List<Tuple2<Relation, IEntityClass>> relatedEntityClassList = relsRows.stream().map(relRow -> {
                Optional<String> relatedBoIdOp = RowUtils.getRowValue(relRow, "joinBoId").map(String::valueOf);
                return relatedBoIdOp.flatMap(x -> {
                    return loadRelationEntityClass(x, relRow, code);
                });
            }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

            //deal Relation

            List<IEntityClass> entityClassList = new LinkedList<>();
            List<Relation> relationList = new LinkedList<>();

            List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField> allFields = new LinkedList<>();
            allFields.addAll(fields);

            relatedEntityClassList.forEach(tuple -> {
                entityClassList.add(tuple._2());
                relationList.add(tuple._1());
            });

            List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field> refFields = loadRelationField(relsRows);
            allFields.addAll(refFields);

            EntityClass entityClass = new EntityClass(Long.valueOf(boId)
                    , code, relationList, entityClassList
                    , parentEntityClassOp.orElse(null), allFields);
            return Optional.of(entityClass);
    }

    private List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField> loadFields(String id){
        DataSet fieldDs = dc.query().from("fields")
                .selectAll().where("boId").eq(id).execute();
        return fieldDs.toRows().stream()
                .map(FieldHelper::toEntityClassField)
                .collect(Collectors.toList());
    }

    /**
     * isSub => turn many to one to filed on sub
     * @param id
     * @return
     */
    private List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field> loadRelationField(String id){
        //load onetoone and many to one
        DataSet relDs = dc.query().from("rels")
                        .selectAll().where("boId").eq(id)
                        .execute();
        return loadRelationField(relDs.toRows());
    }

    /**
     * TODO
     * @param relations
     * @return
     */
    private List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field> loadRelationField(List<Row> relations){
        //relation to field
        return relations.stream().filter(row -> {
            return RowUtils.getRowValue(row,"relType")
                    .map(String::valueOf)
                    .filter(type -> type.equalsIgnoreCase("onetoone") || type.equalsIgnoreCase("manytoone"))
                    .isPresent();
        }).map(row -> {
            //get joinBoId
            Optional<Row> joinBoOp = findOneById("bos",
                    RowUtils.getRowValue(row, "joinBoId").map(String::valueOf).orElse(""));
            if(joinBoOp.isPresent()) {
                String code =joinBoOp
                        .flatMap(x -> RowUtils.getRowValue(x, "code")
                                .map(String::valueOf)).orElse("");
                return toEntityClassFieldFromRel(row, code);
            } else {
                return null;
            }
        }).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private <U> List<U> loadRelationField(List<Row> relations, Function<Row, U> mapper){
        return relations.stream().filter(row -> {
            return RowUtils.getRowValue(row,"relType")
                    .map(String::valueOf)
                    .filter(type -> type.equalsIgnoreCase("onetoone")
                            || type.equalsIgnoreCase("manytoone"))
                    .isPresent();
        }).map(mapper).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    //maybe useless
    private List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field> loadRelationFieldForSub(String id
            , String subId, String code) {
        //load onetoone and many to one
        DataSet relDs = dc.query().from("rels")
                .selectAll()
                .where("joinBoId").eq(id)
                .and("boId").eq(subId)
                .and("relType").eq("OneToMany")
                .execute();

        //to relDs
        return relDs.toRows().stream()
                .map(row -> {
                    return toEntityClassFieldFromRel(row, code);
        }).collect(Collectors.toList());
    }


    private Table getTable(String tableName){
        return dc.getTableByQualifiedLabel("metadata." + tableName);
    }

    private Optional<Row> findOneById(String tableName, String id){
        DataSet ds = dc.query().from(tableName)
                .selectAll()
                .where("id").eq(id)
                .execute();

        if(ds.next()){
            return Optional.ofNullable(ds.getRow());
        }

        return Optional.empty();
    }

    /**
     * nothing todo with the related entity
     * @param boId
     */
    @Override
    synchronized public void clearAllBoIdRelated(String boId){
        UpdateSummary updateSummary = dc.executeUpdate(callback -> {
            callback.deleteFrom(getTable("bos")).where("id").eq(boId).execute();
            callback.deleteFrom(getTable("apis")).where("boId").eq(boId).execute();
            callback.deleteFrom(getTable("fields")).where("boId").eq(boId).execute();
            callback.deleteFrom(getTable("rels")).where("boId").eq(boId).execute();
        });
    }

    //TODO typed converter
    @Override
    public SimpleBoItem findOneById(String boId) {
        DataSet boDs = dc.query()
                .from("bos")
                .selectAll().where("id").eq(boId)
                .execute();
        if(boDs.next()) {
            SimpleBoItem simpleBoItem = new SimpleBoItem();
            Row row = boDs.getRow();
            simpleBoItem.setCode(RowUtils.getRowValue(row, "code").map(String::valueOf).orElse(""));
            simpleBoItem.setParentId(RowUtils.getRowValue(row, "parentId").map(String::valueOf).orElse(""));
            simpleBoItem.setId(boId);
            return simpleBoItem;
        }else{
            return null;
        }
    }

    synchronized private void insertBoTable(String id, String code, String parentId) {
        InsertInto insert = new InsertInto(getTable("bos"))
                .value("id", id)
                .value("code", code)
                .value("parentId", parentId);
        dc.executeUpdate(insert);
    }

    /**
     * bo maybe
     * @param boUp
     */
    synchronized private void insertBo(BoUp boUp){

        insertBoTable(boUp.getId(), boUp.getCode(), boUp.getParentBoId());

        //save relations
        boUp.getRelationsList().forEach(rel -> {
            InsertInto insertRel = new InsertInto(getTable("rels"))
                    .value("id", rel.getId())
                    .value("boId", rel.getBoId())
                    .value("joinBoId", rel.getJoinBoId())
                    .value("identity", rel.getIdentity())
                    .value("relType", rel.getRelationType());
            dc.executeUpdate(insertRel);
        });

        //insert apis
        boUp.getApisList().forEach(api -> {
            insertApi( api, boUp.getId());
        });

        //insert fields
        boUp.getFieldsList().forEach(field -> {
            insertField(field, boUp.getId());
        });

        //maybe not need
        //insert sub bo
        //save if not exist
        boUp.getBoUpsList().stream()
                .filter(relatedBo ->  !findOneById("bos", relatedBo.getId()).isPresent())
                .forEach(relatedBo -> {
                    insertBoTable(relatedBo.getId(), relatedBo.getCode(), relatedBo.getParentBoId());

                    //save fields
                    //insert apis
                    relatedBo.getApisList().forEach(api -> {
                        insertApi(api, relatedBo.getId());
                    });

                    //insert fields
                    relatedBo.getFieldsList().forEach(field -> {
                        insertField(field, relatedBo.getId());
                    });
                });
    }

    synchronized private void insertField(Field field, String boId){

        String editable = field.getEditable();
        String searchable = field.getSearchable();

        //todo formatter
        if("1".equals(field.getEditable())){
            editable = "true";
        }

        if("1".equals(field.getSearchable())){
            searchable = "true";
        }


        InsertInto insert = new InsertInto(getTable("fields"))
                .value("boId", boId)
                .value("id", field.getId())
                .value("code", field.getCode())
                .value("displayType", field.getDisplayType())
                .value("editable", editable)
                .value("enumCode", field.getEnumCode())
                .value("maxLength", field.getMaxLength())
                .value("name", field.getName())
                .value("required", field.getRequired())
                .value("fieldType", field.getFieldType())
                .value("searchable", searchable)
                .value("precision", field.getPrecision());
        dc.executeUpdate(insert);
    }

    synchronized private void insertApi(Api api, String boId){
        InsertInto insert = new InsertInto(getTable("apis"))
                .value("boId", boId)
                .value("url", api.getUrl())
                .value("code", api.getCode())
                .value("method", api.getMethod());
        dc.executeUpdate(insert);
    }
}
