package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.RowItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.SummaryItem;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class EntityController {

    @Autowired
    private EntityService entityService;

    @GetMapping("/api/{tenantId}/{appCode}/bos/{boId}/entities/{id}")
    public Response<Map<String, String>> singleQuery(
            @PathVariable String tenantId,
            @PathVariable String appCode,
            @PathVariable String boId,
            @PathVariable String id){

        //find bo
        Optional<EntityClass> entityClassOp = entityService.load(tenantId, appCode, boId);


        if(entityClassOp.isPresent()) {
            Either<String, Map<String, String>> either =
                    entityService.findOne(entityClassOp.get(), Long.valueOf(id));
            Response rep = new Response();
            rep.setCode("1");
            if(either.isRight()){
                rep.setMessage("获取成功");
                rep.setResult(either.get());
            }else{
                rep.setCode("-1");
                rep.setMessage(either.getLeft());

            }
            return rep;
        }

        return Response.Error("查询记录不存在");
    }

    @DeleteMapping("/api/{tenantId}/{appCode}/bos/{boId}/entities/{id}")
    public Response<String> singleDelete(
        @PathVariable String tenantId,
        @PathVariable String appCode,
        @PathVariable String boId,
        @PathVariable String id
    ){
        Optional<EntityClass> entityClassOp = entityService.load(tenantId, appCode, boId);

        Response rep = new Response();

        if(entityClassOp.isPresent()) {
            Either<String, Integer> result = entityService.deleteOne(entityClassOp.get(), Long.valueOf(id));
            if(result.isRight()){
                rep.setCode("1");
                rep.setResult(String.valueOf(result.get()));
                rep.setMessage("操作成功");
                return rep;
            }
        }

        //TODO
        //entity missing
        rep.setCode("-1");
        rep.setMessage("操作失败");
        return rep;
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
    @PostMapping("/api/{tenantId}/{appCode}/bos/{boId}/entities")
    public Response<String> singleCreate( @PathVariable String tenantId,
                                          @PathVariable String appCode,
                                          @PathVariable String boId,
                                          @RequestBody Map<String, Object> body
    ){
        Optional<EntityClass> entityClassOp = entityService.load(tenantId, appCode, boId);

        Response rep = new Response();

        if(entityClassOp.isPresent()) {
             Either<String, Long> result = entityService.create(entityClassOp.get(), body);


            if(result.isRight()){
                rep.setCode("1");
                rep.setResult(String.valueOf(result.get()));
                rep.setMessage("操作成功");
                return rep;
            }else{
                rep.setCode("-1");
                rep.setResult(result.getLeft());
                rep.setMessage("操作失败");
                return rep;
            }
        }

        //TODO
        //entity missing
        rep.setCode("-1");
        rep.setMessage("操作失败");
        return rep;
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

    @PostMapping("/api/{tenantId}/{appCode}/bos/{boid}/entities/query")
    public Response<RowItem<Map<String, String>>> conditionQuery(@PathVariable String tenantId,
                                                                 @PathVariable String appCode,
                                                                 @PathVariable String boId,
                                                                 @RequestBody ConditionQueryRequest condition){

        Optional<EntityClass> entityClassOp = entityService.load(tenantId, appCode, boId);

        if(entityClassOp.isPresent()) {

            Either<String, Tuple2<Integer, List<Map<String, String>>>> result = entityService.findByCondition(entityClassOp.get(), condition);
            return extractRepList(result);
        }

        return Response.Error("对象不存在");
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
