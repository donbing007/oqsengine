package com.xforceplus.ultraman.oqsengine.pojo.conditions;

import lombok.Data;

import java.io.Serializable;

/**
 * 排序对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class OrderItem implements Serializable {
    private String name;
    private String alias;
    /**
     * 是否正序排列，默认 true
     */
    private boolean asc = true;
}