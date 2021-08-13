package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.oqs.EntityClass;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * where 测试.
 *
 * @author dongbin
 * @version 0.1 2021/04/09 15:37
 * @since 1.8
 */
public class SphinxQLWhereTest {

    @Test
    public void testNoMatchNoEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");

        Assertions.assertEquals("(attr.123279 > 0 AND attr.78823 < 100)", where.toString());
    }

    @Test
    public void testHaveMatchNoEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");

        Assertions.assertEquals("MATCH('((@attrf 123) (@attrf 789))')", where.toString());
    }

    @Test
    public void testNoMatchHaveEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        Assertions.assertEquals("(attr.123279 > 0 AND attr.78823 < 100) AND MATCH('@entityclassf 9223372036854775807')",
            where.toString());
    }

    @Test
    public void testHaveMatchHaveEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");
        where.addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        Assertions.assertEquals("MATCH('((@attrf 123) (@attrf 789)) (@entityclassf 9223372036854775807)')",
            where.toString());
    }

    @Test
    public void testHaveMatchHaveEntityClassMultipleEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)")
            .addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build())
            .addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE - 1).build())
            .addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE - 2).build());


        Assertions.assertEquals(
            "MATCH('((@attrf 123) (@attrf 789)) (@entityclassf 9223372036854775807 | 9223372036854775806 | 9223372036854775805)')",
            where.toString());
    }

    @Test
    public void testHaveMatchHaveAttrFitlerHaveEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");
        where.addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        Assertions.assertEquals(
            "(attr.123279 > 0 AND attr.78823 < 100) AND MATCH('((@attrf 123) (@attrf 789)) (@entityclassf 9223372036854775807)')",
            where.toString());
    }

    @Test
    public void testHaveMatchHaveAttrFitlerHaveEntityClassWithCommitId() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");
        where.addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        where.setCommitId(100);

        Assertions.assertEquals(
            "commitid < 100 AND (attr.123279 > 0 AND attr.78823 < 100) AND MATCH('((@attrf 123) (@attrf 789)) (@entityclassf 9223372036854775807)')",
            where.toString());
    }

    @Test
    public void testHaveMatchHaveAttrFitlerHaveEntityClassWithCommitIdAndFilterIds() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");
        where.addEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        where.setCommitId(100);
        where.addFilterId(1).addFilterId(2).addFilterId(3);

        Assertions.assertEquals(
            "commitid < 100 AND id NOT IN (1,2,3) AND (attr.123279 > 0 AND attr.78823 < 100) AND MATCH('((@attrf 123) (@attrf 789)) (@entityclassf 9223372036854775807)')",
            where.toString());
    }

    @Test
    public void testHaveCommitId() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.setCommitId(100);

        Assertions.assertEquals("commitid < 100", where.toString());
    }

    @Test
    public void testHaveFitlerIds() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.addFilterId(1).addFilterId(2).addFilterId(3);

        Assertions.assertEquals("id NOT IN (1,2,3)", where.toString());
    }

    @Test
    public void testAddWhereAttrFilter() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.addWhere(
            new SphinxQLWhere()
                .addAttrFilter("attr.123279 > 0")
                .addAttrFilter(" AND ")
                .addAttrFilter("attr.78823 < 100"), true);

        Assertions.assertEquals("(attr.123279 > 0 AND attr.78823 < 100)", where.toString());

        where = new SphinxQLWhere();
        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.addWhere(
            new SphinxQLWhere()
                .addAttrFilter("attr.100000 = 100"),
            true
        );

        Assertions.assertEquals("((attr.123279 > 0 AND attr.78823 < 100) AND (attr.100000 = 100))", where.toString());
    }

    @Test
    public void testAddWhereMatch() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.addWhere(
            new SphinxQLWhere()
                .addMatch("(@attrf 123)")
                .addMatch(" ")
                .addMatch("(@attrf 789)"),
            true
        );

        Assertions.assertEquals("MATCH('((@attrf 123) (@attrf 789))')", where.toString());
    }
}