package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 元数据对象-结构描述对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class EntityClass implements Serializable {
    /**
     * 元数据boId
     */
    private Long id;
    /**
     * 关系信息
     */
    private String relaton;
    /**
     * 子对象结构信息
     */
    private List<EntityClass> entityClasss;
    /**
     * 对象属性信息
     */
    private List<Field> fields;
}