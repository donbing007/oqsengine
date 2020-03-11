package com.xforceplus.ultraman.oqsengine.sdk.service;


import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.xplat.galaxy.framework.configuration.AsyncTaskExecutorAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceDispatcherAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceInvokerAutoConfiguration;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {TestConfiguration.class
        , InitServiceAutoConfiguration.class
        , AuthSearcherConfig.class
        , ServiceInvokerAutoConfiguration.class
        , AsyncTaskExecutorAutoConfiguration.class
        , ServiceDispatcherAutoConfiguration.class
})

public class EntityServiceTest {

    @Autowired
    EntityService entityService;

    @Autowired
    EntityServiceEx entityServiceEx;

    @Test
    public void testSelectFromParent() throws InterruptedException {

        Thread.sleep(10000);

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");
        Optional<EntityClass> ticketInvoice = entityService.loadByCode("ticketInvoice");

        if(ticket.isPresent() && ticketInvoice.isPresent()){
            Either<String, Map<String, Object>> either = entityServiceEx
                    .findOneByParentId(ticket.get(), ticketInvoice.get(), 6642957088593018881L);

            either.forEach(System.out::println);
        }
    }

    @Test
    public void testSelectByCode() throws InterruptedException{
        Thread.sleep(10000);
        Optional<EntityClass> baseBill = entityService.loadByCode("baseBill");
        assertTrue("baseBill here", baseBill.isPresent());
    }

    @Test
    public void testImageFindOne() throws InterruptedException{
        Thread.sleep(10000);
        Optional<EntityClass> baseBill = entityService.loadByCode("image");
        assertTrue("image is present", baseBill.isPresent());
        Either<String, Map<String, Object>> one = entityService.findOne(baseBill.get(), 6643447720663056384L);
        System.out.println(one);
        assertTrue("has record", one.isRight());
    }

    @Test
    public void testConditionFindOne() throws InterruptedException{
        Thread.sleep(10000);
        Optional<EntityClass> imageBill = entityService.loadByCode("image");
        assertTrue("image is present", imageBill.isPresent());
        Either<String, Tuple2<Integer, List<Map<String, Object>>>> bills = entityService.findByCondition(imageBill.get(), new RequestBuilder()
                .field("bill_id", ConditionOp.eq, 0).build());

        bills.forEach(x -> System.out.println(x));
    }

    @Test
    public void testSaveImageWithDate() throws InterruptedException {
        Thread.sleep(10000);

        Optional<EntityClass> imageBill = entityService.loadByCode("image");
        assertTrue("image is present", imageBill.isPresent());

        //save image
        Map<String, Object> map = new HashMap<>();
        map.put("rec_start_time", new DateTimeValue(imageBill.get().field("rec_start_time").get(),LocalDateTime.now()).valueToLong());
        System.out.println(entityService.create(imageBill.get(), map).get());

//        Either<String, Tuple2<Integer, List<Map<String, Object>>>> bills = entityService.findByCondition(imageBill.get(), new RequestBuilder()
//                .field("bill_id", ConditionOp.eq, 0).build());
//
//        bills.forEach(x -> System.out.println(x));
    }

    @Test
    public void testUpdateAndDelete() throws InterruptedException {
        Thread.sleep(10000);

        Optional<EntityClass> testBill = entityService.loadByCode("testbill");
        Optional<EntityClass> testBillSub = entityService.loadByCode("testbillsub");

        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("bill_code", "test1");
        mapObject.put("bill_name", "billTest");
        mapObject.put("sub", "hhhh");


        Either<String, IEntity> entity = entityServiceEx.create(testBillSub.get(), mapObject);

        Long childId = entity.get().id();
        Long parentId = entity.get().family().parent();

        System.out.println("child:" + childId);
        System.out.println("parentId:" + parentId);

        //update parent

        Map<String, Object> update = new HashMap<>();
        update.put("bill_code", "test2");


        Either<String, Integer> result = entityService.updateById(testBill.get(), parentId, update);

        System.out.println("result:" + result.get());

        //delete sub

        Either<String, Integer> delResult  = entityService.deleteOne(testBillSub.get(), childId);


        System.out.println("del result:" + delResult.get());
    }

}
