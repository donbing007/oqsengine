package com.xforceplus.ultraman.oqsengine.pojo.dto.summary;

/**
 * desc :
 * name : Offset
 *
 * @author : xujia
 * date : 2020/8/21
 * @since : 1.8
 */
public interface Offset {
    /*
        获取偏移量id
     */
    long offset();
    /*
        设置偏移量id
     */
    void setOffset(long id);
    /*
        减少当前桶内数量
     */
    void decrease(int v);
    /*
        桶内剩余数量
     */
    int left();
}
