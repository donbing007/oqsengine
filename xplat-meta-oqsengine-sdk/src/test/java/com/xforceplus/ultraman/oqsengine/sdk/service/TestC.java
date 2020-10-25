package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.OqsTransactional;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TestC {

    @Autowired
    private EntityService entityService;

    @Autowired
    private TestA testA;

    @Autowired
    private TestB testB;

    @OqsTransactional
    public Long testRollback(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body = new HashMap<>();
        Long aLong = entityService.create(load.get(), body).get();
        Tuple2<Long, Long> longLongTuple2 = testA.testSave();
        Long save1 = testB.save();
        Long save2 = testB.save();

        Either<String, Map<String, Object>> one = entityService.findOne(load.get(), aLong);
        System.out.println(one);

        try {
            testB.queryException();
        } catch (Exception ex){

        }


        Either<String, Map<String, Object>> one2 = entityService.findOne(load.get(), aLong);

        System.out.println(one2);

        return aLong;
    }

    @OqsTransactional
    public void test(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body = new HashMap<>();
        Long aLong = entityService.create(load.get(), body).get();
        Tuple2<Long, Long> longLongTuple2 = testA.testSave();
        Long save1 = testB.save();
        Long save2 = testB.save();

        entityService.deleteOne(load.get(), aLong);
        entityService.deleteOne(load.get(), longLongTuple2._1);
        entityService.deleteOne(load.get(), longLongTuple2._2);
        entityService.deleteOne(load.get(), save1);
        entityService.deleteOne(load.get(), save2);
    }
}
