package com.xforceplus.ultraman.oqsengine.sdk.service.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.reader.IEntityClassReader;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.ValueUp;
import com.xforceplus.ultraman.oqsengine.sdk.service.HandleResultValueService;
import com.xforceplus.ultraman.oqsengine.sdk.service.OperationType;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.RecordOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.ResultSideOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.TriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * TODO
 * default handle result value service
 */
public class DefaultHandleResultValueService implements HandleResultValueService {

    private Logger logger = LoggerFactory.getLogger(HandleResultValueService.class);

    final
    List<RecordOperationHandler> handlers;

    final
    List<ResultSideOperationHandler> resultSideOperationHandlers;

    public DefaultHandleResultValueService(List<RecordOperationHandler> handlers, List<ResultSideOperationHandler> resultSideOperationHandlers) {
        this.handlers = handlers;
        this.resultSideOperationHandlers = resultSideOperationHandlers;
    }

    @Override
    public Record toRecord(EntityClass entityClass, EntityUp up) {
        IEntityClassReader reader = new IEntityClassReader(entityClass);

        Map<String, Object> retValue = up.getValuesList().stream().collect(Collectors.toMap(ValueUp::getName, ValueUp::getValue));
        Record record = reader.toRecord(retValue);

        /**
         * addition modification
         */
        pipeConsumer(record, up);

        /**
         * field modification
         */
        record.stream().forEach(tuple -> {
            record.set(tuple._1(), pipeline(tuple._2(), tuple._1(), OperationType.RESULT));
        });

        return record;
    }

    private Object pipeline(Object value, IEntityField field, OperationType phase) {

        try {
            return resultSideOperationHandlers.stream()
                    .sorted()
                    .map(x -> (TriFunction) x)
                    .reduce(TriFunction::andThen)
                    .map(x -> x.apply(field, value, phase))
                    .orElse(value);
        } catch (Exception ex) {
            logger.error("{}", ex);
            return null;
        }
    }

    /**
     * extra operation
     * @param record
     * @param entityUp
     */
    private void pipeConsumer(Record record, EntityUp entityUp){
        handlers.stream().sorted()
                .map(x -> (BiConsumer) x)
                .reduce(BiConsumer::andThen)
                .ifPresent(x -> x.accept(record, entityUp));
    }

}
