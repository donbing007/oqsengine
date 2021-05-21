package com.xforceplus.ultraman.oqsengine.common.parser;

/**
 * 解析器.
 *
 * @param <I> 解析目标.
 * @param <O> 解析结果.
 * @author dongbin
 * @version 0.1 2020/11/16 12:00
 * @since 1.8
 */
public interface Parser<I, O> {

    /**
     * 解析.
     *
     * @param i 解析目标.
     * @return 解析结果.
     */
    O parse(I i);
}
