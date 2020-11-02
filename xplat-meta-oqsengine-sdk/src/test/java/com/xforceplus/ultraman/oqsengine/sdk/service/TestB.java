package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.OqsTransactional;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.Propagation;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TestB {

    @Autowired
    private EntityService entityService;


    @OqsTransactional
    public Object query(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        return entityService.findByCondition(load.get(), new RequestBuilder().pageNo(1).pageSize(1000).build());
    }

    @OqsTransactional
    public Object queryException(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        entityService.findByCondition(load.get(), new RequestBuilder().pageNo(1).pageSize(1000).build());
        throw new RuntimeException("New Exception");
    }

    @OqsTransactional(propagation = Propagation.REQUIRES_NEW)
    public Object queryExceptionNew(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        entityService.findByCondition(load.get(), new RequestBuilder().pageNo(1).pageSize(1000).build());
        throw new RuntimeException("New Exception");
    }

    @OqsTransactional(propagation = Propagation.NOT_SUPPORTED)
    public Object queryNotSupportEx(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        entityService.findByCondition(load.get(), new RequestBuilder().pageNo(1).pageSize(1000).build());
        throw new RuntimeException("New Exception");
    }

    @OqsTransactional
    public Long save(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body = new HashMap<>();
        Long aLong = entityService.create(load.get(), body).get();
        return aLong;
    }

}

