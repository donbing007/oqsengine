package com.xforceplus.ultraman.oqsengine.storage.undo.constant;

/**
 * 版权：    上海云砺信息科技有限公司
 * 创建者:   youyifan
 * 创建时间: 4/20/2020 6:00 PM
 * 功能描述: flag 0-未提交 1-已提交 2-错误 表示数据的数据库提交状态
 * 修改历史:
 */
public enum UndoLogStatus {
    UNCOMMITTED(0),
    COMMITED(1),
    ERROR(2);

    Integer value;

    UndoLogStatus(Integer value) {
        this.value = value;
    }

    public Integer value(){
        return this.value;
    }
}
