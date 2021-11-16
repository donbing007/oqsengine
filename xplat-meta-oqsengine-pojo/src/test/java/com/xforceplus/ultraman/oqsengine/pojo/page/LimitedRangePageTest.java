package com.xforceplus.ultraman.oqsengine.pojo.page;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试限制行号的分页对象.
 *
 * @author dongbin
 * @version 0.1 2020/5/29 17:05
 * @since 1.5
 */
public class LimitedRangePageTest {

    public LimitedRangePageTest() {
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
        Page page = new LimitedRangePage(index, size, rangeL, rangeR);
        page.setTotalCount(dataTotal);
        PageScope scope = page.getNextPage();
        Assertions.assertEquals(scope.startLine, 3);
        Assertions.assertEquals(scope.endLine, 7);

        scope = page.getNextPage();
        Assertions.assertEquals(scope.startLine, 13);
        Assertions.assertEquals(scope.endLine, 17);

        scope = page.getNextPage();
        Assertions.assertEquals(scope.startLine, 23);
        Assertions.assertEquals(scope.endLine, 27);
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
        Page page = new LimitedRangePage(index, size, rangeL, rangeR);
        page.setTotalCount(dataTotal);

        PageScope scope = page.getAppointPage(5);
        Assertions.assertEquals(scope.startLine, 43);
        Assertions.assertEquals(scope.endLine, 47);

        //获取一个超出总量的页号.
        scope = page.getAppointPage(page.getPageCount() + 1);
        Assertions.assertNull(scope);
    }
}
