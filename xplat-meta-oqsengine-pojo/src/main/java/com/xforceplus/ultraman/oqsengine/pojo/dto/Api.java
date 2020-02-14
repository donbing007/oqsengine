package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 接口对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class Api implements Serializable {

    /**
     * 请求方式
     */
    private String method;
    /**
     * URL地址
     */
    private String url;

}