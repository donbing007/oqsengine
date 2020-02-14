package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 页面对象配置对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class PageBo implements Serializable {

    /**
     * BoSeeting的id
     */
    private Long id;

    /**
     * 页面id
     */
    private Long pageId;

    /**
     * 业务对象名称
     */
    private String boName;

    /**
     * 配置信息
     */
    private String setting;

    /**
     * 版本信息
     */
    private String version;

}