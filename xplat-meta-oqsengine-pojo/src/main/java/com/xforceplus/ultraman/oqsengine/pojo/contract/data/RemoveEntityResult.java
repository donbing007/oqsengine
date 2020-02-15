package com.xforceplus.ultraman.oqsengine.pojo.contract.data;

import com.xforceplus.ultraman.oqsengine.pojo.contract.Result;

import java.io.Serializable;
import java.util.Collection;

/**
 * 删除数据对象返回结果.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class RemoveEntityResult extends Result implements Serializable {

    public RemoveEntityResult(Object status) {
        super(status);
    }

    public RemoveEntityResult(Object status, String message) {
        super(status, message);
    }

    public RemoveEntityResult(Object status, Collection values, String message) {
        super(status, values, message);
    }
}