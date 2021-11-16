package com.xforceplus.ultraman.oqsengine.pojo.page;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 分页对象的测试.
 *
 * @author dongbin
 * @version 0.1 2020/5/29 17:05
 * @since 1.5
 */
public class PageTest {

    /**
     * 普通情况的测试,能过构造方法设置当前序号和分页大小.
     * 设置总量后获取当前页的开始序号和结束序号.
     */
    @Test
    public void testNormalPage() {

        int dataTotal = 1000;
        //默认构造测试.应该使用默认的第1页和10的分页大小.
        Page page = new Page();
        page.setTotalCount(dataTotal);
        PageScope scope = page.getNextPage();
        Assertions.assertEquals(scope.startLine, 1);
        Assertions.assertEquals(scope.endLine, 10);
        Assertions.assertEquals(page.getIndex(), 2);
        Assertions.assertEquals(page.getPageCount(), countPageCount(10, dataTotal));

        //指定当前序号为2并设置分页大小为11.
        int index = 2;
        int size = 11;
        page = new Page(index, size);
        page.setTotalCount(dataTotal);
        scope = page.getNextPage();
        Assertions.assertEquals(scope.startLine, index * size - size + 1);
        Assertions.assertEquals(scope.endLine, size * index);
        Assertions.assertEquals(page.getIndex(), index + 1);
        Assertions.assertEquals(page.getPageCount(), countPageCount(size, dataTotal));

        //获取第3页
        scope = page.getNextPage();
        index++;
        Assertions.assertEquals(scope.startLine, index * size - size + 1);
        Assertions.assertEquals(scope.endLine, size * index);
        Assertions.assertEquals(page.getIndex(), index + 1);
        Assertions.assertEquals(page.getPageCount(), countPageCount(size, dataTotal));
    }

    /**
     * 测试显示最后一页.调用静态化方法,得到一个已经完成准备的实例.
     */
    @Test
    public void testLastPage() {

        int size = 11;
        int dataTotal = 1000;
        //最后一页.
        Page lastPage = Page.lastPage(size);
        lastPage.setTotalCount(dataTotal);
        PageScope scope = lastPage.getNextPage();
        long pageCount = countPageCount(size, dataTotal);
        long remainingCount = size * (pageCount - 1) + 1;
        Assertions.assertEquals(scope.startLine, remainingCount);
        Assertions.assertEquals(scope.endLine, dataTotal);
        Assertions.assertEquals(lastPage.getIndex(), pageCount + 1);
        Assertions.assertEquals(lastPage.getPageCount(), pageCount);

        //直接使用构造方法,应该结果同上.
        lastPage = Page.lastPage(size);
        lastPage.setTotalCount(dataTotal);
        scope = lastPage.getNextPage();
        Assertions.assertEquals(scope.startLine, remainingCount);
        Assertions.assertEquals(scope.endLine, dataTotal);
        Assertions.assertEquals(lastPage.getIndex(), pageCount + 1);
        Assertions.assertEquals(lastPage.getPageCount(), pageCount);

        //已经为最后一页,再次获取返回null.
        scope = lastPage.getNextPage();
        Assertions.assertNull(scope);
        Assertions.assertFalse(lastPage.hasNextPage());
    }

    /**
     * 测试获取指定页.
     */
    @Test
    public void testAppointPage() {
        int size = 11;
        int dataTotal = 1000;
        Page page = new Page(1, size);
        page.setTotalCount(dataTotal);

        long point = 1;

        PageScope scope = page.getAppointPage(point);
        Assertions.assertEquals(scope.startLine, point);
        Assertions.assertEquals(scope.endLine, size);

        point = 10;
        scope = page.getAppointPage(point);
        Assertions.assertEquals(scope.startLine, size * point - size + 1);
        Assertions.assertEquals(scope.endLine, size * point);
    }

    /**
     * 测试没有设置数据总量时的异常.
     */
    @Test
    public void testNotReady() {
        Page page = new Page(1, 10);

        Assertions.assertThrows(
            IllegalStateException.class,
            () -> page.getNextPage()
        );

        Assertions.assertThrows(
            IllegalStateException.class,
            () -> page.getAppointPage(1)
        );
    }

    /**
     * 测试数据总量受限.
     */
    @Test
    public void testLimitTotalCount() {
        Page page = new Page(1, 10);
        page.setVisibleTotalCount(200);
        page.setTotalCount(1000);
        Assertions.assertEquals(1000, page.getTotalCount());
        Assertions.assertEquals(20, page.getPageCount());

        page = new Page(1, 10);
        page.setVisibleTotalCount(1);
        page.setTotalCount(100);
        Assertions.assertEquals(100, page.getTotalCount());
        Assertions.assertEquals(1, page.getPageCount());

        Page usePage = new Page(1, 10);
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> usePage.setVisibleTotalCount(-200)
        );

        page = new Page(4, 199);
        page.setVisibleTotalCount(200);
        page.setTotalCount(3000);

        Assertions.assertEquals(3000, page.getTotalCount());
        Assertions.assertEquals(2, page.getPageCount());
        Assertions.assertEquals(4, page.getIndex());
        Assertions.assertFalse(page.hasNextPage());
        Assertions.assertNull(page.getNextPage());
    }

    @Test
    public void testZeroTotalCount() {
        Page page = new Page(2, 10);
        page.setTotalCount(0);

        Assertions.assertEquals(0, page.getTotalCount());
        Assertions.assertEquals(0, page.getPageCount());
        Assertions.assertFalse(page.hasNextPage());
        PageScope scope = page.getNextPage();
        Assertions.assertNull(scope);
    }

    @Test
    public void testEmptyPage() {
        Page page = Page.emptyPage();
        page.setTotalCount(1000);

        Assertions.assertEquals(0, page.getPageCount());
        Assertions.assertFalse(page.hasNextPage());
        PageScope scope = page.getNextPage();
        Assertions.assertNull(scope);
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        Page sourcePage = Page.emptyPage();
        sourcePage.setTotalCount(100);
        Page clonePage = sourcePage.clone();

        Assertions.assertTrue(clonePage.isEmptyPage());
        Assertions.assertEquals(100, clonePage.getTotalCount());
        clonePage.setVisibleTotalCount(2000);
        Assertions.assertEquals(2000, clonePage.getVisibleTotalCount());
        Assertions.assertEquals(-1, sourcePage.getVisibleTotalCount());

    }

    /**
     * 计算总页数.
     *
     * @param pageNumber  分页大小.
     * @param totalNumber 数据总量.
     * @return 总页数.
     */
    private long countPageCount(long pageNumber, long totalNumber) {
        return totalNumber / pageNumber + (totalNumber % pageNumber == 0 ? 0 : 1);
    }
}
