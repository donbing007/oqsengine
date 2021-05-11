package com.xforceplus.ultraman.oqsengine.pojo.page;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 一个可以限定分页信息中返回列表条数的上限和下限的实现.
 * 比如当前页面大小为10,总页数为5.现在需要获取第2页的第3至第7条数据.
 * Page page = new LimitedRangePage(2,10,3,7);
 * PageScope scope = page.getNextPage();
 * 那么返回的行数范围一定是第2页的第3行至第7行.
 *
 * @author dongbin
 * @version 1.00 2010-12-20
 * @since 1.5
 */
public class LimitedRangePage extends Page implements Externalizable, Cloneable {

    private static final long serialVersionUID = 9022842831962237841L;
    /**
     * 左边行号限制.
     */
    private long rangeL;
    /**
     * 右边行号限制.
     */
    private long rangeR;

    /**
     * 以默认参数构造一个限定行号的分页对象.
     * 使用超类的默认构造方法,同时行号限制为1至10.
     */
    public LimitedRangePage() {
        super();
        rangeL = 1;
        rangeR = 10;
    }

    /**
     * 构造分页对象,每次返回的分页行号都会限制在左(包含),右(包含)行号内.
     * 如果限定的开始行号等于1,结束行号等于分页大小那么将退化成原始的标准分页对象.
     *
     * @param index    当前页号.
     * @param pageSize 页面大小.
     * @param rangeL   页面中的开始最小行号.从1开始.
     * @param rangeR   页面中的结束最大行号.从1开始.
     */
    public LimitedRangePage(long index, long pageSize, long rangeL, long rangeR) {
        super(index, pageSize);
        if (rangeL <= 0 || rangeR <= 0) {
            throw new IllegalArgumentException(
                    "Range of data errors, line "
                            + "number limit is not less than about 1.");
        }
        if (rangeL > rangeR) {
            throw new IllegalArgumentException(
                    "Range of data errors, the left can not be equal to the "
                            + "right of the line number line number.");
        }
        this.rangeL = rangeL;
        this.rangeR = rangeR;
    }

    /**
     * 返回当前页,并限制了此页中的上下限数据.
     *
     * @return 数据范围.
     */
    @Override
    public PageScope getNextPage() {
        PageScope scope = super.getNextPage();
        if (scope == null) {
            return null;
        }
        return limitRange(scope);
    }

    /**
     * 指定页的指定范围的列表行数开始和结束..
     *
     * @param appointPageIndex 指定的页数.
     * @return 列表数据范围.
     */
    @Override
    public PageScope getAppointPage(long appointPageIndex) {
        PageScope scope = super.getAppointPage(appointPageIndex);
        if (scope == null) {
            return null;
        }
        return limitRange(scope);
    }

    /**
     * 限制返回结果的行号范围.
     *
     * @param scope 原始范围.
     * @return 新的范围.
     */
    private PageScope limitRange(PageScope scope) {
        long oldStart = scope.startLine;

        scope.startLine = oldStart + rangeL - 1;
        scope.endLine = oldStart + rangeR - 1;

        if (scope.startLine < oldStart) {
            scope.startLine = oldStart;
        }

        return scope;
    }

    /**
     * 在超类的基础上再读取左右限制行号.
     *
     * @param in 左右行号.
     * @throws IOException            I/O异常.
     * @throws ClassNotFoundException 类找不到.
     */
    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        rangeL = in.readLong();
        rangeR = in.readLong();
    }

    /**
     * 在超类的基础上再写入左右限制行号.
     *
     * @param out 输出流.
     * @throws IOException I/O异常.
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeLong(rangeL);
        out.writeLong(rangeR);
    }

    /**
     * 比较两个对象是否相等.除了超类的检查外还包含取的行数范围是否相等.
     *
     * @param obj 需要比较的对象.
     * @return true相等, false不相等.
     */
    @Override
    public boolean equals(Object obj) {
        boolean superEquals = super.equals(obj);
        if (superEquals) {
            final LimitedRangePage other = (LimitedRangePage) obj;
            if (this.rangeL != other.rangeL) {
                return false;
            }
            if (this.rangeR != other.rangeR) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成哈希码.
     *
     * @return 哈希码.
     */
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 89 * hash + (int) (this.rangeL ^ (this.rangeL >>> 32));
        hash = 89 * hash + (int) (this.rangeR ^ (this.rangeR >>> 32));
        return hash;
    }
}
