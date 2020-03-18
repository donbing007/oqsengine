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
import org.springframework.core.ResolvableType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.AbstractController;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@RequestMapping
public class EntityController {

    //TODO
    @Autowired
    private ServiceDispatcher dispatcher;

    @GetMapping("/bos/{boId}/entities/{id}")
    @ResponseBody
    public Response<Map<String, Object>> singleQuery(
            @PathVariable String boId,
            @PathVariable String id){

        Either<String, Map<String, Object>> result = dispatcher.querySync(new SingleQueryCmd(boId, id)
                , DefaultUiService.class,"singleQuery");

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<Map<String, Object>> rep = new Response<>();
            rep.setCode("1");
            rep.setMessage("获取成功");
            rep.setResult(x);
            return rep;
        }).getOrElseGet(str -> {
            Response<Map<String, Object>> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage(str);
            return rep;
        });
    }

    @DeleteMapping("/bos/{boId}/entities/{id}")
    @ResponseBody
    public Response<String> singleDelete(
        @PathVariable String boId,
        @PathVariable String id
    ){

        Either<String, Integer> result = dispatcher.querySync(new SingleDeleteCmd(boId, id)
                , ResolvableType.forClassWithGenerics(Either.class, String.class, Integer.class));

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return rep;
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage("操作失败");
            rep.setResult(str);
            return rep;
        });
    }

    /**
     * 新增
     * request: {
     *     url: '/api/{tenantId}/{appCode}/bos/{boid}/entities',
     *     method: 'post'
     *     body: {
     *         key: value
     *     }
     *     response: {code:string, message:string}
     * }
     *
     */
    @PostMapping("/bos/{boId}/entities")
    @ResponseBody
    public Response<String> singleCreate( @PathVariable String boId,
                                          @RequestBody Map<String, Object> body
    ){

        Either<String, Long> result = dispatcher
                .querySync(new SingleCreateCmd(boId, body)
                , DefaultUiService.class, "singleCreate");

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return rep;
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage("操作失败");
            rep.setResult(str);
            return rep;
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
    public Response<String> singleModify( @PathVariable String boId,
                                          @PathVariable Long id,
                                          @RequestBody Map<String, Object> body
    ){

        Either<String, Integer> result = dispatcher.querySync(new SingleUpdateCmd(boId, id, body)
                , ResolvableType.forClassWithGenerics(Either.class, String.class, Integer.class));

        return Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")).map(x -> {
            Response<String> rep = new Response<>();
            rep.setCode("1");
            rep.setResult(String.valueOf(x));
            rep.setMessage("操作成功");
            return rep;
        }).getOrElseGet(str -> {
            Response<String> rep = new Response<>();
            rep.setCode("-1");
            rep.setMessage("操作失败");
            rep.setResult(str);
            return rep;
        });
    }

    /**
     * 条件查询
     * request: {
     *     url: '/api/{tenantId}/{appCode}/bos/{boid}/entities',
     *     method: 'post',
     *     body: {
     *         pageNo: number,
     *         pageSize: number,
     *         conditions: {
     *             fields: [
     *                 {
     *                     code: string,
     *                     operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
     *                     value: Array
     *                 }
     *             ],
     *             entities: [
     *                 {
     *                     code: 'otherEntity',
     *                     fields: [
     *                         {
     *                             code: string,
     *                             operation: enum{equal, like, in, gteq&lteq, gteq&lt, gt&lteq, gt&lt, gt, gteq, lt, lteq},
     *                             value: Array
     *                         }
     *                     ],
     *                 }
     *             ]
     *         },
     *         sort: [
     *             {
     *                 field: string,
     *                 order: enum{asc, desc}
     *             }
     *         ],
     *         entity: {
     *             fields: ['id', 'name', 'field1', 'field2', 'field3'],
     *             entities: [
     *                 {
     *                     code: 'otherEntity1',
     *                     fields: ['name'],
     *                 },
     *                 {
     *                     code: 'otherEntity2',
     *                     fields: ['name'],
     *                 },
     *             ],
     *         },
     *     }
     * }
     * response: {
     *     code: string,
     *     message: string,
     *     result: {
     *         rows: [
     *             {key(${EntityCode.FieldCode}): value}
     *         ],
     *         summary: {
     *             total: 100,
     *         },
     *     }
     * }
     */

    @PostMapping("/bos/{boId}/entities/query")
    @ResponseBody
    public Response<RowItem<Map<String, Object>>> conditionQuery(@PathVariable String boId,
                                                                 @RequestBody ConditionQueryRequest condition){

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> result =
                dispatcher.querySync(new ConditionSearchCmd(boId, condition)
                        , DefaultUiService.class, "conditionSearch");
        return  extractRepList(Optional.ofNullable(result).orElseGet(() -> Either.left("没有返回值")));
    }

    private <T> Response<RowItem<T>> extractRepList(Either<String, Tuple2<Integer, List<T>>> result){
        Response rep = new Response();
        if(result.isRight()){
            rep.setCode("1");
            Tuple2<Integer, List<T>> tuple = result.get();
            RowItem<T> rowItem = new RowItem<>();
            rowItem.setSummary(new SummaryItem(tuple._1()));
            rowItem.setRows(tuple._2());
            rep.setResult(rowItem);
            rep.setMessage("操作成功");
            return rep;
        }else{
            rep.setCode("-1");
            rep.setMessage(result.getLeft());
            return rep;
        }
    }
}
