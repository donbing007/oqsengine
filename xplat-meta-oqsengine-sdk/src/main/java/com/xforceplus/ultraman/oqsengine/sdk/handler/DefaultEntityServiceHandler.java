package com.xforceplus.ultraman.oqsengine.sdk.handler;

import akka.NotUsed;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relation;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.sdk.command.*;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityErrorExported;
import com.xforceplus.ultraman.oqsengine.sdk.event.EntityExported;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSink;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSource;
import com.xforceplus.ultraman.oqsengine.sdk.ui.DefaultUiService;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.DictItem;
import com.xforceplus.xplat.galaxy.framework.dispatcher.anno.QueryHandler;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.MetaData;
import com.xforceplus.xplat.galaxy.framework.dispatcher.messaging.QueryMessage;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * default ui service handler
 */
public class DefaultEntityServiceHandler implements DefaultUiService {

    @Autowired
    private EntityService entityService;

    @Autowired
    private ExportSource exportService;

    @Autowired
    private ExportSink exportSink;

    @Autowired
    private EntityServiceEx entityServiceEx;

    @Autowired
    private ActorMaterializer materializer;

    @Autowired
    private ApplicationEventPublisher publisher;

    private static final String MISSING_ENTITIES = "查询对象不存在";

    private Logger log = LoggerFactory.getLogger(DefaultUiService.class);

    private Optional<EntityClass> getEntityClass(MetaDataLikeCmd cmd) {
        return
                Optional
                        .ofNullable(cmd.version()).map(x -> {
                    return entityService.load(cmd.getBoId(), cmd.version());
                }).orElseGet(() -> entityService.load(cmd.getBoId()));
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Map<String, Object>> singleQuery(SingleQueryCmd cmd) {

        Optional<EntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.findOne(entityClassOp.get(), Long.parseLong(cmd.getId()));
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Integer> singleDelete(SingleDeleteCmd cmd) {
        Optional<EntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.deleteOne(entityClassOp.get(), Long.valueOf(cmd.getId()));
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Long> singleCreate(SingleCreateCmd cmd) {

        Optional<EntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.create(entityClassOp.get(), cmd.getBody());
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Integer> singleUpdate(SingleUpdateCmd cmd) {

        Optional<EntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.updateById(entityClassOp.get(), cmd.getId(), cmd.getBody());
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> conditionSearch(ConditionSearchCmd cmd) {

        Optional<EntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.findByCondition(entityClassOp.get(), cmd.getConditionQueryRequest());
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    /**
     * export command
     * either a error msg and a link
     * Notice this is a async calling
     *
     * @param message
     * @return
     */
    @QueryHandler(isDefault = true)
    @Override
    public CompletableFuture<Either<String, String>> conditionExport(QueryMessage<ConditionExportCmd, ?> message) {
        //TODO check this performance

        ConditionExportCmd cmd = message.getPayload();

        MetaData metaData = message.getMetaData();

        AtomicBoolean isFirstLine = new AtomicBoolean(true);

        String token = Optional.ofNullable(metaData.get("name")).map(Object::toString)
                .orElse(Optional.ofNullable(metaData.get("code")).map(Object::toString)
                        .orElse(cmd.getBoId())) + "-" + System.nanoTime();

        Sink<ByteString, CompletionStage<Tuple2<IOResult, String>>> fileSink = exportSink.getSink(token);

        ConditionQueryRequest request = cmd.getConditionQueryRequest();

        Set<String> keys = request.getStringKeys();

        Map<String, List<DictItem>> mapping = new HashMap<>();

        Map<String, Map<String, String>> searchTable = new HashMap<>();

        Optional<? extends IEntityClass> iEntityClassOp;

        if (cmd.getBoId() != null) {
            iEntityClassOp = entityService.load(cmd.getBoId());
        } else {
            iEntityClassOp = entityService.load(cmd.getBoId(), cmd.version());
        }

        if (!iEntityClassOp.isPresent()) {
            return CompletableFuture.completedFuture(Either.left(MISSING_ENTITIES));
        } else {

            try {
                IEntityClass entityClass = iEntityClassOp.get();

                byte[] bom = new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf};
                Source<ByteString, NotUsed> bomSource = Source.single(ByteString.fromArray(bom));

                Source<ByteString, NotUsed> content = exportService.source(entityClass, cmd.getConditionQueryRequest())
                        .map(record -> {
                            StringBuilder sb = new StringBuilder();
                            if (isFirstLine.get()) {
                                String header = record
                                        .stream(keys)
                                        .map(Tuple2::_1)
                                        .map(x -> Optional.ofNullable(x)
                                                .map(IEntityField::cnName)
                                                .orElse(""))
                                        .collect(Collectors.joining(","));
                                sb.append(header);
                                sb.append("\n");
                                isFirstLine.set(false);
                            }

                            //may have null conver to ""
                            String line = record
                                    .stream(keys)
                                    .map(x -> {

                                        IEntityField field = x._1();
                                        Object value = x._2();

                                        //TODO take field type in consideration?
                                        //convert enum to dict value
                                        // \t is a tricky for csv see
                                        //     https://qastack.cn/superuser/318420/formatting-a-comma-delimited-csv-to-force-excel-to-interpret-value-as-a-string
                                        return "\"\t" + getString(entityClass, field, value, mapping, searchTable) + "\"";
                                    })
                                    .collect(Collectors.joining(","));
                            sb.append(line);
                            sb.append("\n");
                            return sb.toString();

                        })
                        .map(x -> ByteString.fromString(x, StandardCharsets.UTF_8));

                CompletableFuture<Either<String, String>> syncCompleteResult = bomSource.concat(content)
                        .runWith(fileSink, materializer)
                        .toCompletableFuture().thenApply(x -> {

                            String downloadUrl = exportSink.getDownloadUrl(x._2());

                            Map<String, Object> context = new HashMap<>(metaData);
                            publisher.publishEvent(new EntityExported(context, downloadUrl, token, cmd.getExportType()));
                            return Either.<String, String>right(downloadUrl);
                        }).exceptionally(th -> {
                            Map<String, Object> context = new HashMap<>(metaData);
                            publisher.publishEvent(new EntityErrorExported(context, token, th.getMessage()));
                            return Either.left(th.getMessage());
                        });


                if ("sync".equalsIgnoreCase(cmd.getExportType())) {
                    return syncCompleteResult;
                } else {
                    return CompletableFuture.completedFuture(Either.right("请求完成"));
                }
            } catch (Exception ex) {
                log.error("{}", ex);
                return CompletableFuture.completedFuture(Either.left(ex.getMessage()));
            }
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, InputStream> importTemplate(GetImportTemplateCmd cmd) {

        String boId = cmd.getBoId();

        Optional<EntityClass> entityClass = entityService.load(boId);

        if (entityClass.isPresent()) {
            List<IEntityField> fields = entityClass.get().fields();

            String columns = fields.stream().map(IEntityField::name).collect(Collectors.joining(","));
            return Either.right(new ByteArrayInputStream(columns.getBytes(StandardCharsets.UTF_8)));

        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    /**
     * @param cmd
     * @return
     */
    @QueryHandler(isDefault = true)
    @Override
    public Either<String, String> batchImport(ImportCmd cmd) {

        String boId = cmd.getBoId();

        Optional<EntityClass> entityClassOp = entityService.load(boId);

        if (entityClassOp.isPresent()) {

            MultipartFile file = cmd.getFile();

            Either<String, String> ret;
            try {
                CSVParser parser = CSVParser.parse(file.getInputStream()
                        , StandardCharsets.UTF_8, CSVFormat.EXCEL);

                List<CSVRecord> list = parser.getRecords();

                Map<String, Object> map = new HashMap<>();


                if (list.size() > 1) {
                    CSVRecord header = list.get(0);

                    ret = entityService.transactionalExecute(() -> {
                        for (int i = 1; i < list.size(); i++) {
                            CSVRecord record = list.get(i);
                            for (int j = 0; j < header.size(); j++) {
                                map.put(header.get(j), StringUtils.isEmpty(record.get(j)) ? null : record.get(j));
                            }
                            entityService.create(entityClassOp.get(), map);
                        }
                        //TODO sum
                        return "ok";
                    });
                } else {
                    ret = Either.right("ok");
                }
                parser.close();
            } catch (IOException e) {
                e.printStackTrace();
                ret = Either.left(e.getMessage());
            }

            return ret;
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    /**
     * searchTable
     *
     * @return
     */
    private String getString(IEntityClass entityClass
            , IEntityField entityField, Object value
            , Map<String, List<DictItem>> mapping
            , Map<String, Map<String, String>> searchTable) {

        FieldType type = entityField.type();

        String retStr = null;

        String safeSourceValue = Optional.ofNullable(value).map(Object::toString).orElse("");

        switch (type) {
            case ENUM:
                String dictId = entityField.dictId();
                List<DictItem> items = mapping.get(dictId);
                if (items == null) {
                    List<DictItem> dictItems = entityServiceEx.findDictItems(dictId, null);
                    mapping.put(dictId, dictItems);
                    items = dictItems;
                }

                retStr = items.stream()
                        .filter(x -> x.getValue().equals(safeSourceValue))
                        .map(DictItem::getText)
                        .findAny().orElse("unknown");
                break;
            case STRINGS:
                String[] ids = safeSourceValue.split(",");
                Long fieldId = entityField.id();

                Optional<Long> relatedId = entityClass.relations().stream()
                        .filter(x -> x.getEntityField() != null)
                        .filter(x -> x.getEntityField().id() == fieldId)
                        .map(Relation::getEntityClassId).findAny();

                if (relatedId.isPresent()) {
                    Optional<EntityClass> relatedEntityOp = entityService.load(relatedId.get().toString());
                    if (relatedEntityOp.isPresent()) {
                        IEntityClass relatedEntity = relatedEntityOp.get();

                        Map<String, String> cachedList = searchTable.get(relatedId.get().toString());
                        if (cachedList == null) {
                            //search
                            Map<String, String> cached = new HashMap<>();
                            searchTable.put(relatedId.get().toString(), cached);
                            cachedList = cached;
                        }

                        //TODO how to find all
                        Map<String, String> finalCachedList = cachedList;


                        //try to find something to display
                        Optional<IEntityField> displayField = relatedEntity.fields().stream()
                                .filter(x -> Optional.ofNullable(x.config())
                                        .filter(field -> "1".equals(field.getDisplayType())).isPresent()).findFirst();

                        retStr = Stream.of(ids).map(x -> {
                            String name = finalCachedList.get(x);
                            String innerRetStr = x;
                            if (name == null) {
                                //only when displayField is present will find one for id
                                if (displayField.isPresent()) {
                                    try {
                                        //search
                                        Either<String, Map<String, Object>> one = entityService.findOne(relatedEntity, Long.parseLong(x));
                                        if (one.isRight()) {
                                            String key = displayField.get().name();
                                            Object relatedDisplayName = one.get().get(key);
                                            if (relatedDisplayName != null) {
                                                innerRetStr = relatedDisplayName.toString();
                                            }
                                        }
                                    } catch (Exception ex) {
                                        log.error("{}", ex);
                                    }
                                }
                                finalCachedList.put(x, innerRetStr);
                            } else {
                                innerRetStr = name;
                            }

                            return innerRetStr;
                        }).collect(Collectors.joining(","));
                    }
                }
                break;

            default:
                retStr = entityField.type().toTypedValue(entityField, safeSourceValue)
                        .map(IValue::getValue)
                        .map(Object::toString).orElse("");

        }

        return retStr;
    }
}
