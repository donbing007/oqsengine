package com.xforceplus.ultraman.oqsengine.storage.undo.pojo;

import com.xforceplus.ultraman.oqsengine.storage.undo.constant.OpType;

import java.io.Serializable;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/20/2020 10:29 AM
 * 功能描述:
 * 修改历史:
 */
public class UndoLogItem implements Serializable {
    OpType opType;
    Object data;

    public UndoLogItem(OpType opType, Object data) {
        this.opType = opType;
        this.data = data;
    }

    public OpType getOpType() {
        return opType;
    }

    public void setOpType(OpType opType) {
        this.opType = opType;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
