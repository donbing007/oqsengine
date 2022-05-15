package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser;


import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import java.util.List;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public interface BinLogParser {


    /**
     * 合并ID相同的记录.
     * @param columns 原始数据集.
     * @param parserContext 上下文.
     * @param parseResult 解析结果.
     */
    void merge(List<CanalEntry.Column> columns, ParserContext parserContext, ParseResult parseResult);

    /**
     * 将rowChange按照 动态/静态 分类.
     *
     * @param parserContext 上下文.
     * @param parseResult   结果集.
     */
    void parser(ParserContext parserContext, ParseResult parseResult);
}
