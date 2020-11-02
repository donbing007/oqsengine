package com.xforceplus.ultraman.oqsengine.sdk;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.TestA;
import com.xforceplus.ultraman.oqsengine.sdk.service.TestB;
import com.xforceplus.ultraman.oqsengine.sdk.service.TestC;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.OqsTransaction;
import com.xforceplus.ultraman.oqsengine.sdk.transactional.annotation.OqsTransactional;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@EnableAspectJAutoProxy
public class TransactionTest extends ContextWareBaseTest {

    @Autowired
    private TestA testA;

    @Autowired
    private TestB testB;

    @Autowired
    private TestC testC;

    @Autowired
    private EntityService entityService;

    @Test
    public void testRequire() {
        Object query = testA.query();
        System.out.println(query);
    }


    @Test
    public void testRequireTwice() {
        Object query = testA.query2();
        System.out.println(query);
    }

    @Test
    public void testRequireEx() {
        try {
            Object query = testA.query3();
            System.out.println(query);
        } catch (Exception ex) {

        }
    }

    @Test
    public void testExCatch() {
        Object query = testA.query4();
        System.out.println(query);
    }

    @Test
    public void testRequiredNew() {
        try {
            Object query = testA.query3New();
            System.out.println(query);
        } catch (Exception ex) {

        }
    }

    @Test
    public void testRequiredNewCatchEx() {
        Object query = testA.query3NewCatch();
        System.out.println(query);
    }

    @Test
    public void testNotSupportCatch() {
        Object query = testA.queryNotSupportCatch();
        System.out.println(query);
    }

    @Test
    public void testNotSupport() {
        Object query = testA.queryNotSupport();
    }

    @Test
    public void testCreateRollBack() {
        Object o = testA.testCreate();
    }

    @Test
    public void testQuery() {
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Either<String, Map<String, Object>> one = entityService.findOne(load.get(), 1319351951225257984L);
        System.out.println(one);
    }

    @Test
    public void testRollBack(){
        Long aLong = testA.testManualRollback();
        Optional<IEntityClass> load = entityService.load("1275678539314814978");
        Either<String, Map<String, Object>> one = entityService.findOne(load.get(), aLong);
        assertTrue(one.isLeft());
    }

    @Test
    public void testMulti(){
        testC.test();
    }

    @Test
    public void testMultiRollback(){
        Long aLong = testC.testRollback();
        Either<String, Map<String, Object>> one = entityService.findOne(entityService.load("1275678539314814978").get(), aLong);
        assertTrue(one.isLeft());
    }

    @Test
    public void testTimeout() {
        testA.testSaveWithTimeout();
    }
}
