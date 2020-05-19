package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import akka.NotUsed;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.GeneralRecord;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.command.ConditionExportCmd;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.ExportSource;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class SequenceExportSource implements ExportSource {


    private Logger logger = LoggerFactory.getLogger(ExportSource.class);

    /**
     * step
     */
    private int step = 1000;

    private int maxRetryTimes = 5;

    private EntityService entityService;

    public SequenceExportSource(EntityService entityService, int step) {
        this.entityService = entityService;
        this.step = step;
    }

    @Override
    public Source<Record, NotUsed> source(IEntityClass entityClass, ConditionQueryRequest queryRequest) {

            AtomicInteger cursor = new AtomicInteger(0);

            AtomicInteger error = new AtomicInteger(0);

            return Source.repeat(1).flatMapConcat(i -> {

                logger.info("-----Export {}:{} ---- query {} times ----", entityClass.code(), entityClass.id(), cursor.get());
                Either<String, Tuple2<Integer, List<Record>>> byCondition =
                        entityService
                                .findRecordsByCondition(entityClass, null, toQueryCondition(queryRequest, cursor.getAndIncrement(), step));
                if (byCondition.isRight()) {
                    //clear error
                    error.set(0);
                    List<Record> ret = byCondition.get()._2();
                    logger.warn("-----Export {}:{} ---- clear error and found size is {} ----", entityClass.code(), entityClass.id(), ret.size());

                    if (ret.size() < step) {
                        LinkedList<Record> list = new LinkedList<>(ret);
                        list.addLast(GeneralRecord.empty());
                        return Source.from(list);
                    } else {
                        return Source.from(byCondition.get()._2());
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

    //TODO side-effect is ok?
    private ConditionQueryRequest toQueryCondition(ConditionQueryRequest request, int pageNo, int pageSize) {
        request.setPageSize(pageSize);
        request.setPageNo(pageNo);
        return request;
    }
}
