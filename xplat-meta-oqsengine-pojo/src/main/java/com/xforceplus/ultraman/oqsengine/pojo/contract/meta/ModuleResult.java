package com.xforceplus.ultraman.oqsengine.pojo.contract.meta;

import com.xforceplus.ultraman.oqsengine.pojo.contract.Result;

import java.io.Serializable;
import java.util.Collection;

/**
 * 模块数据同步结果对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class ModuleResult extends Result implements Serializable {

    public ModuleResult(Object status) {
        super(status);
    }

    public ModuleResult(Object status, String message) {
        super(status, message);
    }

    public ModuleResult(Object status, Collection values, String message) {
        super(status, values, message);
    }
}
