package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.conditions.Order;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;

import java.io.Serializable;

/**
 * 数据条件查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class EntityConditionsSearch implements Serializable {
    private EntitySource entitySource;
    private Conditions conditions;
    private Order order;
    private Page page;
}