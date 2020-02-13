package com.xforceplus.ultraman.oqsengine.pojo.conditions;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 排序对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class Order implements Serializable {
    private List<OrderItem> orderItems;
}