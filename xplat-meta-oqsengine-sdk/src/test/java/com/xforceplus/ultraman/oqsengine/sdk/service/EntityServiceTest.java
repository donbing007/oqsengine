package com.xforceplus.ultraman.oqsengine.sdk.service;


import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Field;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.pojo.utils.OptionalHelper;
import com.xforceplus.ultraman.oqsengine.sdk.autoconfigurer.InitServiceAutoConfiguration;
import com.xforceplus.ultraman.oqsengine.sdk.config.AuthSearcherConfig;
import com.xforceplus.ultraman.oqsengine.sdk.service.operation.FixedDefaultSystemOperationHandler;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.xplat.galaxy.framework.configuration.AsyncTaskExecutorAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceDispatcherAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.configuration.ServiceInvokerAutoConfiguration;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.TENANTID_KEY;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.USERNAME;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {TestConfiguration.class
        , InitServiceAutoConfiguration.class
        , AuthSearcherConfig.class
        , ServiceInvokerAutoConfiguration.class
        , AsyncTaskExecutorAutoConfiguration.class
        , ServiceDispatcherAutoConfiguration.class
        , com.xforceplus.xplat.galaxy.framework.configuration.ContextConfiguration.class
        , RestTemplateAutoConfiguration.class
})

public class EntityServiceTest {

    @Autowired
    EntityService entityService;

    @Autowired
    EntityServiceEx entityServiceEx;

    @Autowired
    ContextService contextService;

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
//        Either<String, Map<String, Object>> one = entityService.findOne(baseBill.get(), 6643447720663056384L);
//        System.out.println(one);
//        assertTrue("has record", one.isRight());
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
//
//    @Test
//    public void selectOneTest() throws InterruptedException {
//        Thread.sleep(10000);
//
//        Optional<EntityClass> testBillSub = entityService.loadByCode("testbillsub");
//
//        Either<String, Map<String, Object>> one = entityService.findOne(testBillSub.get(), 6643531897886474242L);
//
//        System.out.println(one);
//    }


    @Test
    public void exshouldReturnChildId() throws InterruptedException {

        Thread.sleep(10000);

        Optional<EntityClass> testBillSub = entityService.loadByCode("testbillsub");

        Optional<EntityClass> testBill = entityService.loadByCode("testbill");

        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("bill_code", "test1");
        mapObject.put("bill_name", "billTest");
        mapObject.put("sub", "hhhh");
        mapObject.put("deci", "12.56");

        Either<String, IEntity> entity = entityServiceEx.create(testBillSub.get(), mapObject);

        Long childId = entity.get().id();
        Long parentId = entity.get().family().parent();

        System.out.println(parentId);

        Long id = (Long) entityServiceEx.findOneByParentId(testBill.get()
                , testBillSub.get(), parentId).map(x -> x.get("id"))
                .map(String::valueOf)
                .map(Long::valueOf).get();

        System.out.println(childId);

        System.out.println(id);

        System.out.println(entityService.findOne(testBillSub.get(), id));
    }

    @Test
    public void getWiredTicket() throws InterruptedException {
        Thread.sleep(10000);

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");

        System.out.println(entityService.findByCondition(ticket.get(), new RequestBuilder()
                        .field("image_id", ConditionOp.eq, 6643745129398009857L).build()));
    }

    @Test
    public void testDecimal() throws InterruptedException {
        Thread.sleep(10000);

        Optional<EntityClass> testBillSub = entityService.loadByCode("testbillsub");

        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("bill_code", "test1");
        mapObject.put("bill_name", "billTest");
        mapObject.put("sub", "hhhh");
        mapObject.put("deci", "12.56");

        Either<String, IEntity> entity = entityServiceEx.create(testBillSub.get(), mapObject);

        Long childId = entity.get().id();
        Long parentId = entity.get().family().parent();

        System.out.println("child:" + childId);
        System.out.println("parentId:" + parentId);

        Either<String, Map<String, Object>> one = entityService.findOne(testBillSub.get(), childId);

        System.out.println(one);

        //select by condition

        entityService.findByCondition(testBillSub.get(), new RequestBuilder()
                .field("deci", ConditionOp.eq, 12.56).build()).forEach(System.out::println);

        entityService.findByCondition(testBillSub.get(), new RequestBuilder()
                .field("deci", ConditionOp.ge, 0).build()).forEach(System.out::println);

        entityService.findByCondition(testBillSub.get(), new RequestBuilder()
                .field("deci", ConditionOp.le, 12.57).build()).forEach(System.out::println);

    }

    @Test
    public void testFindInIds() throws InterruptedException {

        Thread.sleep(10000);

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");

        System.out.println(entityService.findOne(ticket.get(), 6645501102127054849L));

        entityService.findByConditionWithIds(ticket.get()
                , Arrays.asList(6645501102127054849L, 6645501103938994177L, 6643337484505710593L)
                , new ConditionQueryRequest())
        .forEach(System.out::println);
    }

    @Test
    public void testCondition() throws InterruptedException {


        Thread.sleep(10000);

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");

        entityService.findByCondition(ticket.get()
//                , Arrays.asList(6645501102127054849L, 6645501103938994177L, 6643337484505710593L)
                , new RequestBuilder().field("image_id", ConditionOp.in, Arrays.asList(6645501028428939265L, 6645501028668014593L)).build())
                .forEach(System.out::println);
    }


    @Test
    public void testPagenation() throws InterruptedException{
        Thread.sleep(10000);

        Optional<EntityClass> entityOpt = entityService.loadByCode("image");

        System.out.println(entityService.findByCondition(entityOpt.get(), new RequestBuilder().pageNo(7)
                .pageSize(10).build()).get()._2().size());
    }


    private void setupContext(){

        /**
         *         fixed.put("tenant_id", () -> contextService.get(TENANTID_KEY));
         *         fixed.put("create_time", () -> LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
         *         fixed.put("create_user_name", () -> contextService.get(USERNAME));
         *         fixed.put("create_user_id", () -> contextService.get(ID));
         *         fixed.put("delete_flag", () -> "1");
         *         fixed.put("update_time", () -> LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli());
         *         fixed.put("update_user_id", () -> contextService.get(ID));
         *         fixed.put("update_user_name", () -> contextService.get(USERNAME));
         */
        contextService.set(ID, 123454L);
        contextService.set(USERNAME, "created username");
        contextService.set(TENANTID_KEY, "12312312312");
    }

    @Test
    public void testSystemProperties() throws InterruptedException {
        Thread.sleep(10000);


        setupContext();

        Optional<EntityClass> entityOpt = entityService.loadByCode("baseBill");

        Optional<EntityClass> subEntityOpt = entityService.loadByCode("salesBill");

        Map<String, Object> o = new HashMap<>();
        o.put("image_id", "1231231");
        o.put("seller_name", "hello");

        //overwrite
        o.put("create_user_id", 9000L);

        Either<String, IEntity> iEntities = entityServiceEx.create(subEntityOpt.get(), o);

        Long parent = iEntities.get().family().parent();
        Long child = iEntities.get().id();

        Either<String, Map<String, Object>> either = entityService.findOne(subEntityOpt.get(), child);

        Either<String, Map<String, Object>> findByOneChild = entityService.findOne(subEntityOpt.get(), child);

        Either<String, Map<String, Object>> findByOneParent = entityService.findOne(entityOpt.get(), parent);

        Either<String, Map<String, Object>> oneByParentId = entityServiceEx
                            .findOneByParentId(entityOpt.get(), subEntityOpt.get(), parent);


        String[] keys = new String[]{"update_time", "update_user_id", "update_user_name", "create_user_id", "tenant_id", "delete_flag", "create_user_name", "create_time"};

        System.out.println(oneByParentId.get());

        assertTrue("create_user_id is override", oneByParentId.get().get("create_user_id").equals("123454"));
        assertTrue("has all fields", Stream.of(keys).allMatch(x -> oneByParentId.get().containsKey(x)));
        assertTrue("has all fields", Stream.of(keys).allMatch(x -> findByOneChild.get().containsKey(x)));
        assertTrue("has all fields", Stream.of(keys).allMatch(x -> findByOneParent.get().containsKey(x)));

        Long id = Long.parseLong(oneByParentId.get().get("id").toString());

        //search by create_time
        Object create_time = either.get().get("create_time");
        Either<String, Tuple2<Integer, List<Map<String, Object>>>> result = entityService.findByCondition(entityOpt.get(), new RequestBuilder()
                .field("create_time", ConditionOp.eq, create_time)
                .build());


        assertTrue("can query by create_time", result.get()._1 >= 0);


        Either<String, Tuple2<Integer, List<Map<String, Object>>>> result2 = entityService.findByCondition(entityOpt.get(), new RequestBuilder()
                .field("create_user_id", ConditionOp.eq, 123454L)
                .build());


        assertTrue("can query by create_user_id", result2.get()._1 >= 0);


        entityService.deleteOne(subEntityOpt.get(), id);

    }

    private EntityClass sampleEntity(){
        /**
         * long id, String name, FieldType fieldType, FieldConfig config, String dictId, String defaultValue
         */

        FieldConfig fieldConfig = new FieldConfig();
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield", FieldType.ENUM, fieldConfig, "abc", "Happy")));

        return entityClass;
    }


    private EntityClass regexSampleEntity(){
        /**
         * long id, String name, FieldType fieldType, FieldConfig config, String dictId, String defaultValue
         */
        FieldConfig fieldConfig = new FieldConfig();
        fieldConfig.setValidateRegexString("^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$");
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }

    private EntityClass expressionSampleEntity(){
        FieldConfig fieldConfig = new FieldConfig();
        EntityClass entityClass = new EntityClass(123L, "TestDefault"
                , Arrays.asList(new Field(123L, "defaultfield"
                , FieldType.STRING, fieldConfig)));

        return entityClass;
    }

    @Test
    public void testRegx() {
        EntityClass entityClass = regexSampleEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("defaultfield", "luye66@163.com");

        Long id = entityService.create(entityClass, map).get();
        assertTrue("默认值正确", entityService.findOne(entityClass, id).get().get("defaultfield").equals("luye66@163.com"));
    }

    @Test
    public void testExpression(){

        setupContext();

        EntityClass entityClass = expressionSampleEntity();

        Map<String, Object> map = new HashMap<>();
        map.put("defaultfield", "{{id}}");

        Long id = entityService.create(entityClass, map).get();
        assertTrue("默认值正确", entityService.findOne(entityClass, id).get().get("defaultfield").equals("123454"));
    }

    @Test
    public void testDefaultValue() throws InterruptedException {

        EntityClass sampleEntity = sampleEntity();

        Map<String, Object> map = new HashMap<>();

        Long id = entityService.create(sampleEntity, map).get();

        assertTrue("默认值正确", entityService.findOne(sampleEntity, id).get().get("defaultfield").equals("Happy"));
    }


    @Test
    public void testError() throws InterruptedException {

        Thread.sleep(10000);


        Optional<EntityClass> entityOpt = entityService.loadByCode("ticketInvoice");
        String qstr = "{'tax_amount':'0.0','tenant_id':'1203260024735584256','paper_drew_date':'20200318','exception_status':'0','amount_without_tax':'0.0','batch_no':'1','create_time':'1584522310130','create_user_name':'荣颖','ticket_code':'ticketInvoice','invoice_no':'07612455','warning_status':'0','purchaser_tax_no':'91370000661397973Y','amount_with_tax':'0.0','invoice_code':'3500171130','exception_info':'','seller_name':'乐普艺术陶瓷有限公司','seller_tax_no':'91350583741673616C','purchaser_name':'山东小珠山建设发展有限公司','is_public':'0',";
        String hstr =  "'create_user':'1214481717915123712','image_id':'6645968161583661057','warning_info':'','invoice_sheet':'1','invoice_type':'s','x_point':0,'y_point':0,'width':0,'height':0,'angle':0}";
        JSONObject json1 = JSONObject.parseObject(qstr+hstr);
        Either<String, IEntity> iEntityEither = entityServiceEx.create(entityOpt.get(), json1);
        Long sId = iEntityEither.get().id();
        Long fId = iEntityEither.get().family().parent();

        Either<String, Map<String, Object>> mapEither1 = entityService.findOne(entityOpt.get(), sId);
        String id = "'id':" + sId +",";
        JSONObject json2 = JSONObject.parseObject(qstr+id+hstr);

        Either<String, Integer> integerEither = entityService.updateById(entityOpt.get(), sId, json2);

        Either<String, Map<String, Object>> mapEither2 = entityService.findOne(entityOpt.get(), sId);
        Optional<EntityClass> entityOpt1 = entityService.loadByCode("ticket");
        Either<String, Map<String, Object>> mapEither = entityService.findOne(entityOpt1.get(), fId);
    }

    @Test
    public void testBillIdSearch() throws InterruptedException {
        Thread.sleep(10000);

        Optional<EntityClass> entityOpt = entityService.loadByCode("image");
    }
}