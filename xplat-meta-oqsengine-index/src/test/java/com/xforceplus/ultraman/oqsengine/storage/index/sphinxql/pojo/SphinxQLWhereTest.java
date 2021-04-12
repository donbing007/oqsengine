package com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.EntityClass;
import org.junit.Assert;
import org.junit.Test;

/**
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

        Assert.assertEquals("(attr.123279 > 0 AND attr.78823 < 100)", where.toString());
    }

    @Test
    public void testHaveMatchNoEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");

        Assert.assertEquals("MATCH('((@attrf 123) (@attrf 789))')", where.toString());
    }

    @Test
    public void testNoMatchHaveEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.setEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        Assert.assertEquals("(attr.123279 > 0 AND attr.78823 < 100) AND MATCH('@entityclassf =9223372036854775807')",
            where.toString());
    }

    @Test
    public void testHaveMatchHaveEntityClass() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();

        where.addMatch("(@attrf 123)")
            .addMatch(" ")
            .addMatch("(@attrf 789)");
        where.setEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        Assert.assertEquals("MATCH('((@attrf 123) (@attrf 789)) (@entityclassf =9223372036854775807)')", where.toString());
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
        where.setEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        Assert.assertEquals("(attr.123279 > 0 AND attr.78823 < 100) AND MATCH('((@attrf 123) (@attrf 789)) (@entityclassf =9223372036854775807)')",
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
        where.setEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        where.setCommitId(100);

        Assert.assertEquals("commitid < 100 AND (attr.123279 > 0 AND attr.78823 < 100) AND MATCH('((@attrf 123) (@attrf 789)) (@entityclassf =9223372036854775807)')",
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
        where.setEntityClass(EntityClass.Builder.anEntityClass().withId(Long.MAX_VALUE).build());

        where.setCommitId(100);
        where.addFilterId(1).addFilterId(2).addFilterId(3);

        Assert.assertEquals("commitid < 100 AND id NOT IN (1,2,3) AND (attr.123279 > 0 AND attr.78823 < 100) AND MATCH('((@attrf 123) (@attrf 789)) (@entityclassf =9223372036854775807)')",
            where.toString());
    }

    @Test
    public void testHaveCommitId() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.setCommitId(100);

        Assert.assertEquals("commitid < 100", where.toString());
    }

    @Test
    public void testHaveFitlerIds() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.addFilterId(1).addFilterId(2).addFilterId(3);

        Assert.assertEquals("id NOT IN (1,2,3)", where.toString());
    }

    @Test
    public void testAddWhereAttrFilter() throws Exception {
        SphinxQLWhere where = new SphinxQLWhere();
        where.addWhere(
            new SphinxQLWhere()
                .addAttrFilter("attr.123279 > 0")
                .addAttrFilter(" AND ")
                .addAttrFilter("attr.78823 < 100"), true);

        Assert.assertEquals("(attr.123279 > 0 AND attr.78823 < 100)", where.toString());

        where = new SphinxQLWhere();
        where.addAttrFilter("attr.123279 > 0")
            .addAttrFilter(" AND ")
            .addAttrFilter("attr.78823 < 100");
        where.addWhere(
            new SphinxQLWhere()
                .addAttrFilter("attr.100000 = 100")
            , true
        );

        Assert.assertEquals("((attr.123279 > 0 AND attr.78823 < 100) AND (attr.100000 = 100))", where.toString());
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

        Assert.assertEquals("MATCH('((@attrf 123) (@attrf 789))')", where.toString());
    }
}