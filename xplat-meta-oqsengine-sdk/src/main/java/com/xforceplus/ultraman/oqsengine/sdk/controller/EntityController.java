package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.command.*;
import com.xforceplus.ultraman.oqsengine.sdk.ui.DefaultUiService;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.RowItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.SummaryItem;
import com.xforceplus.xplat.galaxy.framework.dispatcher.ServiceDispatcher;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ResolvableType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author admin
 */
@RequestMapping
public class EntityController {

    //TODO
    @Autowired
    private ServiceDispatcher dispatcher;

    private static String FAILED = "操作失败:";

    @Value("${xplat.oqsengine.sdk.export.maxsize:50000}")
    private int exportMaxSize;

    @GetMapping("/bos/{boId}/entities/{id}")
    @ResponseBody
    public ResponseEntity<Response<Map<String, Object>>> singleQuery(
            @PathVariable String boId,
            @PathVariable String id,
            @RequestParam(required = false, value = "v") String version
    ) {

        Either<String, Map<String, Object>> result = dispatcher.querySync(new SingleQueryCmd(boId, id, version)
                , DefaultUiService.class, "singleQuery");

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<Map<String, Object>> rep = new Response<>();
            rep.setCode("1");
            rep.setMessage("获取成功");
            rep.setResult(x);
            return ResponseEntity.ok(rep);
        }).getOrElseGet(str -> {
            Response<Map<String, Object>> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage(FAILED.concat(str));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rep);
        });
    }

    @DeleteMapping("/bos/{boId}/entities/{id}")
    @ResponseBody
    public ResponseEntity<Response<String>> singleDelete(
            @PathVariable String boId,
            @PathVariable String id,
            @RequestParam(required = false, value = "v") String version
    ) {

        Either<String, Integer> result = dispatcher.querySync(new SingleDeleteCmd(boId, id, version)
                , ResolvableType.forClassWithGenerics(Either.class, String.class, Integer.class));

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return ResponseEntity.ok(rep);
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage(FAILED.concat(str));
            rep.setResult(str);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rep);
        });
    }

    /**
     * 新增
     * request: {
     * url: '/api/{tenantId}/{appCode}/bos/{boid}/entities',
     * method: 'post'
     * body: {
     * key: value
     * }
     * response: {code:string, message:string}
     * }
     */
    @PostMapping("/bos/{boId}/entities")
    @ResponseBody
    public ResponseEntity<Response<String>> singleCreate(@PathVariable String boId,
                                                         @RequestParam(required = false, value = "v") String version,
                                                         @RequestBody Map<String, Object> body
    ) {

        Either<String, Long> result = dispatcher
                .querySync(new SingleCreateCmd(boId, body, version)
                        , DefaultUiService.class, "singleCreate");

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return ResponseEntity.ok(rep);
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage(FAILED.concat(str));
            rep.setResult(str);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rep);
        });
    }

//    request: {
//        url: '/api/{tenantId}/{appCode}/bos/{boid}/entities/{id}',
//                method: 'put'
//        body: {
//            key: value
//        }
//    }
//    response: {code:string, message:string}

    @PutMapping("/bos/{boId}/entities/{id}")
    @ResponseBody
    public ResponseEntity<Response<String>> singleModify(@PathVariable String boId,
                                                         @PathVariable Long id,
                                                         @RequestParam(required = false, value = "v") String version,
                                                         @RequestBody Map<String, Object> body
    ) {

        Either<String, Integer> result = dispatcher.querySync(new SingleUpdateCmd(boId, id, body, version)
                , ResolvableType.forClassWithGenerics(Either.class, String.class, Integer.class));

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return ResponseEntity.ok(rep);
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage(FAILED.concat(str));
            rep.setResult(str);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rep);
        });
    }

    /**
     * 条件查询
     * request: {
     * url: '/api/{tenantId}/{appCode}/bos/{boid}/entities',
     * method: 'post',
     * body: {
     * pageNo: number,
     * pageSize: number,
     * conditions: {
     * fields: [
     * {
     * code: string,
     * operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
     * value: Array
     * }
     * ],
     * entities: [
     * {
     * code: 'otherEntity',
     * fields: [
     * {
     * code: string,
     * operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
     * value: Array
     * }
     * ],
     * }
     * ]
     * },
     * sort: [
     * {
     * field: string,
     * order: enum{asc, desc}
     * }
     * ],
     * entity: {
     * fields: ['id', 'name', 'field1', 'field2', 'field3'],
     * entities: [
     * {
     * code: 'otherEntity1',
     * fields: ['name'],
     * },
     * {
     * code: 'otherEntity2',
     * fields: ['name'],
     * },
     * ],
     * },
     * }
     * }
     * response: {
     * code: string,
     * message: string,
     * result: {
     * rows: [
     * {key(${EntityCode.FieldCode}): value}
     * ],
     * summary: {
     * total: 100,
     * },
     * }
     * }
     */

    @PostMapping("/bos/{boId}/entities/query")
    @ResponseBody
    public ResponseEntity<Response<RowItem<Map<String, Object>>>> conditionQuery(
            @PathVariable String boId,
            @RequestParam(required = false, value = "v") String version,
            @RequestBody ConditionQueryRequest condition) {

        //default
        if (condition != null){
            if (condition.getPageNo() == null){
                condition.setPageNo(1);
            }

            if (condition.getPageSize() == null){
                condition.setPageSize(10);
            }
        }

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> result =
                dispatcher.querySync(new ConditionSearchCmd(boId, condition, version)
                        , DefaultUiService.class, "conditionSearch");
        return extractRepList(Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")));
    }

    private <T> ResponseEntity<Response<RowItem<T>>> extractRepList(Either<String, Tuple2<Integer, List<T>>> result) {
        Response rep = new Response();
        if (result.isRight()) {
            rep.setCode("1");
            Tuple2<Integer, List<T>> tuple = result.get();
            RowItem<T> rowItem = new RowItem<>();
            rowItem.setSummary(new SummaryItem(tuple._1()));
            rowItem.setRows(tuple._2());
            rep.setResult(rowItem);
            rep.setMessage("操作成功");
            return ResponseEntity.ok(rep);
        } else {
            rep.setCode("-1");
            rep.setMessage(FAILED.concat(result.getLeft()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rep);
        }
    }

    /**
     * TODO
     *
     * @param boId
     * @param version
     * @param condition
     * @return
     */
    @PostMapping("/bos/{boId}/entities/export")
    @ResponseBody
    public CompletableFuture<Response<String>> conditionExport(
            @PathVariable String boId,
            @RequestParam(required = false, value = "v") String version,
            @RequestParam(required = true, defaultValue = "sync", value = "exportType") String exportType,
            @RequestBody ConditionQueryRequest condition) {

        //default
        if (condition != null){
            if (condition.getPageNo() == null){
                condition.setPageNo(1);
            }

            if (condition.getPageSize() == null || condition.getPageSize() > exportMaxSize){
                condition.setPageSize(exportMaxSize);
            }
        }

        CompletableFuture<Either<String, String>> exportResult = dispatcher.querySync(new ConditionExportCmd(boId, condition, version, exportType)
                , DefaultUiService.class, "conditionExport");

        return exportResult.thenApply(x -> {
            if (x.isRight()) {
                Response<String> response = new Response<>();
                response.setResult(x.get());
                response.setMessage("OK");
                response.setCode("1");
                return response;
            } else {
                return Response.Error(x.getLeft());
            }
        });
    }


    /**
     * download template
     *
     * @param boId
     * @return
     */
    @GetMapping("/bos/{boId}/entities/import/template")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> importTemplate(@PathVariable String boId
            , @RequestParam(required = false, value = "v") String version) {


        Either<String, InputStream> importTemplate = dispatcher.querySync(new GetImportTemplateCmd(boId, version)
                , DefaultUiService.class, "importTemplate");

        if (importTemplate.isRight()) {

            InputStream finalInput = importTemplate.get();
            StreamingResponseBody responseBody = outputStream -> {
                StreamUtils.copy(finalInput, outputStream);
                outputStream.close();
            };

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + boId + "-template.csv")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(responseBody);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PostMapping("/bos/{boId}/entities/import")
    @ResponseBody
    public ResponseEntity<Response<String>> importEntities(@PathVariable String boId
            , @RequestParam(required = false, value = "v") String version, MultipartFile file) {


        Either<String, String> result = dispatcher.querySync(new ImportCmd(boId, version, file)
                , DefaultUiService.class, "batchImport");


        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return ResponseEntity.ok(rep);
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage(FAILED.concat(str));
            rep.setResult(str);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(rep);
        });
    }
}
