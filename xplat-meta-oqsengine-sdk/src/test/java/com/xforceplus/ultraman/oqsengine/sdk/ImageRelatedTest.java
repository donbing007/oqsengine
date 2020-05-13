package com.xforceplus.ultraman.oqsengine.sdk;

import com.alibaba.fastjson.JSONObject;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.DateTimeValue;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityService;
import com.xforceplus.ultraman.oqsengine.sdk.service.EntityServiceEx;
import com.xforceplus.ultraman.oqsengine.sdk.util.RequestBuilder;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionOp;
import com.xforceplus.ultraman.oqsengine.sdk.vo.dto.ConditionQueryRequest;
import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.AssertionErrors;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.LongKeys.ID;
import static com.xforceplus.xplat.galaxy.framework.context.ContextKeys.StringKeys.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * remote image test
 */
@ActiveProfiles("image")
public class ImageRelatedTest extends ContextWareBaseTest {

    @Autowired
    EntityService entityService;

    @Autowired
    EntityServiceEx entityServiceEx;

    @Autowired
    ContextService contextService;

    @Before
    public void waitForLoad(){
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFindByParentEntityTicket() throws InterruptedException {

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");
        Optional<EntityClass> ticketInvoice = entityService.loadByCode("ticketInvoice");

//        if (ticket.isPresent() && ticketInvoice.isPresent()) {
//            Either<String, Map<String, Object>> either = entityServiceEx
//                    .findOneByParentId(ticket.get(), ticketInvoice.get(), 6642957088593018881L);
//
//            either.forEach(System.out::println);
//        }

        //insert child
        Map<String, Object> body = new HashMap<>();
        IEntity resultLong = entityServiceEx.create(ticketInvoice.get(), body).get();
        Long parentId = resultLong.family().parent();
        Either<String, Map<String, Object>> oneByParentId =
                entityServiceEx.findOneByParentId(ticket.get(), ticketInvoice.get(), parentId);

        assertTrue("can find by parent ID ", oneByParentId.isRight());

        entityService.deleteOne(ticket.get(), parentId);
    }

    @Test
    public void testSelectByCode() throws InterruptedException {

        Optional<EntityClass> baseBill = entityService.loadByCode("baseBill");
        assertTrue("baseBill here", baseBill.isPresent());
    }

    @Test
    public void testConditionFind() throws InterruptedException {

        Optional<EntityClass> imageBill = entityService.loadByCode("image");
        assertTrue("image is present", imageBill.isPresent());

        //clear
        Either<String, Tuple2<Integer, List<Map<String, Object>>>> billsBefore =
                entityService.findByCondition(imageBill.get()
                        , new RequestBuilder().field("bill_image_id", ConditionOp.eq, 1111111111).build());


        billsBefore.forEach(x -> x._2().forEach(row -> {
            Long id = Long.parseLong(row.get("id").toString());
            entityService.deleteOne(imageBill.get(), id);
        }));

        Map<String, Object> body = new HashMap<>();
        body.put("bill_image_id", "1111111111");

        Long createId = entityService.create(imageBill.get(), body).get();
        Either<String, Tuple2<Integer, List<Map<String, Object>>>> bills =
                entityService.findByCondition(imageBill.get()
                        , new RequestBuilder().field("bill_image_id", ConditionOp.eq, 1111111111).build());

        assertTrue("result num is one", bills.get()._1() == 1);
        assertTrue("result list size is one", bills.get()._2().size() == 1);

        entityService.deleteOne(imageBill.get(), createId);
    }

    @Test
    public void testSaveImageWithDate() throws InterruptedException {

        Optional<EntityClass> image = entityService.loadByCode("image");
        AssertionErrors.assertTrue("image is present", image.isPresent());

        //save image
        Map<String, Object> map = new HashMap<>();
        Long timestamp = new DateTimeValue(image.get().field("rec_start_time").get(), LocalDateTime.now()).valueToLong();
        map.put("rec_start_time", timestamp);
        Long id = entityService.create(image.get(), map).get();

        Either<String, Map<String, Object>> one = entityService.findOne(image.get(), id);

        assertTrue("rec_start_time is ok ", one.get().get("rec_start_time").equals(timestamp.toString()));

        entityService.deleteOne(image.get(), id);
    }

    @Test
    public void testUpdateAndDelete() throws InterruptedException {

        Optional<EntityClass> testBill = entityService.loadByCode("testbill");
        Optional<EntityClass> testBillSub = entityService.loadByCode("testbillsub");

        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("bill_code", "test1");
        mapObject.put("bill_name", "billTest");
        mapObject.put("sub", "hhhh");


        Either<String, IEntity> entity = entityServiceEx.create(testBillSub.get(), mapObject);

        Long childId = entity.get().id();
        Long parentId = entity.get().family().parent();

        //update parent

        Map<String, Object> update = new HashMap<>();
        update.put("bill_code", "test2");

        Either<String, Integer> result = entityService.updateById(testBill.get(), parentId, update);

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> byCondition = entityService.findByCondition(testBill.get()
                , new RequestBuilder().field("bill_code", ConditionOp.eq, "test2").build());


        assertTrue("update is ok", byCondition.get()._2().get(0).get("bill_code").equals("test2"));


        Either<String, Integer> delResult = entityService.deleteOne(testBillSub.get(), childId);

        assertTrue("del is ok", delResult.isRight());
    }

    @Test
    public void exShouldReturnChildId() throws InterruptedException {

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

        assertTrue("parentId is valid", parentId > 0);
        assertTrue("childId is valid", childId > 0);

        /**
         * this id is child id
         */
        Long id = (Long) entityServiceEx.findOneByParentId(testBill.get()
                , testBillSub.get(), parentId).map(x -> x.get("id"))
                .map(String::valueOf)
                .map(Long::valueOf).get();


        assertTrue("child id field is ok", id != null);

        assertTrue("child id is valid", entityService.findOne(testBillSub.get(), id).isRight());

        assertTrue("clear is ok", entityService.deleteOne(testBillSub.get(), id).isRight());
    }

    @Test
    public void testDecimal() throws InterruptedException {

        Optional<EntityClass> testBillSub = entityService.loadByCode("testbillsub");

        Map<String, Object> mapObject = new HashMap<>();
        mapObject.put("bill_code", "test1");
        mapObject.put("bill_name", "billTest");
        mapObject.put("sub", "hhhh");
        mapObject.put("deci", "12.56");

        Either<String, IEntity> entity = entityServiceEx.create(testBillSub.get(), mapObject);

        Long childId = entity.get().id();
        Long parentId = entity.get().family().parent();

        assertTrue("parentId is valid", parentId > 0);
        assertTrue("childId is valid", childId > 0);

        Either<String, Map<String, Object>> one = entityService.findOne(testBillSub.get(), childId);

        assertTrue("insert is ok", one.isRight());

        //select by condition
        assertTrue("select by deci eq is ok", entityService.findByCondition(testBillSub.get()
                , new RequestBuilder().field("deci", ConditionOp.eq, 12.56).build()).get()._1() > 0);

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> deci = entityService.findByCondition(testBillSub.get()
                , new RequestBuilder().field("deci", ConditionOp.ge, 0).build());

        deci.get();

        assertTrue("select by deci ge is ok", deci.get()._1() > 0);

        assertTrue("select by deci le is ok", entityService.findByCondition(testBillSub.get()
                , new RequestBuilder().field("deci", ConditionOp.le, 12.57).build()).get()._1() > 0);

        assertTrue("clear is ok", entityService.deleteOne(testBillSub.get(), childId).isRight());

    }

    @Test
    public void testFindInIds() throws InterruptedException {

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("file_url", "a");

        Long id1 = entityService.create(ticket.get(), objectMap).get();

        Map<String, Object> objectMap2 = new HashMap<>();
        objectMap2.put("file_url", "b");


        Long id2 = entityService.create(ticket.get(), objectMap2).get();

        Map<String, Object> objectMap3 = new HashMap<>();
        objectMap3.put("file_url", "c");

        Long id3 = entityService.create(ticket.get(), objectMap3).get();

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> byConditionWithIds =
                entityService.findByConditionWithIds(ticket.get()
                        , Arrays.asList(id1, id2, id3)
                        , new ConditionQueryRequest());

        System.out.println(byConditionWithIds);

        assertEquals("records is equals 3", 3, (int) byConditionWithIds.get()._1());

        //clear
        entityService.deleteOne(ticket.get(), id1);
        entityService.deleteOne(ticket.get(), id2);
        entityService.deleteOne(ticket.get(), id3);
    }

    @Test
    public void testInCondition() throws InterruptedException {

        Optional<EntityClass> ticket = entityService.loadByCode("ticket");

        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("image_id", "100001");

        Long id1 = entityService.create(ticket.get(), objectMap).get();

        Map<String, Object> objectMap2 = new HashMap<>();
        objectMap2.put("image_id", "100002");


        Long id2 = entityService.create(ticket.get(), objectMap2).get();

        Map<String, Object> objectMap3 = new HashMap<>();
        objectMap3.put("image_id", "100003");

        Long id3 = entityService.create(ticket.get(), objectMap3).get();

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> image_id = entityService.findByCondition(ticket.get()
                , new RequestBuilder().field("image_id", ConditionOp.in, Arrays.asList("100001", "100002", "100003")).build());

        assertEquals("query size is 3", 3, image_id.get()._1.intValue());

        //clear
        entityService.deleteOne(ticket.get(), id1);
        entityService.deleteOne(ticket.get(), id2);
        entityService.deleteOne(ticket.get(), id3);
    }


    private void setupContext() {
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
        contextService.set(USERNAME, "created");
        contextService.set(USER_DISPLAYNAME, "created");
        contextService.set(TENANTID_KEY, "12312312312");
    }

    @Test
    public void testSystemProperties() throws InterruptedException {

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

        assertTrue("create_user_id is override", oneByParentId.get().get("create_user_id").equals("9000"));
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

    @Test
    public void testParentUpdateAndSearch() throws InterruptedException {

        Optional<EntityClass> entityOpt = entityService.loadByCode("ticketInvoice");
        String qstr = "{'tax_amount':'0.0','tenant_id':'1203260024735584256','paper_drew_date':'20200318','exception_status':'0','amount_without_tax':'0.0','batch_no':'1','create_time':'1584522310130','create_user_name':'荣颖','ticket_code':'ticketInvoice','invoice_no':'07612455','warning_status':'0','purchaser_tax_no':'91370000661397973Y','amount_with_tax':'0.0','invoice_code':'3500171130','exception_info':'','seller_name':'乐普艺术陶瓷有限公司','seller_tax_no':'91350583741673616C','purchaser_name':'山东小珠山建设发展有限公司','is_public':'0',";
        String hstr = "'create_user_id':'1214481717915123712','image_id':'6645968161583661057','warning_info':'','invoice_sheet':'1','invoice_type':'s','x_point':0,'y_point':0,'width':0,'height':0,'angle':0}";
        JSONObject json1 = JSONObject.parseObject(qstr + hstr);
        Either<String, IEntity> iEntityEither = entityServiceEx.create(entityOpt.get(), json1);

        Long sId = iEntityEither.get().id();
        Long fId = iEntityEither.get().family().parent();

        Either<String, Map<String, Object>> mapEither1 = entityService.findOne(entityOpt.get(), sId);
        String id = "'id':" + sId + ",";
        JSONObject json2 = JSONObject.parseObject(qstr + id + hstr);

        Either<String, Integer> integerEither = entityService.updateById(entityOpt.get(), sId, json2);

        Either<String, Map<String, Object>> mapEither2 = entityService.findOne(entityOpt.get(), sId);

        assertTrue("child search is ok", mapEither2.isRight());

        Optional<EntityClass> entityOpt1 = entityService.loadByCode("ticket");
        Either<String, Map<String, Object>> mapEither = entityService.findOne(entityOpt1.get(), fId);

        assertTrue("parent search is ok", mapEither2.isRight());

        entityService.deleteOne(entityOpt.get(), sId);
    }

    @Test
    public void testEq() throws InterruptedException {

        Optional<EntityClass> entityOpt = entityService.loadByCode("salesBill");
        Optional<EntityClass> parentOp = entityService.loadByCode("baseBill");

        EntityClass salesBill = entityOpt.get();
        EntityClass baseBill = parentOp.get();

        //clean old
        entityService.findByCondition(salesBill
                , new RequestBuilder()
                        .field("bill_data_status", ConditionOp.eq, "2000")
                        .build()).map(x -> {
            return x._2();
        }).forEach(x -> x.stream().forEach(l ->
                entityService.deleteOne(salesBill, Long.parseLong(l.get("id").toString()))));

        entityService.findByCondition(baseBill
                , new RequestBuilder()
                        .field("bill_data_status", ConditionOp.eq, "2000")
                        .build()).map(x -> {
            return x._2();
        }).forEach(x -> x.stream().forEach(l ->
                entityService.deleteOne(baseBill, Long.parseLong(l.get("id").toString()))));


        entityService.findByCondition(salesBill
                , new RequestBuilder()
                        .field("bill_data_status", ConditionOp.eq, "3000")
                        .build()).map(x -> {
            return x._2();
        }).forEach(x -> x.stream().forEach(l ->
                entityService.deleteOne(salesBill, Long.parseLong(l.get("id").toString()))));


        //insert one
        Map<String, Object> map = new HashMap<>();
        map.put("bill_data_status", "2000");
        map.put("seller_tax_no", "212324");

        Either<String, IEntity> ret = entityServiceEx.create(salesBill, map);
        Long parentId = ret.get().family().parent();
        Long selfId = ret.get().id();


        //insert two
        Map<String, Object> mapD = new HashMap<>();

        mapD.put("bill_data_status", "2000");
        mapD.put("seller_tax_no", "212325");

        Either<String, IEntity> ret2 = entityServiceEx.create(salesBill, mapD);
        Long parentId2 = ret2.get().family().parent();
        Long selfId2 = ret2.get().id();

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> billSearchRet =
                entityService.findByCondition(baseBill
                        , new RequestBuilder()
                                .field("bill_data_status", ConditionOp.eq, "2000")
                                .build());

        assertEquals("search by parent size is 2", 2, (int) billSearchRet.get()._1());

        //update by sub
        Map<String, Object> map3 = new HashMap<>();
        map3.put("bill_data_status", "3000");

        entityService.updateById(salesBill, selfId, map3);

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> billSearchRet2 =
                entityService.findByCondition(baseBill
                        , new RequestBuilder()
                                .field("bill_data_status", ConditionOp.eq, "3000")
                                .build());

        assertEquals("search by parent size is 1", 1, (int) billSearchRet2.get()._1());


        //update by parent
        Map<String, Object> map4 = new HashMap<>();
        map4.put("bill_data_status", "2000");
        entityService.updateById(baseBill, parentId, map4);

        Either<String, Tuple2<Integer, List<Map<String, Object>>>> billSearchRet3 =
                entityService.findByCondition(salesBill
                        , new RequestBuilder()
                                .field("bill_data_status", ConditionOp.eq, "2000")
                                .build());
        assertEquals("search by parent size is 2", 2, (int) billSearchRet3.get()._1());


        //clear
        entityService.deleteOne(salesBill, selfId2);
        entityService.deleteOne(salesBill, selfId);
    }

    @Test
    public void testSystemOverride() throws InterruptedException {

        setupContext();
        Optional<EntityClass> entityOpt = entityService.loadByCode("baseBill");

        Map<String, Object> ss = new HashMap<>();
        ss.put("create_user_id", "1111111");
        Long x = entityService.create(entityOpt.get(), ss).get();

        assertTrue(entityService.findOne(entityOpt.get(), x).isRight());

        //search by create_user_name
        assertTrue(entityService.findByCondition(entityOpt.get()
                , new RequestBuilder()
                        .field("create_user_name", ConditionOp.eq, "created")
                        .build()).get()._1() > 0);

        entityService.deleteOne(entityOpt.get(), x);
    }

    @Test
    public void testLabelQuery() throws InterruptedException {

        setupContext();

        Optional<EntityClass> entityOpt = entityService.loadByCode("label");

        Map<String, Object> ss = new HashMap<>();
        ss.put("create_user_id", "1111111");

        Long record1 = entityService.create(entityOpt.get(), ss).get();

        ss.put("create_user_id", "1111112");

        Long record2 = entityService.create(entityOpt.get(), ss).get();

        assertTrue(entityService.findOne(entityOpt.get(), record1).isRight());

        //search by create_user_name
        assertTrue(entityService.findByCondition(entityOpt.get()
                , new RequestBuilder()
                        .field("create_user_name", ConditionOp.eq, "created")
                        .build()).get()._1() > 1);

        //clear
        entityService.deleteOne(entityOpt.get(), record1);
        entityService.deleteOne(entityOpt.get(), record2);
    }

    @Test
    public void testRecordError() throws InterruptedException {

        setupContext();
        Optional<EntityClass> entityOpt = entityService.loadByCode("ticketAttachment");

        Map<String, Object> map = new HashMap<>();
        Long id = entityService.create(entityOpt.get(), map).get();

        Map<String, Object> ret = entityService.findOne(entityOpt.get(), id).get();
        Map<String, Object> ret2 = entityService.findByConditionWithIds(entityOpt.get(), Arrays.asList(id), new ConditionQueryRequest()).get()._2().get(0);

        assertEquals(ret, ret2);

        entityService.deleteOne(entityOpt.get(), id);
    }

    @Test
    public void findAllEntities() throws InterruptedException {

        entityService.getEntityClasss().forEach(System.out::println);

    }

    @Test
    public void searchLabel() throws InterruptedException{

        Optional<EntityClass> label = entityService.loadByCode("ticketAttachment");

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("label.ids", "1234");

        Long id = entityService.create(label.get(), inputMap).get();

        System.out.println(id);

        System.out.println(entityService.findOne(label.get(), id));
    }
}
