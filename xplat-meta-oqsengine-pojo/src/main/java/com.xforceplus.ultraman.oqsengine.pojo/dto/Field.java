package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 字段对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class Field implements Serializable {

    /**
     * 字段名称
     */
    private String name;

    /**
     * 字段类型
     */
    private String fieldType;

}