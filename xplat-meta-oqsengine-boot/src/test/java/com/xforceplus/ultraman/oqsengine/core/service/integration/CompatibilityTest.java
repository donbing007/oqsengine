package com.xforceplus.ultraman.oqsengine.core.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.boot.OqsengineBootApplication;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import com.xforceplus.ultraman.oqsengine.common.version.OqsVersion;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.*;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.status.CommitIdStatusService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 兼容性测试,用以测试读取低版本的正确性.
 *
 * @author dongbin
 * @version 0.1 2020/11/27 11:54
 * @since 1.8
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = OqsengineBootApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CompatibilityTest extends AbstractContainerTest {

    @Resource(name = "masterDataSource")
    private DataSource masterDataSource;

    @Resource(name = "indexWriteDataSourceSelector")
    private Selector<DataSource> indexDataSource;

    @Resource
    private CommitIdStatusService commitIdStatusService;

    @Resource
    private EntitySearchService entitySearchService;


    @Resource
    private EntityManagementService entityManagementService;

    @Resource
    private ObjectMapper objectMapper;

    IEntityClass fatherClass = new EntityClass(100, "father", Arrays.asList(
        new EntityField(123, "c1", FieldType.LONG, FieldConfig.build().searchable(true)),
        new EntityField(456, "c2", FieldType.STRING, FieldConfig.build().searchable(true))
    ));
    IEntityClass childClass = new EntityClass(200, "child", null, null, fatherClass, Arrays.asList(
        new EntityField(789, "c3", FieldType.ENUM, FieldConfig.build().searchable(true)),
        new EntityField(910, "c4", FieldType.BOOLEAN, FieldConfig.build().searchable(true))
    ));

    @After
    public void after() throws Exception {
        try (Connection conn = masterDataSource.getConnection()) {
            try (Statement stat = conn.createStatement()) {
                stat.executeUpdate("truncate table oqsbigentity");
            }
        }

        for (DataSource ds : indexDataSource.selects()) {
            try (Connection indexConn = ds.getConnection()) {
                try (Statement st = indexConn.createStatement()) {
                    st.executeUpdate("truncate table oqsindex");
                }
            }
        }
    }

    /**
     * 测试读取小于当前主版本号产生的Entity.
     *
     * @throws Exception
     */
    @Test
    public void testReadMajor0Entity() throws Exception {
        Connection conn = masterDataSource.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate(
            "insert into oqsbigentity (id,entity,tx,commitid,op,version,time,pref,cref,deleted,attribute,meta,oqsmajor) " +
                "values" +
                "(1, 100, 0, 0, 1, 0, 0,0,2,0,'{\"123L\":0, \"456S\":\"v1\"}','[]',0)," +
                "(2, 200, 0, 0, 1, 0, 0,1,0,0,'{\"789S\":\"0\", \"910L\":0}','[]',0)");
        st.close();
        conn.close();
        IEntity entity = entitySearchService.selectOne(2, childClass).get();
        Assert.assertEquals(2, entity.id());
        Assert.assertEquals(0, entity.major());
        Assert.assertEquals(childClass.id(), entity.entityClass().id());

        IEntityValue values = entity.entityValue();
        Assert.assertTrue(values.getValue("c1").isPresent());
        Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

        Assert.assertTrue(values.getValue("c2").isPresent());
        Assert.assertEquals("v1", values.getValue("c2").get().valueToString());

        Assert.assertTrue(values.getValue("c3").isPresent());
        Assert.assertEquals("0", values.getValue("c3").get().valueToString());

        Assert.assertTrue(values.getValue("c4").isPresent());
        Assert.assertEquals(0, values.getValue("c4").get().valueToLong());

        // 父类.
        entity = entitySearchService.selectOne(1, fatherClass).get();
        values = entity.entityValue();
        Assert.assertEquals(1, entity.id());
        Assert.assertEquals(0, entity.major());
        Assert.assertTrue(values.getValue("c1").isPresent());
        Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

        Assert.assertTrue(values.getValue("c2").isPresent());
        Assert.assertEquals("v1", values.getValue("c2").get().valueToString());
    }

    /**
     * 测试主版本为0的数据被更新.
     *
     * @throws Exception
     */
    @Test
    public void testReplaceMajor0Entity() throws Exception {
        Connection conn = masterDataSource.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate(
            "insert into oqsbigentity (id,entity,tx,commitid,op,version,time,pref,cref,deleted,attribute,meta,oqsmajor) " +
                "values" +
                "(1, 100, 0, 0, 1, 0, 0,0,2,0,'{\"123L\":0, \"456S\":\"v1\"}','[]',0)," +
                "(2, 200, 0, 0, 1, 0, 0,1,0,0,'{\"789S\":\"0\", \"910L\":0}','[]',0)");
        st.close();
        conn.close();
        IEntity entity = entitySearchService.selectOne(2, childClass).get();
        Assert.assertEquals(ResultStatus.SUCCESS, entityManagementService.replace(entity));

        conn = masterDataSource.getConnection();
        st = conn.createStatement();
        ResultSet rs = st.executeQuery("select attribute,meta from oqsbigentity where id=2");
        rs.next();

        Map<String, Object> attribute = objectMapper.readValue(rs.getString("attribute"), Map.class);
        List<String> meta = objectMapper.readValue(rs.getString("meta"), List.class);
        rs.close();
        st.close();
        conn.close();

        Assert.assertEquals(4, attribute.size());
        Assert.assertEquals(4, meta.size());

        Assert.assertEquals(0, attribute.get("F123L"));
        Assert.assertEquals("v1", attribute.get("F456S"));
        Assert.assertEquals("0", attribute.get("F789S"));
        Assert.assertEquals(0, attribute.get("F910L"));

        Assert.assertTrue(meta.contains("123-Long"));
        Assert.assertTrue(meta.contains("456-String"));
        Assert.assertTrue(meta.contains("789-Enum"));
        Assert.assertTrue(meta.contains("910-Boolean"));


        // 保证更新不会破坏原有数据,同时版本更新为当前版本.
        IEntity newEntity = entitySearchService.selectOne(2, childClass).get();
        Assert.assertEquals(2, newEntity.id());
        Assert.assertEquals(OqsVersion.MAJOR, newEntity.major());
        Assert.assertEquals(childClass.id(), newEntity.entityClass().id());

        IEntityValue values = newEntity.entityValue();
        Assert.assertTrue(values.getValue("c1").isPresent());
        Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

        Assert.assertTrue(values.getValue("c2").isPresent());
        Assert.assertEquals("v1", values.getValue("c2").get().valueToString());

        Assert.assertTrue(values.getValue("c3").isPresent());
        Assert.assertEquals("0", values.getValue("c3").get().valueToString());

        Assert.assertTrue(values.getValue("c4").isPresent());
        Assert.assertEquals(0, values.getValue("c4").get().valueToLong());

        // 父类.
        newEntity = entitySearchService.selectOne(1, fatherClass).get();
        values = newEntity.entityValue();
        Assert.assertEquals(1, newEntity.id());
        Assert.assertEquals(OqsVersion.MAJOR, newEntity.major());
        Assert.assertTrue(values.getValue("c1").isPresent());
        Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

        Assert.assertTrue(values.getValue("c2").isPresent());
        Assert.assertEquals("v1", values.getValue("c2").get().valueToString());
    }

    /**
     * 测试主版本为0的数据被条件查询.
     *
     * @throws Exception
     */
    @Test
    public void testReadMajor0MultipleEntity() throws Exception {
        Connection conn = masterDataSource.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate(
            "insert into oqsbigentity (id,entity,tx,commitid,op,version,time,pref,cref,deleted,attribute,meta,oqsmajor) " +
                "values" +
                "(1, 100, 0, 3, 1, 0, 0,0,2,0,'{\"123L\":0, \"456S\":\"v1\"}','[]',0)," +
                "(2, 200, 0, 3, 1, 0, 0,1,0,0,'{\"789S\":\"0\", \"910L\":0}','[]',0)," +
                "(3, 100, 0, 3, 1, 0, 0,0,4,0,'{\"123L\":10, \"456S\":\"v2\"}','[]',0)," +
                "(4, 200, 0, 3, 1, 0, 0,3,0,0,'{\"789S\":\"12\", \"910L\":1}','[]',0)"
        );
        st.close();
        conn.close();
        commitIdStatusService.save(3, true);

        Collection<IEntity> entities = entitySearchService.selectMultiple(new long[]{2, 4}, childClass);
        Assert.assertEquals(2, entities.size());
        for (IEntity entity : entities) {
            if (entity.id() == 2) {

                Assert.assertEquals(0, entity.major());
                Assert.assertEquals(childClass.id(), entity.entityClass().id());

                IEntityValue values = entity.entityValue();
                Assert.assertTrue(values.getValue("c1").isPresent());
                Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

                Assert.assertTrue(values.getValue("c2").isPresent());
                Assert.assertEquals("v1", values.getValue("c2").get().valueToString());

                Assert.assertTrue(values.getValue("c3").isPresent());
                Assert.assertEquals("0", values.getValue("c3").get().valueToString());

                Assert.assertTrue(values.getValue("c4").isPresent());
                Assert.assertEquals(0, values.getValue("c4").get().valueToLong());

            } else {

                Assert.assertEquals(0, entity.major());
                Assert.assertEquals(childClass.id(), entity.entityClass().id());

                IEntityValue values = entity.entityValue();
                Assert.assertTrue(values.getValue("c1").isPresent());
                Assert.assertEquals(10, values.getValue("c1").get().valueToLong());

                Assert.assertTrue(values.getValue("c2").isPresent());
                Assert.assertEquals("v2", values.getValue("c2").get().valueToString());

                Assert.assertTrue(values.getValue("c3").isPresent());
                Assert.assertEquals("12", values.getValue("c3").get().valueToString());

                Assert.assertTrue(values.getValue("c4").isPresent());
                Assert.assertEquals(1, values.getValue("c4").get().valueToLong());
            }
        }
    }

    /**
     * 测试读取新的版本.
     *
     * @throws Exception
     */
    @Test
    public void testReadCurrentMajorEntity() throws Exception {
        Connection conn = masterDataSource.getConnection();
        Statement st = conn.createStatement();
        st.executeUpdate(
            "insert into oqsbigentity (id,entity,tx,commitid,op,version,time,pref,cref,deleted,attribute,meta,oqsmajor) " +
                "values" +
                "(1, 100, 0, 0, 1, 0, 0,0,2,0,'{\"123L\":0, \"456S\":\"v1\"}','[]'," + OqsVersion.MAJOR + ")," +
                "(2, 200, 0, 0, 1, 0, 0,1,0,0,'{\"123L\":0, \"456S\":\"v1\",\"789S\":\"0\", \"910L\":0}','[]'," + OqsVersion.MAJOR + ")");
        st.close();
        conn.close();

        IEntity entity = entitySearchService.selectOne(2, childClass).get();
        Assert.assertEquals(2, entity.id());
        Assert.assertEquals(OqsVersion.MAJOR, entity.major());
        Assert.assertEquals(childClass.id(), entity.entityClass().id());

        IEntityValue values = entity.entityValue();
        Assert.assertTrue(values.getValue("c1").isPresent());
        Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

        Assert.assertTrue(values.getValue("c2").isPresent());
        Assert.assertEquals("v1", values.getValue("c2").get().valueToString());

        Assert.assertTrue(values.getValue("c3").isPresent());
        Assert.assertEquals("0", values.getValue("c3").get().valueToString());

        Assert.assertTrue(values.getValue("c4").isPresent());
        Assert.assertEquals(0, values.getValue("c4").get().valueToLong());

        // 父类.
        entity = entitySearchService.selectOne(1, fatherClass).get();
        values = entity.entityValue();
        Assert.assertEquals(1, entity.id());
        Assert.assertEquals(OqsVersion.MAJOR, entity.major());
        Assert.assertTrue(values.getValue("c1").isPresent());
        Assert.assertEquals(0, values.getValue("c1").get().valueToLong());

        Assert.assertTrue(values.getValue("c2").isPresent());
        Assert.assertEquals("v1", values.getValue("c2").get().valueToString());
    }
}