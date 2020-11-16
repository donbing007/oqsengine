package com.xforceplus.ultraman.oqsengine.common.parser;

/**
 * 解析器.
 *
 * @author dongbin
 * @version 0.1 2020/11/16 12:00
 * @since 1.8
 */
public interface Parser<IN, OUT> {

    /**
     * 解析.
     *
     * @param in 解析目标.
     * @return 解析结果.
     */
    OUT parse(IN in);
}
