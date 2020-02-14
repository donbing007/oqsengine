package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 元数据对象-数据对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class EntityValue implements Serializable {
    /**
     * 元数据boId
     */
    private Long id;
    /**
     * 子对象数据信息
     */
    private List<EntityValue> entityValues;
    /**
     * 数据信息
     */
    private String value;//这部分采用的结构有疑虑

}