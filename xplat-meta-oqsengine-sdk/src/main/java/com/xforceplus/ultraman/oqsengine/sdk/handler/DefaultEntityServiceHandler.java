package com.xforceplus.ultraman.oqsengine.sdk.handler;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.sdk.command.*;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.EntityExportService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.ui.DefaultUiService;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * default ui service handler
 */
public class DefaultEntityServiceHandler implements DefaultUiService {

    @Autowired
    private EntityService entityService;

    @Autowired
    private EntityExportService exportService;

    private static final String MISSING_ENTITIES = "查询对象不存在";

    private Logger log = LoggerFactory.getLogger(DefaultUiService.class);

    private Optional<IEntityClass> getEntityClass(MetaDataLikeCmd cmd) {
        return
                Optional
                        .ofNullable(cmd.version()).map(x -> {
                    return entityService.load(cmd.getBoId(), cmd.version());
                }).orElseGet(() -> entityService.load(cmd.getBoId()));
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Map<String, Object>> singleQuery(SingleQueryCmd cmd) {

        Optional<IEntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.findOne(entityClassOp.get(), Long.parseLong(cmd.getId()));
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Integer> singleDelete(SingleDeleteCmd cmd) {
        Optional<IEntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.deleteOne(entityClassOp.get(), Long.valueOf(cmd.getId()));
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Long> singleCreate(SingleCreateCmd cmd) {

        Optional<IEntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.create(entityClassOp.get(), cmd.getBody());
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Integer> singleUpdate(SingleUpdateCmd cmd) {

        Optional<IEntityClass> entityClassOp = getEntityClass(cmd);

        if (entityClassOp.isPresent()) {
            return entityService.updateById(entityClassOp.get(), cmd.getId(), cmd.getBody());
        } else {
            return Either.left(MISSING_ENTITIES);
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, Tuple2<Integer, List<Map<String, Object>>>> conditionSearch(ConditionSearchCmd cmd) {

        Optional<IEntityClass> entityClassOp = getEntityClass(cmd);

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

        //#26 user-center not support Chinese
        //remove
        Long currentTime = System.nanoTime();

        String token = Optional.ofNullable(metaData.get("code")).map(Object::toString)
                .orElse(cmd.getBoId()).trim() + "-" + currentTime;

        String fileName = Optional.ofNullable(metaData.get("name")).map(Object::toString).map(String::trim)
                .orElse(Optional.ofNullable(metaData.get("code")).map(Object::toString)
                        .orElse(cmd.getBoId())).trim() + "-" + currentTime;

        Optional<? extends IEntityClass> iEntityClassOp;

        if (cmd.getBoId() != null) {
            iEntityClassOp = entityService.load(cmd.getBoId());
        } else {
            iEntityClassOp = entityService.load(cmd.getBoId(), cmd.version());
        }

        if (!iEntityClassOp.isPresent()) {
            return CompletableFuture.completedFuture(Either.left(MISSING_ENTITIES));
        } else {
            //TODO
            return exportService.export(iEntityClassOp.get(), cmd.getConditionQueryRequest()
                    , token, fileName, new HashMap<>(metaData), cmd.getExportType(), cmd.getAppId());
        }
    }

    @QueryHandler(isDefault = true)
    @Override
    public Either<String, InputStream> importTemplate(GetImportTemplateCmd cmd) {

        String boId = cmd.getBoId();

        Optional<IEntityClass> entityClass = entityService.load(boId);

        if (entityClass.isPresent()) {
            Collection<IEntityField> fields = entityClass.get().fields();

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

        Optional<IEntityClass> entityClassOp = entityService.load(boId);

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
}
