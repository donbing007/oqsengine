package com.xforceplus.ultraman.oqsengine.sdk.controller;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.store.repository.MetadataRepository;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.BoItem;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * module controller
 *
 * @author admin
 */
@RequestMapping
public class ModuleController {

    @Autowired
    private MetadataRepository metadataRepository;

    @Autowired
    private AuthSearcherConfig config;

    //TODO tenantId and appCode not used currently
    @GetMapping("/bos/{id}/entityClass")
    @ResponseBody
    public Response<BoItem> getBoDetails(@PathVariable String id) {

        Response<BoItem> response = new Response<>();
        BoItem boItem = metadataRepository.getBoDetailById(id);

        if (boItem != null) {
            //TODO ? any failure condition
            response.setCode("1");
            response.setMessage("获取成功");
            response.setResult(boItem);

        } else {
            response.setCode("-1");
            response.setMessage("不存在该对象");
        }
        return response;
    }


    //TODO tenantId and appCode not used currently
    @GetMapping("/bos/entityClasses")
    @ResponseBody
    public Response<List<String>> getAllBoDetails() {

        Response<List<String>> response = new Response<>();
        List<IEntityClass> allEntities = metadataRepository.findAllEntities();

        //TODO ? any failure condition
        response.setCode("1");
        response.setMessage("获取成功");
        response.setResult(allEntities.stream().map(IEntityClass::toString).collect(Collectors.toList()));
        return response;
    }


    @GetMapping("/bos/entityClass/{id}")
    @ResponseBody
    public Response<String> getEntityClassById(@PathVariable Long id) {


        Optional<IEntityClass> load = metadataRepository.load(config.getTenant(), config.getAppId(), id.toString());

        return load.map(entity -> {
            Response<String> response = new Response<>();
            response.setCode("1");
            response.setMessage("获取成功");
            response.setResult(entity.toString());
            return response;
        }).orElseThrow(() -> new RuntimeException("NOT found"));
    }

    @GetMapping("/bos/entityClass/code/{code}")
    @ResponseBody
    public Response<String> getEntityClassByCode(@PathVariable String code) {


        Optional<IEntityClass> load = metadataRepository.loadByCode(config.getTenant(), config.getAppId(), code);

        return load.map(entity -> {
            Response<String> response = new Response<>();
            response.setCode("1");
            response.setMessage("获取成功");
            response.setResult(entity.toString());
            return response;
        }).orElseThrow(() -> new RuntimeException("NOT found"));
    }
}
