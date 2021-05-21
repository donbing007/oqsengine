package com.xforceplus.ultraman.oqsengine.common.parser;

import java.util.Map;

/**
 * 解析结果以KEY-VALUE形式返回.
 *
 * @param <I>  解析目标.
 * @param <O> 解析结果.
 * @author dongbin
 * @version 0.1 2020/11/16 12:15
 * @since 1.8
 */
public interface KeyValueParser<I, O extends Map> extends Parser<I, O> {
}
