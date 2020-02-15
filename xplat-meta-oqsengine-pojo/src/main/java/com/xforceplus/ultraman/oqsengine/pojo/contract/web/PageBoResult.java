package com.xforceplus.ultraman.oqsengine.pojo.contract.web;

import com.xforceplus.ultraman.oqsengine.pojo.contract.Result;
import com.xforceplus.ultraman.oqsengine.pojo.dto.PageBo;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * 页面对象配置查询对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class PageBoResult extends Result implements Serializable{
    private List<PageBo> pageBos;

    public PageBoResult(Object status) {
        super(status);
    }

    public PageBoResult(Object status, String message) {
        super(status, message);
    }

    public PageBoResult(Object status, Collection values, String message) {
        super(status, values, message);
    }
}