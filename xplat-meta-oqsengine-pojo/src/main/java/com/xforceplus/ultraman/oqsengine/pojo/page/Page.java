package com.xforceplus.ultraman.oqsengine.pojo.page;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 此类的作用是辅助分页,页面序号是从1开始.
 * 第一页是1,第二页是2.
 * 如果设置值singlePage为true那么相当于提示此次的分页一定是从1到N个数据，只会获取一次不会
 * 获取第一页后的数据。
 *
 * @author Mike
 * @version 1.01, 2010.02.22
 * @since 1.0
 */
public class Page implements Externalizable, Cloneable {

    private static final long serialVersionUID = -6034178522810109360L;

    private static final long UNSET = -1;
    /**
     * 默认页面大小.
     */
    private static final long DEFAULT_PAGE_SIZE = 10;
    /**
     * 默认页面页数.
     */
    private static final long DEFAULT_PAGE_INDEX = 1;
    /**
     * 是否为单页,即总共只需要开头一页,不会再要第二页.
     */
    private boolean singlePage = false;
    /**
     * 表示是否为空页.
     */
    private boolean emptyPage = false;
    /**
     * 页面大小.
     */
    private long pageSize;
    /**
     * 页面页数.
     */
    private long pageIndex;
    /**
     * 数据总量.
     */
    private long totalCount = UNSET;
    /**
     * 数据剩余总量.
     */
    private long surplusCount;
    /**
     * 页面总量.
     */
    private long pageCount;
    /**
     * 是否可以工作，默认为<tt>false</tt>.直到设置了数据总量才为<tt>true</tt>
     */
    private boolean ready = false;
    /**
     * 最后一页,此值为true表示直接定位为最后一页.
     */
    private boolean lastPage = false;

    /**
     * 可见数据量.
     */
    private long visibleTotalCount = UNSET;

    /**
     * 默认构造方法，以内定默认页面大小和当前页数构造.
     */
    public Page() {
        this(Page.DEFAULT_PAGE_INDEX, Page.DEFAULT_PAGE_SIZE);
    }

    /**
     * 以指定的页数和页面大小构造对象.
     *
     * @param pageIndex 当前页面序号,如果指定数小于1则使用默认设置1。
     * @param pageSize  页面大小，如果指数数小于1，则使用默认设置1.
     */
    public Page(long pageIndex, long pageSize) {
        if (pageIndex > 0) {
            this.pageIndex = pageIndex;
        } else {
            this.pageIndex = Page.DEFAULT_PAGE_INDEX;
        }

        if (pageSize > 0) {
            this.pageSize = pageSize;
        } else {
            this.pageSize = Page.DEFAULT_PAGE_SIZE;
        }
    }

    /**
     * 构造一个实际不要求任何页面的分页信息.
     * 表示实际不需要任何数据.
     * 返回的是一个没有准备好的实例.
     *
     * @return 分页实例.
     */
    public static Page emptyPage() {
        /*
         * 因定取第1页的第一条,实际这些值没有意义.
         */
        final long fixOne = 1;
        Page page = new Page(fixOne, fixOne);
        page.emptyPage = true;
        return page;
    }

    /**
     * 返回一个表示第一页的分页对象，并给出建议不会获取第一页以后的页数了。
     * 返回的实例是一个已经准备好的实例.
     *
     * @param pageSize 需要的记录数。
     * @return 构造好的分页对象。
     */
    public static Page newSinglePage(long pageSize) {
        Page page = new Page(1, pageSize);
        page.singlePage = true;
        page.setTotalCount(pageSize);
        return page;
    }

    /**
     * 返回一个表示最后一页的分页对象。此方法返回的是一个没有准备好的实例.
     *
     * @param pageSize 需要的记录数。
     * @return 构造好的分页对象。
     */
    public static Page lastPage(long pageSize) {
        Page page = new Page(0, pageSize);
        page.lastPage = true;
        return page;
    }

    /**
     * 返回当前的页的序号.
     *
     * @return 当前面的序号。
     */
    public long getIndex() {

        return pageIndex;
    }

    /**
     * 获取当前总页数.
     *
     * @return 总页数．
     */
    public long getPageCount() {
        if (ready) {
            if (isEmptyPage()) {
                return 0;
            } else {
                return pageCount;
            }
        } else {
            return 0;
        }
    }

    /**
     * 设置数据总量上限,如果大于此值将被限制为 limit.
     * 必须在调用 setTotalCount 之前设置.
     * 默认无限.
     * 设定后,分页计算时如果实际数据量超过此值将使用此值计算分页.
     *
     * @param totalCount 分页数据量上限.
     */
    public Page setVisibleTotalCount(long totalCount) {
        if (totalCount <= 0) {
            throw new IllegalArgumentException("The total amount of data must be a positive integer greater than 0.");
        }

        this.visibleTotalCount = totalCount;
        return this;
    }

    /**
     * 得到当前可见数据量.
     *
     * @return 可见数据量.
     */
    public long getVisibleTotalCount() {
        return visibleTotalCount;
    }

    /**
     * 是否设置了可视数据早上限.
     *
     * @return true 已经设置,false 没有设置.
     */
    public boolean hasVisibleTotalCountLimit() {
        return this.visibleTotalCount != UNSET;
    }

    /**
     * 设定数据总量，这个值必须在使用前设置.
     *
     * @param totalCount 数据总量
     */
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;

        if (!isEmptyPage()) {

            long useTotalCount = this.totalCount;

            if (visibleTotalCount != UNSET) {
                if (this.totalCount > this.visibleTotalCount) {
                    useTotalCount = this.visibleTotalCount;
                }
            }

            pageCount = this.countPageCount(getPageSize(), useTotalCount);
            if (lastPage) {
                pageIndex = pageCount;
            }
            surplusCount = countSurplus(pageIndex, getPageSize(), totalCount);
        }

        ready = true;
    }

    /**
     * 返回当前数据总量，初始为－１.
     *
     * @return 当前数据总量，如果为-1代表没有设置真实数据总量。
     */
    public long getTotalCount() {
        return this.totalCount;
    }

    /**
     * 返回下个页面,会将当前页面序号加1.
     *
     * @return 返回的PageScope对象内部包含开始行和结束行，为null表示已经没有下一页了。
     * @throws IllegalStateException 没有设置数据总量。
     */
    public PageScope getNextPage() {
        checkReady();

        if (!hasNextPage()) {
            return null;
        }

        long startLine = (getIndex() * getPageSize()) - (getPageSize() - 1);
        long endLine;
        surplusCount = countSurplus(pageIndex, getPageSize(), totalCount);
        if (surplusCount <= getPageSize()) {
            endLine = startLine + (surplusCount - 1);

        } else {
            endLine = startLine + (getPageSize() - 1);
        }

        pageIndex++;

        if (startLine < 0) {
            startLine = 0;
        }
        if (endLine < 0) {
            endLine = 0;
        }

        return new PageScope(startLine, endLine);
    }

    /**
     * 返回指定页面的开始行和结束行,如果指定的页号超出了当前页数总量范围将返回null.
     *
     * @param appointPageIndex 指定页面序号，第行页为１，第二页为２
     * @return PageScope表示指定页面的开始行和结束行
     * @throws IllegalStateException 没有设置数据总量。
     */
    public PageScope getAppointPage(long appointPageIndex) {
        checkReady();

        if (isEmptyPage()) {
            return new PageScope(0, 0);
        }

        long nowPointIndex = appointPageIndex;
        if (nowPointIndex <= 0) {
            return null;
        }
        if (nowPointIndex > pageCount) {
            return null;
        }

        long startLine = (nowPointIndex * getPageSize()) - (getPageSize() - 1);
        long endLine;
        surplusCount = countSurplus(nowPointIndex, getPageSize(), totalCount);
        if (surplusCount <= getPageSize()) {
            endLine = startLine + (surplusCount - 1);

        } else {
            endLine = startLine + (getPageSize() - 1);
        }

        return new PageScope(startLine, endLine);
    }

    /**
     * 判断是否已经没有下一页.
     *
     * @return 是否还有下一页, <tt>true</tt>还有可用页,<tt>false</tt>已经没有可用页了.
     */
    public boolean hasNextPage() {
        checkReady();
        if (isEmptyPage()) {
            return false;
        } else {
            return pageIndex <= pageCount;
        }
    }

    /**
     * 判断当前分页对象是否准备工作完成,已经获得了数据总量可以工作了.
     *
     * @return true准备完成, false数据总量还没有获得.
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * 返回此分页信息的字符串表示.
     * 该字符串由分页信息的＂页面大小＂，＂当前页面序号＂，＂总记录数＂，＂是否准备好＂组成．
     *
     * @return 此分页信息的字符串表示。
     */
    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append(getClass());
        if (isReady()) {
            buff.append("[");
            buff.append("PageSize=");
            buff.append(getPageSize());
            buff.append(",");

            buff.append("PageIndex=");
            buff.append(getIndex());
            buff.append(",");

            buff.append("PageCount=");
            buff.append(getPageCount());
            buff.append(",");

            buff.append(("TotalNumber="));
            buff.append(getTotalCount());
            buff.append("]");
        } else {
            buff.append(" can not work.");
        }

        return buff.toString();
    }

    /**
     * 克隆方法.
     *
     * @return 新的Page对象.
     * @throws CloneNotSupportedException 如果对象的类不支持 Cloneable 接口，
     *                                    则重写 clone 方法的子类也会抛出此异常，
     *                                    以指示无法复制某个实例。
     */
    @Override
    public Page clone() throws CloneNotSupportedException {
        return (Page) super.clone();
    }

    /**
     * 生成Page对象的哈希码.
     *
     * @return 哈希码.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.singlePage ? 1 : 0);
        hash = 89 * hash + (int) (this.pageSize ^ (this.pageSize >>> 32));
        hash = 89 * hash + (int) (this.pageIndex ^ (this.pageIndex >>> 32));
        hash = 89 * hash + (int) (this.totalCount ^ (this.totalCount >>> 32));
        hash = 89 * hash + (int) (this.surplusCount ^ (this.surplusCount >>> 32));
        hash = 89 * hash + (int) (this.pageCount ^ (this.pageCount >>> 32));
        hash = 89 * hash + (this.ready ? 1 : 0);
        hash = 89 * hash + (this.lastPage ? 1 : 0);
        hash = 89 * hash + (this.emptyPage ? 1 : 0);
        return hash;
    }

    /**
     * 比较两个对象是否表示同样数量中的同样分页大小下的同样序号.
     * 比如总量为1000的分页大小为10,第1页,是否为单页(只查从开头为止的N个数据).
     * 这三个数据相等返回ture,否则返回false.
     *
     * @param obj 　目标对象.如果不是Page的一个实例将返回false.
     * @return 是否相等．
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Page other = (Page) obj;

        //是否为单页
        if (isEmptyPage() != other.isEmptyPage()) {
            return false;
        }
        //是否为单页
        if (isSinglePage() != other.isSinglePage()) {
            return false;
        }
        //分页大小
        if (getPageSize() != other.getPageSize()) {
            return false;
        }
        //当前页号
        if (getIndex() != other.getIndex()) {
            return false;
        }

        //是否都是准备好的.
        if (isReady() != other.isReady()) {
            return false;
        }

        return true;
    }

    /**
     * 获取当前页面大小.
     *
     * @return 页面大小
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 返回当前分页建议.
     *
     * @return true当前是单页，只是当前一页。false需要进行正常分页.
     */
    public boolean isSinglePage() {
        return singlePage;
    }

    /**
     * 判断当前实例是否为空页.
     *
     * @return true 空页.false 非空页.
     */
    public boolean isEmptyPage() {
        return emptyPage;
    }

    /**
     * 计算页面总数,总量除以单页量后根据是否整除来决定是否增加1.
     *
     * @param pageNumber  页面大小
     * @param totalNumber 数据总量
     * @return 页面总数
     */
    protected long countPageCount(long pageNumber, long totalNumber) {
        return totalNumber / pageNumber + (totalNumber % pageNumber == 0 ? 0 : 1);
    }

    /**
     * 计算数据剩余量.
     *
     * @param indexNumber 当前页面序号.
     * @param sizeNumber  每页最大数据量.
     * @param totalNumber 数据总量.
     * @return 当前数据剩余量.
     */
    protected long countSurplus(long indexNumber, long sizeNumber,
                                long totalNumber) {
        return totalNumber - sizeNumber * (indexNumber - 1);
    }

    // 检查当前 page 是否可以工作.
    private void checkReady() {
        if (!ready) {
            throw new IllegalStateException("Have not designated data amount!");
        }
    }

    /**
     * 序列化写入方法.
     *
     * @param out 写入流.
     * @throws IOException I/O异常.
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeBoolean(singlePage);
        out.writeLong(pageSize);
        out.writeLong(pageIndex);
        out.writeLong(totalCount);
        out.writeLong(surplusCount);
        out.writeLong(pageCount);
        out.writeBoolean(ready);
        out.writeBoolean(lastPage);
        out.writeBoolean(emptyPage);
    }

    /**
     * 序列化读取方法,从流中读取字节转换成原始对象.
     *
     * @param in 读取流.
     * @throws IOException            I/O异常.
     * @throws ClassNotFoundException 没有找到类.
     */
    @Override
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        singlePage = in.readBoolean();
        pageSize = in.readLong();
        pageIndex = in.readLong();
        totalCount = in.readLong();
        surplusCount = in.readLong();
        pageCount = in.readLong();
        ready = in.readBoolean();
        lastPage = in.readBoolean();
        emptyPage = in.readBoolean();
    }

}
