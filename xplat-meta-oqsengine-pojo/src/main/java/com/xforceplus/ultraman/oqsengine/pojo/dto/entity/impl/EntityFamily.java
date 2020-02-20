package com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityFamily;

/**
 * 继承家族信息实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/19 18:04
 * @since 1.8
 */
public class EntityFamily implements IEntityFamily {

    private long parent;
    private long child;

    public EntityFamily(long parent, long child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public long parent() {
        return 0;
    }

    @Override
    public long child() {
        return 0;
    }
}
