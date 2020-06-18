package com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl;


import com.xforceplus.ultraman.metadata.grpc.Api;
import com.xforceplus.ultraman.metadata.grpc.BoUp;
import com.xforceplus.ultraman.metadata.grpc.Field;
import com.xforceplus.ultraman.metadata.grpc.ModuleUpResult;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldLikeRelationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.CurrentVersion;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.SimpleBoItem;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.VersionService;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.impl.tables.*;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xforceplus.ultraman.oqsengine.sdk.store.RowUtils.getRowValue;

/**
 * TODO abstract this class with pojo
 * TODO refactor this more typed
 * TODO since the store is not thread-safe using read / write lock instead of sync keywords
 */
public class MetadataRepositoryInMemoryImpl implements MetadataRepository {

    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    private VersionService versionService;

    private int maxVersion = 3;

    Logger logger = LoggerFactory.getLogger(MetadataRepository.class);

    public MetadataRepositoryInMemoryImpl() {
        this(-1, null);
    }

    public MetadataRepositoryInMemoryImpl(int maxVersion, ApplicationEventPublisher publisher) {

        if (maxVersion > 0) {
            this.maxVersion = maxVersion;
        }

        this.versionService = new DefaultVersionService(this.maxVersion, publisher);
        this.versionService.initVersionedDC(this.maxVersion, this::generateNewDC);
    }

    /**
     * generate the new pojo updateContext
     *
     * @return
     */
    private UpdateableDataContext generateNewDC() {

        TableDataProvider[] tableDataProviders = Stream.of(
                new ModuleTable()
                , new BoTable()
                , new ApiTable()
                , new FieldTable()
                , new RelationTable())
                .map(x -> {
                    SimpleTableDef tableDef = new SimpleTableDef(x.name(), x.columns());
                    return new MapTableDataProvider(tableDef, x.getStore());
                }).toArray(TableDataProvider[]::new);

        return new PojoDataContext("metadata", tableDataProviders);
    }

    private Map<String, ApiItem> toApiItemMap(DataSet apis) {
        Map<String, ApiItem> map = new HashMap<>();
        while (apis.next()) {
            Row row = apis.getRow();
            String code = getRowValue(row, ApiTable.CODE).map(String::valueOf).orElse("");
            String url = getRowValue(row, ApiTable.URL).map(String::valueOf).orElse("");
            String method = getRowValue(row, ApiTable.METHOD).map(String::valueOf).orElse("");
            ApiItem apiItem = new ApiItem(url, method);
            map.put(code, apiItem);
        }
        return map;
    }

    private List<FieldItem> toFieldItemList(DataSet fields) {
        List<FieldItem> items = new ArrayList<>();

        while (fields.next()) {
            Row row = fields.getRow();
            FieldItem fieldItem = new FieldItem();
            fieldItem.setCode(getRowValue(row, FieldTable.CODE).map(String::valueOf).orElse(""));
            fieldItem.setDisplayType(getRowValue(row, FieldTable.DISPLAY_TYPE).map(String::valueOf).orElse(""));
            fieldItem.setEditable(getRowValue(row, FieldTable.EDITABLE).map(String::valueOf).orElse(""));
            fieldItem.setEnumCode(getRowValue(row, FieldTable.ENUM_CODE).map(String::valueOf).orElse(""));
            fieldItem.setMaxLength(getRowValue(row, FieldTable.MAX_LENGTH).map(String::valueOf).orElse(""));
            fieldItem.setName(getRowValue(row, FieldTable.NAME).map(String::valueOf).orElse(""));
            fieldItem.setRequired(getRowValue(row, FieldTable.REQUIRED).map(String::valueOf).orElse(""));
            fieldItem.setType(getRowValue(row, FieldTable.FIELD_TYPE).map(String::valueOf).orElse(""));
            fieldItem.setSearchable(getRowValue(row, FieldTable.SEARCHABLE).map(String::valueOf).orElse(""));
            fieldItem.setDictId(getRowValue(row, FieldTable.DICT_ID).map(String::valueOf).orElse(""));
            fieldItem.setDefaultValue(getRowValue(row, FieldTable.DEFAULT_VALUE).map(String::valueOf).orElse(""));
            fieldItem.setPrecision(getRowValue(row, FieldTable.PRECISION).map(String::valueOf).orElse(""));
            //TODO
            fieldItem.setRelationshipEntity(null);
            items.add(fieldItem);
        }

        return items;
    }

    private <T> T read(Supplier<T> supplier) {
        rwLock.readLock().lock();
        try {
            return supplier.get();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    private void write(Supplier<Void> supplier) {
        rwLock.writeLock().lock();
        try {
            supplier.get();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    @Override
    public BoItem getBoDetailById(String id) {

        return read(() -> {

            UpdateableDataContext dc = versionService.getCurrentVersionDCForBoById(Long.parseLong(id));

            if (dc == null) return null;

            DataSet boDetails = dc.query().from(BoTable.TABLE_NAME)
                    .selectAll()
                    .where(BoTable.ID).eq(id)
                    .execute();

            if (boDetails.next()) {
                Row ds = boDetails.getRow();
                DataSet apis = dc.query()
                        .from(ApiTable.TABLE_NAME)
                        .selectAll().where(ApiTable.BO_ID)
                        .eq(id).execute();

                Map<String, ApiItem> apiItemMap = toApiItemMap(apis);

                DataSet fields = dc.query()
                        .from(FieldTable.TABLE_NAME)
                        .selectAll()
                        .where(FieldTable.BO_ID).eq(id).execute();

                List<FieldItem> fieldItemList = toFieldItemList(fields);

                //deal with rel
                DataSet rels = dc.query()
                        .from(RelationTable.TABLE_NAME)
                        .selectAll()
                        .where(RelationTable.BO_ID).eq(id).execute();

                List<Row> rows = rels.toRows();

                List<String> relIds = rows
                        .stream()
                        .map(x -> RowUtils.getRowValue(x, RelationTable.JOIN_BO_ID)
                                .map(String::valueOf).orElse(""))
                        .collect(Collectors.toList());

                List<FieldItem> relField = this.loadRelationField(rows, row -> {

                    String joinBoId = RowUtils.getRowValue(row, RelationTable.JOIN_BO_ID)
                            .map(String::valueOf)
                            .orElse("");

                    DataSet boDs = dc.query().from(BoTable.TABLE_NAME)
                            .selectAll()
                            .where(BoTable.ID).eq(joinBoId)
                            .execute();

                    if (boDs.next()) {

                        Row bo = boDs.getRow();
                        String boCode = RowUtils.getRowValue(bo, BoTable.CODE)
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
                                , ""
                                , ""
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
                        RowUtils.getRowValue(ds, BoTable.PARENT_ID)
                                .map(String::valueOf).orElse(""));
                boItem.setSubEntities(relIds);


                return boItem;
            }
            return null;
        });
    }

    /**
     * @param moduleUpResult
     * @param tenantId
     * @param appId
     */
    @Override
    public void save(ModuleUpResult moduleUpResult, String tenantId, String appId) {

        write(() -> {

            String version = moduleUpResult.getVersion();
            long moduleId = moduleUpResult.getId();

            logger.info("------- Version {} Got For {}", version, moduleId);

            versionService.saveModule(moduleId, version
                    , moduleUpResult.getBoUpsList().stream().flatMap(x -> {

                        Stream<BoNode> single = Stream.of(new BoNode(x.getCode(), Long.parseLong(x.getId())));
                        Stream<BoNode> nestOne = x.getBoUpsList().stream().map(sub -> new BoNode(sub.getCode(), Long.parseLong(sub.getId())));
                        return Stream.concat(single, nestOne);
                    }).collect(Collectors.toList()));

            UpdateableDataContext versionedDCForModule = versionService.getVersionedDCForModule(moduleId, version);

            moduleUpResult.getBoUpsList().forEach(boUp -> {
                //clear bo
                clearAllBoIdRelated(boUp.getId(), moduleId, versionedDCForModule);
                logger.info("Clear Bo:{}", boUp.getId());

                //insert bo
                insertBo(moduleId, boUp, versionedDCForModule);
                logger.info("Insert Bo:{}",  boUp.getId());
            });
            return null;
        });
    }

    /**
     * no more parent
     * no relation
     * no relation fields
     *
     * @return
     */
    private Optional<IEntityClass> loadParentEntityClass(String boId, UpdateableDataContext dc) {

        return read(() -> Optional.ofNullable(dc).flatMap(x -> {
            DataSet boDs = dc.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll().where(BoTable.ID).eq(boId)
                    .execute();
            if (boDs.next()) {
                Row row = boDs.getRow();

                String code = RowUtils.getRowValue(row, BoTable.CODE).map(String::valueOf).orElse("");
                return Optional.of(new EntityClass(Long.valueOf(boId), code, Collections.emptyList()
                        , Collections.emptyList()
                        , null
                        , loadFields(boId, dc)));
            }

            return Optional.empty();
        }));
    }

    /**
     * load related entity class
     *
     * @param boId
     * @return
     */
    private Optional<Tuple2<Relation, IEntityClass>> loadRelationEntityClass(String boId, Row relRow, String mainBoCode
            , UpdateableDataContext contextDC) {

        return read(() -> {
            String relationType = RowUtils.getRowValue(relRow, RelationTable.REL_TYPE)
                    .map(String::valueOf)
                    .orElse("");

            String name = RowUtils.getRowValue(relRow, RelationTable.REL_NAME)
                    .map(String::valueOf)
                    .orElse("");

            Long joinBoId = RowUtils.getRowValue(relRow, RelationTable.JOIN_BO_ID)
                    .map(String::valueOf)
                    .map(Long::valueOf)
                    .orElse(0L);

            Long relId = RowUtils.getRowValue(relRow, RelationTable.ID)
                    .map(String::valueOf)
                    .map(Long::valueOf)
                    .orElse(0L);

            return findOneById(BoTable.TABLE_NAME, boId, contextDC).map(row -> {
                Optional<IEntityClass> parentEntityClass = RowUtils
                        .getRowValue(row, BoTable.PARENT_ID)
                        .map(String::valueOf)
                        .flatMap(x -> this.loadParentEntityClass(x, contextDC));

                String subCode = RowUtils.getRowValue(row, BoTable.CODE)
                        .map(String::valueOf).orElse("");

                List<IEntityField> listFields = new LinkedList<>();

                //assemble relation Field
                /**
                 *  used as dto
                 *   public Relation(Long id, String name, String entityClassName, String ownerClassName, String relationType) {
                 */
                Relation relation = new Relation(relId, name, joinBoId, subCode, mainBoCode, relationType);

                FieldLikeRelationType.from(relationType).ifPresent(x -> {
                    IEntityField relField = x.getField(relation);
                    relation.setEntityField(relField);

                    if (!x.isOwnerSide()) {
                        listFields.add(relField);
                    }
                });

                listFields.addAll(loadFields(boId, contextDC));
                //assemble entity class
                IEntityClass entityClass = new EntityClass(Long.valueOf(boId)
                        , subCode
                        , Collections.emptyList()
                        , Collections.emptyList()
                        , parentEntityClass.orElse(null)
                        , listFields);

                return Tuple.of(relation, entityClass);
            });
        });
}

    @Override
    public Optional<EntityClass> loadByCode(String tenantId, String appCode, String boCode) {

        return read(() -> {
            UpdateableDataContext dc = versionService.getCurrentVersionDCForBoByCode(boCode);
            return Optional.ofNullable(dc).flatMap(contextDC ->
                    this.loadByCode(tenantId, appCode, boCode, contextDC));
        });
    }

    /**
     * load by version
     *
     * @param tenantId
     * @param appCode
     * @param boCode
     * @param contextDC
     * @return
     */
    public Optional<EntityClass> loadByCode(String tenantId, String appCode, String boCode, UpdateableDataContext contextDC) {
        return read(() -> {
            DataSet boDs = contextDC.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll()
                    .where(BoTable.CODE).eq(boCode)
                    .execute();

            if (boDs.next()) {
                return toEntityClass(boDs.getRow(), contextDC);
            } else {
                return Optional.empty();
            }
        });
    }

    @Override
    public List<EntityClass> findSubEntitiesById(String tenantId, String appId, String parentId) {
        return Optional.ofNullable(versionService.getCurrentVersionDCForBoById(Long.parseLong(parentId)))
                .map(x -> this.findSubEntitiesById(tenantId, appId, parentId, x))
                .orElseGet(Collections::emptyList);
    }

    @Override
    public List<EntityClass> findSubEntitiesById(String tenantId, String appId, String parentId, String version) {
        return read(() -> {
            return Optional.ofNullable(versionService.getVersionedDCForBoById(Long.parseLong(parentId), version))
                    .map(x -> this.findSubEntitiesById(tenantId, appId, parentId, x))
                    .orElseGet(Collections::emptyList);
        });
    }

    private List<EntityClass> findSubEntitiesById(String tenantId, String appId, String parentId, UpdateableDataContext contextDC) {
        return read(() -> {
            DataSet boDs = contextDC.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll()
                    .where(BoTable.PARENT_ID)
                    .eq(parentId)
                    .execute();

            List<Row> rows = boDs.toRows();

            return rows.stream().map(row -> toEntityClass(row, contextDC))
                    .filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());
        });
    }

    private List<EntityClass> findSubEntitiesByCode(String tenantId, String appId, String parentCode, UpdateableDataContext contextDC) {

        return read(() -> {
            DataSet boDs = contextDC.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll()
                    .where(BoTable.CODE)
                    .eq(parentCode)
                    .execute();

            if (boDs.next()) {
                String id = RowUtils
                        .getRowValue(boDs.getRow(), BoTable.ID)
                        .map(String::valueOf).orElse("");
                return findSubEntitiesById(tenantId, appId, id);
            }

            return Collections.emptyList();
        });
    }

    @Override
    public List<EntityClass> findSubEntitiesByCode(String tenantId, String appId, String parentCode) {

        return read(() -> {
            UpdateableDataContext contextDC = versionService.getCurrentVersionDCForBoByCode(parentCode);

            return Optional.ofNullable(contextDC)
                    .map(x -> findSubEntitiesByCode(tenantId, appId, parentCode, x))
                    .orElseGet(Collections::emptyList);
        });
    }

    @Override
    public List<EntityClass> findSubEntitiesByCode(String tenantId, String appId, String parentCode, String version) {

        return read(() -> {
            UpdateableDataContext contextDC = versionService.getVersionedDCForBoByCode(parentCode, version);

            return Optional.ofNullable(contextDC)
                    .map(x -> findSubEntitiesByCode(tenantId, appId, parentCode, x))
                    .orElseGet(Collections::emptyList);
        });
    }

    /**
     * load an entity class
     *
     * @param tenantId
     * @param appCode
     * @param boId
     * @return
     */
    @Override
    public Optional<EntityClass> load(String tenantId, String appCode, String boId) {

        return read(() -> {
            UpdateableDataContext dc = versionService.getCurrentVersionDCForBoById(Long.parseLong(boId));
            return Optional.ofNullable(dc).flatMap(x -> this.load(tenantId, appCode, boId, x));
        });

    }

    @Override
    public Optional<EntityClass> load(String tenantId, String appCode, String boId, String version) {
        return read(() -> {
            UpdateableDataContext contextDc = versionService.getVersionedDCForBoById(Long.parseLong(boId), version);
            return Optional.ofNullable(contextDc).flatMap(x -> load(tenantId, appCode, boId, x));
        });
    }

    @Override
    public Optional<EntityClass> loadByCode(String tenantId, String appCode, String boCode, String version) {

        return read(() -> {
            UpdateableDataContext contextDc = versionService.getVersionedDCForBoByCode(boCode, version);
            return Optional.ofNullable(contextDc).flatMap(x -> loadByCode(tenantId, appCode, boCode, x));
        });
    }

    private Optional<EntityClass> load(String tenantId, String appCode, String boId, UpdateableDataContext contextDC) {

        return read(() -> {
            DataSet boDs = contextDC.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll().where(BoTable.ID).eq(boId)
                    .execute();
            if (boDs.next()) {
                return toEntityClass(boDs.getRow(), contextDC);
            } else {
                return Optional.empty();
            }
        });
    }

    /**
     * how to build a entity class
     * check if has parent --> load parent info
     * |-> deal parent fields
     * find relation --> build relation entity
     * |-> build relation fields
     * deal with self fields
     *
     * @param row
     * @return
     */
    private Optional<EntityClass> toEntityClass(Row row, UpdateableDataContext contextDC) {

        return read(() -> {

            String code = RowUtils.getRowValue(row, "code").map(String::valueOf).orElse("");
            String boId = RowUtils.getRowValue(row, "id").map(String::valueOf).orElse("0");
            String name = RowUtils.getRowValue(row, "name").map(String::valueOf).orElse("");

            List<com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField> fields = loadFields(boId, contextDC);

            //build up parentClass
            String parentId = RowUtils.getRowValue(row, "parentId").map(String::valueOf).orElse("");

            Optional<IEntityClass> parentEntityClassOp = loadParentEntityClass(parentId, contextDC);

            //deal relation Classes
            DataSet relDs = contextDC.query()
                    .from("rels")
                    .selectAll().where("boId")
                    .eq(boId)
                    .execute();

            List<Row> relsRows = relDs.toRows();

            List<Tuple2<Relation, IEntityClass>> relatedEntityClassList = relsRows.stream().map(relRow -> {
                Optional<String> relatedBoIdOp = RowUtils.getRowValue(relRow, "joinBoId").map(String::valueOf);
                return relatedBoIdOp.flatMap(x -> {
                    return loadRelationEntityClass(x, relRow, code, contextDC);
                });
            }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

            //deal Relation
            List<IEntityClass> entityClassList = new LinkedList<>();
            List<Relation> relationList = new LinkedList<>();

            List<IEntityField> allFields = new LinkedList<>();
            allFields.addAll(fields);

            relatedEntityClassList.forEach(tuple -> {
                entityClassList.add(tuple._2());
                relationList.add(tuple._1());
            });

            //append all rel fields to fields
            relationList.stream().filter(x -> {
                return FieldLikeRelationType.from(x.getRelationType())
                        .map(FieldLikeRelationType::isOwnerSide)
                        .orElse(false);
            }).forEach(x -> allFields.add(x.getEntityField()));

            EntityClass entityClass = new EntityClass(Long.valueOf(boId)
                    , code, name, relationList, entityClassList
                    , parentEntityClassOp.orElse(null), allFields);
            return Optional.of(entityClass);
        });
    }

    private <U> List<U> loadRelationField(List<Row> relations, Function<Row, U> mapper) {
        return relations.stream().filter(row -> {
            return RowUtils.getRowValue(row, RelationTable.REL_TYPE)
                    .map(String::valueOf)
                    .filter(type -> type.equalsIgnoreCase("onetoone")
                            || type.equalsIgnoreCase("manytoone"))
                    .isPresent();
        }).map(mapper).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Table getTable(String tableName, UpdateableDataContext contextDC) {
        return read(() -> {
            return contextDC.getTableByQualifiedLabel("metadata." + tableName);
        });
    }

    private Optional<Row> findOneById(String tableName, String id, UpdateableDataContext dc) {

        return read(() -> {
            DataSet ds = dc.query().from(tableName)
                    .selectAll()
                    .where("id").eq(id)
                    .execute();

            if (ds.next()) {
                return Optional.ofNullable(ds.getRow());
            }
            return Optional.empty();
        });
    }

    /**
     * nothing todo with the related entity
     *
     * @param boId
     */
    @Override
    public void clearAllBoIdRelated(String boId, Long moduleId, UpdateableDataContext dc) {
        write(() -> {
            UpdateSummary updateSummary = dc.executeUpdate(callback -> {
                callback.deleteFrom(getTable(BoTable.TABLE_NAME, dc)).where(BoTable.ID).eq(boId).execute();
                callback.deleteFrom(getTable(ApiTable.TABLE_NAME, dc)).where(ApiTable.BO_ID).eq(boId).execute();
                callback.deleteFrom(getTable(FieldTable.TABLE_NAME, dc)).where(FieldTable.BO_ID).eq(boId).execute();
                callback.deleteFrom(getTable(RelationTable.TABLE_NAME, dc)).where(RelationTable.BO_ID).eq(boId).execute();
            });

            return null;
        });
    }

    @Override
    public SimpleBoItem findOneById(String boId) {
        return read(() -> {
            UpdateableDataContext dc = versionService.getCurrentVersionDCForBoById(Long.parseLong(boId));
            return Optional.ofNullable(dc).map(x -> this.findOneById(boId, dc)).orElse(null);
        });
    }

    @Override
    public SimpleBoItem findOneById(String boId, String version) {
        return read(() -> {
            UpdateableDataContext dc = versionService.getVersionedDCForBoById(Long.parseLong(boId), version);
            return Optional.ofNullable(dc).map(x -> this.findOneById(boId, dc)).orElse(null);
        });
    }

    public SimpleBoItem findOneById(String boId, UpdateableDataContext contextDC) {

        return read(() -> {
            DataSet boDs = contextDC.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll().where(BoTable.ID).eq(boId)
                    .execute();
            if (boDs.next()) {
                SimpleBoItem simpleBoItem = new SimpleBoItem();
                Row row = boDs.getRow();
                simpleBoItem.setCode(RowUtils.getRowValue(row, BoTable.CODE).map(String::valueOf).orElse(""));
                simpleBoItem.setParentId(RowUtils.getRowValue(row, BoTable.PARENT_ID).map(String::valueOf).orElse(""));
                simpleBoItem.setId(boId);
                simpleBoItem.setCname(RowUtils.getRowValue(row, BoTable.NAME).map(String::valueOf).orElse(""));
                return simpleBoItem;
            } else {
                return null;
            }
        });
    }

    //TODO
    @Override
    public List<EntityClass> findAllEntities() {

        return read(() -> {
            return versionService.getBoModuleMapping().entrySet().stream().map(x -> {
                Long boId = x.getKey().getId();
                LinkedList<Tuple2<Long, String>> value = x.getValue();
                String version = value.getLast()._2();

                UpdateableDataContext versionedDCForBoId = versionService.getVersionedDCForBoById(boId, version);
                return load("", "", String.valueOf(boId), versionedDCForBoId);
            }).filter(Optional::isPresent).map(Optional::get)
                    .collect(Collectors.toList());
        });
    }

    private List<EntityClass> findAllEntities(UpdateableDataContext contextDC) {

        return read(() -> {

            DataSet boDs = contextDC.query()
                    .from(BoTable.TABLE_NAME)
                    .selectAll()
                    .execute();

            List<Row> rows = boDs.toRows();

            return rows.stream().map(x -> this.toEntityClass(x, contextDC))
                    .filter(Optional::isPresent)
                    .map(Optional::get).collect(Collectors.toList());
        });
    }

    //TODO
    @Override
    public CurrentVersion currentVersion() {
        return read(() -> {
            return new CurrentVersion(versionService.getCurrentVersion());
        });
    }

    private void insertBoTable(String id, String moduleId, String code, String parentId, String name, UpdateableDataContext contextDC) {
        write(() -> {

            InsertInto insert = new InsertInto(getTable(BoTable.TABLE_NAME, contextDC))
                    .value(BoTable.ID, id)
                    .value(BoTable.CODE, code)
                    .value(BoTable.PARENT_ID, parentId)
                    .value(BoTable.NAME, name)
                    .value(BoTable.MODULE_ID, moduleId);
            contextDC.executeUpdate(insert);
            return null;
        });
    }

    /**
     * bo insert
     *
     * @param boUp
     */
    private void insertBo(long moduleId, BoUp boUp, UpdateableDataContext contextDC) {

        write(() -> {

            insertBoTable(boUp.getId()
                    , String.valueOf(moduleId)
                    , boUp.getCode()
                    , boUp.getParentBoId()
                    , boUp.getName()
                    , contextDC);

            //save relations
            boUp.getRelationsList().forEach(rel -> {
                InsertInto insertRel = new InsertInto(
                        getTable(RelationTable.TABLE_NAME, contextDC))
                        .value(RelationTable.ID, rel.getId())
                        .value(RelationTable.BO_ID, rel.getBoId())
                        .value(RelationTable.JOIN_BO_ID, rel.getJoinBoId())
                        .value(RelationTable.IDENTITY, rel.getIdentity())
                        .value(RelationTable.REL_TYPE, rel.getRelationType())
                        .value(RelationTable.REL_NAME, rel.getRelName());
                contextDC.executeUpdate(insertRel);
            });

            //insert apis
            boUp.getApisList().forEach(api -> {
                insertApi(api, boUp.getId(), contextDC);
            });

            //insert fields
            boUp.getFieldsList().forEach(field -> {
                insertField(field, boUp.getId(), contextDC);
            });

            //maybe not need
            //insert sub bo
            //save if not exist
            boUp.getBoUpsList().stream()
                    .filter(relatedBo -> !findOneById(BoTable.TABLE_NAME, relatedBo.getId(), contextDC).isPresent())
                    .forEach(relatedBo -> {
                        insertBoTable(relatedBo.getId()
                                , String.valueOf(moduleId)
                                , relatedBo.getCode()
                                , relatedBo.getParentBoId()
                                , relatedBo.getName()
                                , contextDC);

                        //save fields
                        //insert apis
                        relatedBo.getApisList().forEach(api -> {
                            insertApi(api, relatedBo.getId(), contextDC);
                        });

                        //insert fields
                        relatedBo.getFieldsList().forEach(field -> {
                            insertField(field, relatedBo.getId(), contextDC);
                        });
                    });
            return null;
        });
    }

    private void insertField(Field field, String boId, UpdateableDataContext contextDC) {
        write(() -> {

            String editable = field.getEditable();
            String searchable = field.getSearchable();
            String identifier = field.getIdentifier();

            //todo formatter
            if ("1".equals(editable)) {
                editable = "true";
            }

            if ("1".equals(searchable)) {
                searchable = "true";
            }

            if ("1".equals(identifier)) {
                identifier = "true";
            }

            //TODO replace with table
            InsertInto insert = new InsertInto(getTable(FieldTable.TABLE_NAME, contextDC))
                    .value(FieldTable.BO_ID, boId)
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
                    .value("dictId", field.getDictId())
                    .value("defaultValue", field.getDefaultValue())
                    .value("precision", String.valueOf(field.getPrecision()))
                    .value("identifier", identifier)
                    .value("validateRule", field.getValidateRule());
            contextDC.executeUpdate(insert);
            return null;
        });
    }

    private void insertApi(Api api, String boId, UpdateableDataContext dc) {
        write(() -> {
            InsertInto insert = new InsertInto(getTable(ApiTable.TABLE_NAME, dc))
                    .value(ApiTable.BO_ID, boId)
                    .value(ApiTable.URL, api.getUrl())
                    .value(ApiTable.CODE, api.getCode())
                    .value(ApiTable.METHOD, api.getMethod());
            dc.executeUpdate(insert);
            return null;
        });
    }

//------------------------------------------------------------------------------------------------------------

    private List<IEntityField> loadFields(String id, UpdateableDataContext dc) {
        return read(() -> {
            DataSet fieldDs = dc.query().from(FieldTable.TABLE_NAME)
                    .selectAll().where(FieldTable.BO_ID).eq(id).execute();
            return fieldDs.toRows().stream()
                    .map(FieldHelper::toEntityClassField)
                    .collect(Collectors.toList());
        });
    }
}
