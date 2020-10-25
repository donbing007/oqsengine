package com.xforceplus.ultraman.oqsengine.sdk.service;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.OqsTransaction;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.TransactionManager;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.OqsTransactional;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Service
public class TestA{

    @Autowired
    private EntityService entityService;

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private TestB testB;

    @OqsTransactional
    public Object query(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        return entityService.findByCondition(load.get(), new RequestBuilder().pageNo(1).pageSize(1000).build());
    }

    @OqsTransactional
    public Object query2(){
        return testB.query();
    }

    @OqsTransactional
    public Object query3(){
        return testB.queryException();
    }

    @OqsTransactional
    public Object queryCatch(){
        try {
            return testB.queryException();
        }catch (Exception ex){
            return null;
        }
    }

    @OqsTransactional
    public Object query3New(){
        return testB.queryExceptionNew();
    }

    @OqsTransactional
    public Object query3NewCatch(){
        try {
            return testB.queryExceptionNew();
        } catch (Exception ex){

        }
        return null;
    }


    @OqsTransactional
    public Object queryNotSupportCatch(){
        try {
            return testB.queryNotSupportEx();
        } catch (Exception ex){

        }
        return null;
    }

    @OqsTransactional
    public Object queryNotSupport(){
        OqsTransaction current = transactionManager.getCurrent();
        try {
            testB.queryNotSupportEx();
        } catch(Exception ex){

        }

        assertTrue(!current.isRollBack());
        return null;
    }

    @OqsTransactional
    public Object query4(){
        try {
            return testB.queryException();
        }catch (Exception ex){

        }
        return null;
    }

    @OqsTransactional
    public Object testCreate(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body  = new HashMap<>();
        Either<String, Long> longs = entityService.create(load.get(), body);
        Long id = longs.getOrElseThrow(x -> new RuntimeException(x));
        try {
            testB.queryException();
        } catch (Exception ex){

        }

        OqsTransaction oqs = transactionManager.getCurrent();
        assertTrue(oqs.isRollBack());
        return id;
    }

    @OqsTransactional
    public Long testManualRollback(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body = new HashMap<>();
        Long aLong = entityService.create(load.get(), body).get();
        System.out.println(aLong);
        OqsTransaction current = transactionManager.getCurrent();
        current.setRollBack(true);
        return  aLong;
    }

    @OqsTransactional
    public Tuple2<Long, Long> testSave(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body = new HashMap<>();
        Long aLong = entityService.create(load.get(), body).get();
        Long save = testB.save();
        return Tuple.of(aLong, save);
    }

    @OqsTransactional(timeout = 10000)
    public Tuple2<Long, Long> testSaveWithTimeout(){
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Map<String, Object> body = new HashMap<>();
        Long aLong = entityService.create(load.get(), body).get();
        Long save = testB.save();
        return Tuple.of(aLong, save);
    }
}
