package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops;

import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.boot.grpc.devops.DataOpsService;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.integration.mock.MockEntityClassDefine;
import com.xforceplus.ultraman.oqsengine.core.service.pojo.OqsResult;
import com.xforceplus.ultraman.oqsengine.devops.om.model.*;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.LongValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.StringValue;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.CanalContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.ManticoreContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.MysqlContainer;
import com.xforceplus.ultraman.oqsengine.testcontainer.container.impl.RedisContainer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Statement;
import java.util.*;

/**
 * CopyRight: 上海云砺信息科技有限公司
 * User: youyifan
 * DateTime: 2022/3/25 09:39
 * Description:
 * History:
 */
@ExtendWith({
        RedisContainer.class,
        MysqlContainer.class,
        ManticoreContainer.class,
        CanalContainer.class,
        SpringExtension.class
})
@ActiveProfiles("integration")
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class DataOpsServiceTest {

    final Logger logger = LoggerFactory.getLogger(DataOpsServiceTest.class);

    @Resource
    private DataOpsService dataOpsService;

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexWriteDataSourceSelector;

    @Resource
    private EntityManagementService entityManagementService;

    @Resource
    private SegmentStorage segmentStorage;

    @MockBean(name = "metaManager")
    private MetaManager metaManager;

    private SegmentInfo segmentInfo = MockEntityClassDefine.getDefaultSegmentInfo();

    private IEntity testData1;

    private IEntity testData2;

    /**
     * 每个测试的初始化.
     */
    @BeforeEach
    public void before() throws Exception {
        System.setProperty(DataSourceFactory.CONFIG_FILE, "classpath:oqsengine-ds.conf");

        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }


        for (DataSource ds : indexWriteDataSourceSelector.selects()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    stat.executeUpdate("truncate table oqsindex");
                }
            }
        }

        segmentStorage.build(segmentInfo);

        MockEntityClassDefine.initMetaManager(metaManager);

        IEntity entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 1L),
                                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "1")
                        )
                ).build();
        OqsResult<IEntity> oqsResult = entityManagementService.build(entity);
        testData1 = ResultStatus.SUCCESS.equals(oqsResult.getResultStatus()) ? oqsResult.getValue().get() : null;

        entity = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 2L),
                                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "2")
                        )
                ).build();
        oqsResult = entityManagementService.build(entity);
        testData2 = ResultStatus.SUCCESS.equals(oqsResult.getResultStatus()) ? oqsResult.getValue().get() : null;
    }

    /**
     * 每个测试的清理.
     */
    @AfterEach
    public void after() throws Exception {
        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }

        for (DataSource ds : indexWriteDataSourceSelector.selects()) {
            try (Connection conn = ds.getConnection()) {
                try (Statement stat = conn.createStatement()) {
                    stat.executeUpdate("truncate table oqsindex");
                }
            }
        }

        segmentStorage.delete(segmentInfo);
    }

    @Test
    public void tesetConditionQuery() {
        DevOpsQueryConfig devOpsQueryConfig = new DevOpsQueryConfig();
        devOpsQueryConfig.setEntityClassId(MockEntityClassDefine.L2_ENTITY_CLASS.id());
        devOpsQueryConfig.setPageNo(0L);
        devOpsQueryConfig.setPageSize(100L);

        List<DevOpsQuerySort> sorts = new ArrayList<>();
        DevOpsQuerySort sort = new DevOpsQuerySort();
        sort.setField("l0-long");
        sort.setOrder("asc");
        sorts.add(sort);
        devOpsQueryConfig.setSort(sorts);

        DevOpsQueryCondition conditions = new DevOpsQueryCondition();
        List<DevOpsQueryConditionItem> fields = new ArrayList<>();
        DevOpsQueryConditionItem field = new DevOpsQueryConditionItem();
        field.setCode("l0-long");
        field.setOperation("gt_lt");
        field.setValue(new String[]{"0", "10"});
        DevOpsQueryConditionItem field2 = new DevOpsQueryConditionItem();
        field2.setCode("l2-string");
        field2.setOperation("eq");
        field2.setValue(new String[]{"1"});

        fields.add(field);
        fields.add(field2);

        conditions.setFields(fields);
        devOpsQueryConfig.setConditions(conditions);

        DevOpsQueryResponse response = dataOpsService.conditionQuery(devOpsQueryConfig);

        Assertions.assertTrue(response != null && response.getRows().size() == 1 && response.getSummary().getTotal() == 1);
    }

    @Test
    public void testSingleCreate() {
        Map data = new HashMap();
        data.put("l0-long", 3L);
        data.put("l2-string", "3");
        DevOpsDataResponse response = dataOpsService.singleCreate(1L, MockEntityClassDefine.L2_ENTITY_CLASS.id(), data);

        Assertions.assertTrue(DevOpsDataResponse.SUCCESS_CODE.equals(response.getCode()));
    }

    @Test
    public void testSingleModify() {
        Map data = new HashMap();
        data.put("l0-long", 1L);
        data.put("l2-string", "1");
        DevOpsDataResponse response = dataOpsService.singleModify(1L, MockEntityClassDefine.L2_ENTITY_CLASS.id(), testData1.id(), data);

        Assertions.assertTrue(DevOpsDataResponse.SUCCESS_CODE.equals(response.getCode()));
    }

    @Test
    public void testSingleDelete() {
        DevOpsDataResponse response = dataOpsService.singleDelete(1L, MockEntityClassDefine.L2_ENTITY_CLASS.id(), testData1.id());

        Assertions.assertTrue(DevOpsDataResponse.SUCCESS_CODE.equals(response.getCode()));
    }

    @Test
    public void testBatchCreate() {
        Map item = new HashMap();
        item.put("l0-long", 3L);
        item.put("l2-string", "3");
        Map item2 = new HashMap();
        item2.put("l0-long", 4L);
        item2.put("l2-string", "4");
        List<Map> data = new ArrayList<>();
        data.add(item);
        data.add(item2);

        DevOpsDataResponse response = dataOpsService.batchCreate(1L, MockEntityClassDefine.L2_ENTITY_CLASS.id(), data);
        Assertions.assertTrue(DevOpsDataResponse.SUCCESS_CODE.equals(response.getCode()));
    }

    @Test
    public void testBatchModify() {

        Map item = new HashMap();
        item.put("id", String.valueOf(testData1.id()));
        item.put("l0-long", 3L);
        item.put("l2-string", "3");
        Map item2 = new HashMap();
        item2.put("id", String.valueOf(testData2.id()));
        item2.put("l0-long", 4L);
        item2.put("l2-string", "4");
        List<Map> data = new ArrayList<>();
        data.add(item);
        data.add(item2);

        DevOpsDataResponse response = dataOpsService.batchModify(1L, MockEntityClassDefine.L2_ENTITY_CLASS.id(), data);
        Assertions.assertTrue(DevOpsDataResponse.SUCCESS_CODE.equals(response.getCode()));
    }

    @Test
    public void testBatchDelete() {
        List<String> idStrs = Arrays.asList(String.valueOf(testData1.id()), String.valueOf(testData2.id()));
        DevOpsDataResponse response = dataOpsService.batchDelete(1L, MockEntityClassDefine.L2_ENTITY_CLASS.id(), idStrs);
        Assertions.assertTrue(DevOpsDataResponse.SUCCESS_CODE.equals(response.getCode()));
    }

    @Test
    public void testConvertOqsResult() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取class
        Class clazz = dataOpsService.getClass();
        // 获取方法，注意param的类型
        Method convertOqsResult = clazz.getDeclaredMethod("convertOqsResult", DataOpsService.OperateType.class, Object.class);
        convertOqsResult.setAccessible(true);

        IEntity data1 = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 3L),
                                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "3")
                        )
                ).build();

        IEntity data2 = Entity.Builder.anEntity()
                .withEntityClassRef(MockEntityClassDefine.L2_ENTITY_CLASS.ref())
                .withValues(Arrays.asList(
                                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 4L),
                                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "4")
                        )
                ).build();

        //验证新增的返回结果解析是否成功
        OqsResult<IEntity> oqsResult = OqsResult.success(data1);
        Map result = (Map) convertOqsResult.invoke(dataOpsService, DataOpsService.OperateType.SINGLE_CREATE, oqsResult);
        Assertions.assertTrue(result.containsKey("status") && result.containsKey("data"));

        //验证删除的返回结果解析是否成功
        oqsResult = OqsResult.success(data1);
        result = (Map) convertOqsResult.invoke(dataOpsService, DataOpsService.OperateType.SINGLE_DELETE, oqsResult);
        Assertions.assertTrue(result.containsKey("status") && result.containsKey("data"));

        //验证修改的返回结果解析是否成功
        Map<IEntity, IValue[]> input = new HashMap<>();
        input.put(testData1, Arrays.asList(
                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 5L),
                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "5")
        ).toArray(new IValue[]{}));
        OqsResult<Map.Entry<IEntity, IValue[]>> oqsResult2 = OqsResult.success(input.entrySet().stream().findFirst().get());
        result = (Map) convertOqsResult.invoke(dataOpsService, DataOpsService.OperateType.SINGLE_MODIFY, oqsResult2);
        Assertions.assertTrue(result.containsKey("status") && result.containsKey("data"));

        //验证批量新增的返回结果解析是否成功
        OqsResult<IEntity[]> oqsResult3 = OqsResult.success(new IEntity[]{data1, data2});
        result = (Map) convertOqsResult.invoke(dataOpsService, DataOpsService.OperateType.BATCH_CREATE, oqsResult3);
        Assertions.assertTrue(result.containsKey("status") && result.containsKey("data"));

        //验证批量删除的返回结果解析是否成功
        oqsResult3 = OqsResult.success(new IEntity[]{data1, data2});
        result = (Map) convertOqsResult.invoke(dataOpsService, DataOpsService.OperateType.BATCH_DELETE, oqsResult3);
        Assertions.assertTrue(result.containsKey("status") && result.containsKey("data"));

        //验证批量修改的返回结果解析是否成功
        input = new HashMap<>();
        input.put(data1, Arrays.asList(
                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 5L),
                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "5")
        ).toArray(new IValue[]{}));
        input.put(data2, Arrays.asList(
                new LongValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l0-long").get(), 6L),
                new StringValue(MockEntityClassDefine.L2_ENTITY_CLASS.field("l2-string").get(), "6")
        ).toArray(new IValue[]{}));
        OqsResult<Map<IEntity, IValue[]>> oqsResult4 = OqsResult.success(input);
        result = (Map) convertOqsResult.invoke(dataOpsService, DataOpsService.OperateType.BATCH_MODIFY, oqsResult4);
        Assertions.assertTrue(result.containsKey("status") && result.containsKey("data"));
    }
}
