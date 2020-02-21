package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Condition;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ResponseMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class EntityController {

    @Autowired
    private EntityService entityService;

    @GetMapping("/api/{tenantId}/{appCode}/bos/{boId}entities/{id}")
    public Response<Map<String, String>> singleQuery(
            @PathVariable String tenantId,
            @PathVariable String appCode,
            @PathVariable String boId,
            @PathVariable String id){

        //find bo
        Optional<EntityClass> entityClassOp = entityService.load(tenantId, appCode, boId);

        Response rep = new Response();
        Map<String, String> result = null;

        if(entityClassOp.isPresent()) {
            Condition condition = new Condition();
            condition.eq("id", id);
            result = entityService.findOne(entityClassOp.get(), condition);
        }

        if(result != null){
            rep.setCode("1");
            rep.setMessage("查询成功");
            rep.setResult(result);
        }else{
            rep.setCode("-1");
            rep.setMessage("查询记录不存在");
        }

        return rep;
    }
}
