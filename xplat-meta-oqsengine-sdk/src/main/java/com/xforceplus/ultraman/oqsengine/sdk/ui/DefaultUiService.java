package com.xforceplus.ultraman.oqsengine.sdk.ui;

import com.xforceplus.ultraman.oqsengine.sdk.command.*;
import io.vavr.Tuple2;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * default ui service
 */
public interface DefaultUiService {

    Either<String, Map<String, Object>> singleQuery(SingleQueryCmd cmd);

    Either<String, Integer> singleDelete(SingleDeleteCmd cmd);

    Either<String, Long> singleCreate(SingleCreateCmd cmd);

    Either<String, Integer> singleUpdate(SingleUpdateCmd cmd);

    Either<String, Tuple2<Integer, List<Map<String, Object>>>> conditionSearch(ConditionSearchCmd cmd);

    CompletableFuture<Either<String, String>> conditionExport(ConditionExportCmd cmd);
}
