package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.parse;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.AggregationType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldConfig;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.FieldType;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Relationship;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.Aggregation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 聚合树解析器.
 *
 * @author: wangzheng.
 * @date: 2021/9/8 17:33
 */
public class AggregationParseTest {

    @Resource
    AggregationParse aggregationParse;

    private List<IEntityClass> entityClasses = new ArrayList<>();

    /**
     * 初始化.
     */
    @BeforeEach
    public void before() {
        // a <- b <- c
        //        <- c1
        // d <- e
        // a [id,longf,bigf,timef,relb.id]
        // b [id,julongf,jubigf,jutimef,relc.id,relc1.id ]
        // c [id,jujulongf]
        // c1[id,jujubigf]
        // d [id,longf,bigf,timef,rele.id]
        // e [id,jutimef]
        IEntityClass a = EntityClass.Builder.anEntityClass()
            .withId(1)
            .withCode("a")
            .withFields(Arrays.asList(
                EntityField.Builder.anEntityField()
                    .withId(11)
                    .withName("longf")
                    .withFieldType(FieldType.LONG)
                    .withConfig(FieldConfig.Builder.anFieldConfig().build())
                    .build(),
                EntityField.Builder.anEntityField()
                    .withId(12)
                    .withName("bigf")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(FieldConfig.Builder.anFieldConfig().build())
                    .build(),
                EntityField.Builder.anEntityField()
                    .withId(13)
                    .withName("timef")
                    .withFieldType(FieldType.DATETIME)
                    .withConfig(FieldConfig.Builder.anFieldConfig().build())
                    .build()))
            .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                .withId(100)
                .withCode("relb")
                .withLeftEntityClassId(1)
                .withLeftEntityClassCode("a")
                .withRightEntityClassId(2)
                .withBelongToOwner(false)
                .withIdentity(false)
                .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                .build()))
            .build();
        IEntityClass b = EntityClass.Builder.anEntityClass()
            .withId(2)
            .withCode("b")
            .withFields(Arrays.asList(
                EntityField.Builder.anEntityField()
                    .withId(21)
                    .withName("julongf")
                    .withFieldType(FieldType.LONG)
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(1)
                            .withFieldId(11)
                            .withRelationId(200)
                            .withAggregationType(AggregationType.SUM)
                            .build())
                        .build())
                    .build(),
                EntityField.Builder.anEntityField()
                    .withId(22)
                    .withName("jubigf")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(1)
                            .withFieldId(12)
                            .withRelationId(200)
                            .withAggregationType(AggregationType.MAX)
                            .build())
                        .build())
                    .build(),
                EntityField.Builder.anEntityField()
                    .withId(23)
                    .withName("jutimef")
                    .withFieldType(FieldType.DATETIME)
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(1)
                            .withFieldId(13)
                            .withRelationId(200)
                            .withAggregationType(AggregationType.MAX)
                            .build())
                        .build())
                    .build()))
            .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                .withId(200)
                .withCode("relb")
                .withBelongToOwner(true)
                .withIdentity(false)
                .withLeftEntityClassId(2)
                .withLeftEntityClassCode("b")
                .withRightEntityClassId(1)
                .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                .build()))
            .build();
        IEntityClass c = EntityClass.Builder.anEntityClass()
            .withId(3)
            .withCode("c")
            .withFields(Arrays.asList(
                EntityField.Builder.anEntityField()
                    .withId(31)
                    .withName("jujulongf")
                    .withFieldType(FieldType.LONG)
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(2)
                            .withFieldId(21)
                            .withAggregationType(AggregationType.COUNT)
                            .withRelationId(300)
                            .build())
                        .build())
                    .build()))
            .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                .withId(300)
                .withCode("relb")
                .withBelongToOwner(true)
                .withIdentity(false)
                .withLeftEntityClassId(3)
                .withLeftEntityClassCode("c")
                .withRightEntityClassId(2)
                .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                .build()))
            .build();
        IEntityClass c1 = EntityClass.Builder.anEntityClass()
            .withId(4)
            .withCode("c1")
            .withFields(Arrays.asList(
                EntityField.Builder.anEntityField()
                    .withId(41)
                    .withName("jujulongf")
                    .withFieldType(FieldType.LONG)
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(2)
                            .withFieldId(21)
                            .withAggregationType(AggregationType.SUM)
                            .withRelationId(400)
                            .build())
                        .build())
                    .build()))
            .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                .withId(400)
                .withCode("relb")
                .withBelongToOwner(true)
                .withIdentity(false)
                .withLeftEntityClassId(3)
                .withLeftEntityClassCode("c")
                .withRightEntityClassId(2)
                .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                .build()))
            .build();
        IEntityClass d = EntityClass.Builder.anEntityClass()
            .withId(5)
            .withCode("d")
            .withFields(Arrays.asList(
                EntityField.Builder.anEntityField()
                    .withId(51)
                    .withName("longf")
                    .withFieldType(FieldType.LONG)
                    .withConfig(FieldConfig.Builder.anFieldConfig().build())
                    .build(),
                EntityField.Builder.anEntityField()
                    .withId(52)
                    .withName("bigf")
                    .withFieldType(FieldType.DECIMAL)
                    .withConfig(FieldConfig.Builder.anFieldConfig().build())
                    .build(),
                EntityField.Builder.anEntityField()
                    .withId(53)
                    .withName("timef")
                    .withFieldType(FieldType.DATETIME)
                    .withConfig(FieldConfig.Builder.anFieldConfig().build())
                    .build()))
            .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                .withId(500)
                .withCode("rele")
                .withBelongToOwner(false)
                .withIdentity(false)
                .withLeftEntityClassId(5)
                .withLeftEntityClassCode("d")
                .withRightEntityClassId(6)
                .withRelationType(Relationship.RelationType.MANY_TO_ONE)
                .build()))
            .build();
        IEntityClass e = EntityClass.Builder.anEntityClass()
            .withId(6)
            .withCode("e")
            .withFields(Arrays.asList(
                EntityField.Builder.anEntityField()
                    .withId(61)
                    .withName("jujulongf")
                    .withFieldType(FieldType.LONG)
                    .withConfig(FieldConfig.Builder.anFieldConfig()
                        .withCalculation(Aggregation.Builder.anAggregation()
                            .withClassId(5)
                            .withFieldId(51)
                            .withAggregationType(AggregationType.SUM)
                            .withRelationId(600)
                            .build())
                        .build())
                    .build()))
            .withRelations(Arrays.asList(Relationship.Builder.anRelationship()
                .withId(600)
                .withCode("reld")
                .withBelongToOwner(true)
                .withIdentity(false)
                .withLeftEntityClassId(6)
                .withLeftEntityClassCode("e")
                .withRightEntityClassId(5)
                .withRelationType(Relationship.RelationType.ONE_TO_MANY)
                .build()))
            .build();
        entityClasses.add(a);
        entityClasses.add(b);
        entityClasses.add(c);
        entityClasses.add(c1);
        entityClasses.add(d);
        entityClasses.add(e);
    }

    /**
     * 查找聚合树.
     */
    @Test
    public void find() {
        aggregationParse = new MetaAggregationParse();
        aggregationParse.builderTrees("7", 100001, entityClasses);
        ParseTree parseTree = aggregationParse.find(2L, 21L, "");
        Assertions.assertNotNull(parseTree);
    }

    /**
     * 查找这个对象下有多少个需要更新的聚合树.
     */
    @Test
    public void findTrees() {
        aggregationParse = new MetaAggregationParse();
        aggregationParse.builderTrees("7", 100001, entityClasses);
        //List<ParseTree> parseTrees = aggregationParse.find(2l,"");
        //Assertions.assertNotNull(parseTrees);
        //Assertions.assertEquals(parseTrees.size(), 3);
    }

    /**
     * 追加最新聚合树.
     */
    @Test
    public void appendTree() {

    }

    /**
     * 构建解析器.
     */
    @Test
    public void builder() {
        aggregationParse = new MetaAggregationParse();
        aggregationParse.builderTrees("7", 100001, entityClasses);
        Assertions.assertNotNull(aggregationParse);
    }

}
