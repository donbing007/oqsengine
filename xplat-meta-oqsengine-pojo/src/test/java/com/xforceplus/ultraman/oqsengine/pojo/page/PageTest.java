package com.xforceplus.ultraman.oqsengine.pojo.page;

import org.junit.*;

/**
 * 分页对象的测试.
 *
 * @author dongbin
 * @version 0.1 2020/5/29 17:05
 * @since 1.5
 */
public class PageTest {

    public PageTest() {
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
        Assert.assertEquals(scope.startLine, 1);
        Assert.assertEquals(scope.endLine, 10);
        Assert.assertEquals(page.getIndex(), 2);
        Assert.assertEquals(page.getPageCount(), countPageCount(10, dataTotal));

        //指定当前序号为2并设置分页大小为11.
        int index = 2;
        int size = 11;
        page = new Page(index, size);
        page.setTotalCount(dataTotal);
        scope = page.getNextPage();
        Assert.assertEquals(scope.startLine, index * size - size + 1);
        Assert.assertEquals(scope.endLine, size * index);
        Assert.assertEquals(page.getIndex(), index + 1);
        Assert.assertEquals(page.getPageCount(), countPageCount(size, dataTotal));

        //获取第3页
        scope = page.getNextPage();
        index++;
        Assert.assertEquals(scope.startLine, index * size - size + 1);
        Assert.assertEquals(scope.endLine, size * index);
        Assert.assertEquals(page.getIndex(), index + 1);
        Assert.assertEquals(page.getPageCount(), countPageCount(size, dataTotal));
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
        Assert.assertEquals(scope.startLine, remainingCount);
        Assert.assertEquals(scope.endLine, dataTotal);
        Assert.assertEquals(lastPage.getIndex(), pageCount + 1);
        Assert.assertEquals(lastPage.getPageCount(), pageCount);

        //直接使用构造方法,应该结果同上.
        lastPage = Page.lastPage(size);
        lastPage.setTotalCount(dataTotal);
        scope = lastPage.getNextPage();
        Assert.assertEquals(scope.startLine, remainingCount);
        Assert.assertEquals(scope.endLine, dataTotal);
        Assert.assertEquals(lastPage.getIndex(), pageCount + 1);
        Assert.assertEquals(lastPage.getPageCount(), pageCount);

        //已经为最后一页,再次获取返回null.
        scope = lastPage.getNextPage();
        Assert.assertEquals(0, scope.getStartLine());
        Assert.assertEquals(0, scope.getEndLine());
        Assert.assertEquals(lastPage.hasNextPage(), false);

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
        Assert.assertEquals(scope.startLine, point);
        Assert.assertEquals(scope.endLine, size);

        point = 10;
        scope = page.getAppointPage(point);
        Assert.assertEquals(scope.startLine, size * point - size + 1);
        Assert.assertEquals(scope.endLine, size * point);
    }

    /**
     * 测试没有设置数据总量时的异常.
     */
    @Test
    public void testNotReady() {
        Page page = new Page(1, 10);

        try {
            page.getNextPage();
            Assert.fail("Not ready, but did not throw an exception.");
        } catch (IllegalStateException ex) {
        }
        try {
            page.getAppointPage(1);
            Assert.fail("Not ready, but did not throw an exception.");
        } catch (IllegalStateException ex) {
        }
    }

    /**
     * 测试数据总量受限.
     */
    @Test
    public void testLimitTotalCount() {
        Page page = new Page(1, 10);
        page.setVisibleTotalCount(200);
        page.setTotalCount(1000);
        Assert.assertEquals(1000, page.getTotalCount());
        Assert.assertEquals(20, page.getPageCount());

        page = new Page(1, 10);
        page.setVisibleTotalCount(1);
        page.setTotalCount(100);
        Assert.assertEquals(100, page.getTotalCount());
        Assert.assertEquals(1, page.getPageCount());

        page = new Page(1, 10);
        try {
            page.setVisibleTotalCount(-200);
            Assert.fail("An exception was expected to be thrown, but it didn't.");
        } catch (Exception ex) {

        }

        page = new Page(4, 199);
        page.setVisibleTotalCount(200);
        page.setTotalCount(3000);

        Assert.assertEquals(3000, page.getTotalCount());
        Assert.assertEquals(2, page.getPageCount());
        Assert.assertEquals(4, page.getIndex());
        Assert.assertFalse(page.hasNextPage());
    }

    @Test
    public void testZeroTotalCount() {
        Page page = new Page(2, 10);
        page.setTotalCount(0);

        Assert.assertEquals(0, page.getTotalCount());
        Assert.assertEquals(0, page.getPageCount());
        PageScope scope = page.getNextPage();
        Assert.assertEquals(0, scope.startLine);
        Assert.assertEquals(0, scope.endLine);

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
