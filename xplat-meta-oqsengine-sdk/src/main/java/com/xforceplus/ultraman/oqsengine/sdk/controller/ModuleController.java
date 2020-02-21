package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ModuleController {


    @Autowired
    private MetadataRepository metadataRepository;

    //TODO tenantId and appCode not used currently
    @GetMapping("/api/{tenantId}/{appCode}/bos/{id}/entityClass")
    public Response<BoItem> getBoDetails(@PathVariable String tenantId
                 , @PathVariable String appCode
                 , @PathVariable String id){

        Response<BoItem> response = new Response<>();
        BoItem boItem = metadataRepository.getBoDetailById(id);

        if(boItem != null) {
            //TODO ? any failure condition
            response.setCode("1");
            response.setMessage("获取成功");
            response.setResult(boItem);

        }else{
            response.setCode("-1");
            response.setMessage("不存在该对象");
        }
        return response;
    }
}
