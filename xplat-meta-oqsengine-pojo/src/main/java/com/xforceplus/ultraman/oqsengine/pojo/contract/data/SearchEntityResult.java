package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.dto.Entity;

import java.io.Serializable;
import java.util.List;

/**
 * 删除数据对象返回结果.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class SearchEntityResult implements Serializable {
    private List<Entity> entities;
}