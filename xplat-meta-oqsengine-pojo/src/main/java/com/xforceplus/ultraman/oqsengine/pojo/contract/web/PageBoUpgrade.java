package com.xforceplus.ultraman.oqsengine.pojo.contract.web;

import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;

import java.io.Serializable;
import java.util.List;

/**
 * 页面对象配置查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class PageBoUpgrade implements Serializable {
    private Long id;
    private String version;
    private List<PageBo> pageBos;
}