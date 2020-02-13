package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 字典内容对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class DictDetail implements Serializable {
    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;
}