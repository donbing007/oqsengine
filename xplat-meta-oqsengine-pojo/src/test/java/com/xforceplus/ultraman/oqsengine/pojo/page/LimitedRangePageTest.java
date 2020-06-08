package com.xforceplus.ultraman.oqsengine.pojo.page;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 测试限制行号的分页对象.
 * @author dongbin
 * @version 0.1 2020/5/29 17:05
 * @since 1.5
 */
public class LimitedRangePageTest {

    public LimitedRangePageTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * 测试普通使用方式.返回的行号应该限定在指定的行号内.
     */
    @Test
    public void testNormalPage() {

        int index = 1;
        int size = 10;
        int dataTotal = 1000;
        long rangeL = 3;
        long rangeR = 7;
        Page page = new LimitedRangePage(index,size,rangeL,rangeR);
        page.setTotalCount(dataTotal);
        PageScope scope = page.getNextPage();
        Assert.assertEquals(scope.startLine, 3);
        Assert.assertEquals(scope.endLine, 7);

        scope = page.getNextPage();
        Assert.assertEquals(scope.startLine, 13);
        Assert.assertEquals(scope.endLine, 17);

        scope = page.getNextPage();
        Assert.assertEquals(scope.startLine, 23);
        Assert.assertEquals(scope.endLine, 27);
    }

    /**
     * 测试获取指定页号.
     */
    @Test
    public void testAppointPage() {
        int index = 1;
        int size = 10;
        int dataTotal = 1000;
        long rangeL = 3;
        long rangeR = 7;
        Page page = new LimitedRangePage(index,size,rangeL,rangeR);
        page.setTotalCount(dataTotal);

        PageScope scope = page.getAppointPage(5);
        Assert.assertEquals(scope.startLine, 43);
        Assert.assertEquals(scope.endLine, 47);

        //获取一个超出总量的页号.
        scope = page.getAppointPage(page.getPageCount() + 1);
        Assert.assertNull(scope);
    }
}
