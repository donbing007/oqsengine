package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.NameMapping;
import io.vavr.control.Either;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Entity export service
 */
public interface EntityExportService {


    CompletableFuture<Either<String, String>> export(IEntityClass entityClass, ConditionQueryRequest query, String token
            , String fileName
            , Map<String, Object> context, String exportType, String appId);
}
