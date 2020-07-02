package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.GeneralRecord;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSource;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Conditions;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.FieldCondition;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * export source
 */
public class SequenceExportSource implements ExportSource {


    private Logger logger = LoggerFactory.getLogger(ExportSource.class);

    /**
     * step
     */
    private int step = 1000;

    private int maxRetryTimes = 5;

    private EntityService entityService;

    private ContextService contextService;

    public SequenceExportSource(EntityService entityService, int step, ContextService contextService) {
        this.entityService = entityService;
        this.step = step;
        this.contextService = contextService;
    }

    @Override
    public Source<Record, NotUsed> source(IEntityClass entityClass, ConditionQueryRequest queryRequest) {

        AtomicInteger cursor = new AtomicInteger(0);

        AtomicInteger error = new AtomicInteger(0);

        //last record id for search
        AtomicLong lastId = new AtomicLong(0);

        Map<String, Object> contextMap = null;
        if (contextService != null) {
            contextMap = contextService.getAll();
        }

        Map<String, Object> finalContextMap = contextMap;

        return Source.repeat(1).flatMapConcat(i -> {

            if (contextService != null && finalContextMap != null) {
                contextService.fromMap(finalContextMap);
            }

            logger.info("-----Export {}:{} ---- query {} times ----", entityClass.code(), entityClass.id(), cursor.get());
            Either<String, Tuple2<Integer, List<Record>>> byCondition =
                    entityService
                            .findRecordsByCondition(entityClass, null, toQueryCondition(queryRequest, cursor.getAndIncrement(), step, lastId.get()));

            if (contextService != null) {
                contextService.clear();
            }

            if (byCondition.isRight()) {
                //clear error
                error.set(0);
                List<Record> ret = byCondition.get()._2();
                logger.warn("-----Export {}:{} ---- clear error and found size is {} ----", entityClass.code(), entityClass.id(), ret.size());

                if (ret.size() < step) {
                    //end here
                    LinkedList<Record> list = new LinkedList<>(ret);
                    list.addLast(GeneralRecord.empty());
                    return Source.from(list);
                } else {
                    //go on
                    Record lastRecord = ret.get(ret.size() - 1);
                    if (lastRecord != null) {
                        lastId.set(lastRecord.getId());
                    }
                    return Source.from(ret);
                }
            } else {
                logger.warn("-----Export {}:{} ---- found error {} ----", entityClass.code(), entityClass.id(), byCondition.getLeft());
                if (error.getAndIncrement() < maxRetryTimes) {
                    return Source.empty();
                } else {
                    return Source.single(GeneralRecord.empty());
                }
            }


        }).takeWhile(Record::nonEmpty);
    }

    //TODO
    //TODO side-effect is ok?
    private ConditionQueryRequest toQueryCondition(ConditionQueryRequest request, int pageNo, int pageSize, Long lastId) {
        request.setPageSize(pageSize);
        request.setPageNo(pageNo);

        Conditions conditions = request.getConditions();

        List<FieldCondition> newFieldConditions;
        if (conditions == null || conditions.getFields() == null) {
            newFieldConditions = new LinkedList<>();
        } else {
            newFieldConditions = new LinkedList<>(conditions.getFields());
        }

        FieldCondition condition = new FieldCondition();
        condition.setCode("id");
        condition.setOperation(ConditionOp.gt);
        condition.setValue(Collections.singletonList(lastId.toString()));
        newFieldConditions.add(condition);

        if (conditions == null) {
            conditions = new Conditions();
        }

        conditions.setFields(newFieldConditions);
        request.setConditions(conditions);

        return request;
    }
}
