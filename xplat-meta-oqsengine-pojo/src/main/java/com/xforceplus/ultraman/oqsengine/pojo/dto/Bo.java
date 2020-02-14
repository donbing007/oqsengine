package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 元数据对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class Bo implements Serializable {
    /**
     * 对象id.
     */
    private Long id;
    /**
     * 业务对象编码
     */
    private String code;
    /**
     * ApiList列表
     */
    private List<Api> apis;
    /**
     * fieldList列表
     */
    private List<Field> fields;
    /**
     * 子对象List列表
     */
    private List<Bo> bos;

}