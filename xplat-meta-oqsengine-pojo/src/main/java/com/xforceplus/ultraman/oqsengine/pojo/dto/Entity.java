package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class Entity implements Serializable {

    /**
     * 数据id
     */
    private Long id;
    /**
     * 数据结构
     */
    private EntityClass entityClass;
    /**
     * 数据集合
     */
    private EntityValue entityValue;

}