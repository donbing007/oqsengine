package com.xforceplus.ultraman.oqsengine.sdk.service.export.impl;

import akka.NotUsed;
import akka.stream.javadsl.Flow;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.reader.record.Record;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.ExportRecordStringFlow;
import com.xforceplus.ultraman.oqsengine.sdk.service.export.ExportStringTransformer;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.NameMapping;
import io.vavr.Tuple2;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * CSV like string transformer
 */
public class CSVRecordFlow implements ExportRecordStringFlow {

    @Override
    public Flow<Record, String, NotUsed> getFlow(IEntityClass entityClass, AtomicBoolean isFirstLine, List<NameMapping> nameMapping
            , List<String> columns, Set<String> filterColumns, ExportStringTransformer transformer, Map<String, Object> context) {

        return Flow.of(Record.class).map(record -> {
            StringBuilder sb = new StringBuilder();
            if (isFirstLine.get()) {

                //nameMapping
                //caution!! nameMapping is maybe
                //using merge func to always return first elem
                Map<String, String> map =
                        Optional.ofNullable(nameMapping).map(x -> x.stream()
                                .collect(Collectors.toMap(NameMapping::getCode, NameMapping::getText, (a, b) -> a)))
                                .orElse(Collections.emptyMap());

                String header = record
                        .stream(filterColumns)
                        .sorted(Comparator.comparingInt(field -> columns.indexOf(field._1().name())))
                        .map(Tuple2::_1)
                        .map(x -> Optional.ofNullable(x)
                                .map(y -> {
                                    String fieldName = y.name();
                                    String name = map.get(fieldName);
                                    if (name == null) {
                                        return y.cnName();
                                    } else {
                                        return name;
                                    }
                                })
                                .orElse(""))
                        .collect(Collectors.joining(","));

                //setup headers
                sb.append(header);
                sb.append("\n");
                isFirstLine.set(false);
            }

            //may have null conver to ""
            String line = record
                    .stream(filterColumns)
                    .sorted(Comparator.comparingInt(field -> columns.indexOf(field._1().name())))
                    .map(x -> {

                        IEntityField field = x._1();
                        Object value = x._2();

                        //TODO take field type in consideration?
                        //convert enum to dict value
                        // \t is a tricky for csv see
                        //     https://qastack.cn/superuser/318420/formatting-a-comma-delimited-csv-to-force-excel-to-interpret-value-as-a-string
                        // using escape instead
                        return transformer.toString(entityClass, field, value, context);
                    })
                    .collect(Collectors.joining(","));
            sb.append(line);
            sb.append("\n");
            return sb.toString();
        });
    }
}
