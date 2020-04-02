package com.xforceplus.ultraman.oqsengine.common.id.node;

/**
 * 静态的结点 id 生成器.
 *
 * @author dongbin
 * @version 0.1 2020/4/1 17:56
 * @since 1.8
 */
public class StaticNodeIdGenerator implements NodeIdGenerator {

    private int id;

    public StaticNodeIdGenerator(int id) {
        this.id = id;
    }

    @Override
    public Integer next() {
        return id;
    }
}
