package com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.task;

import com.xforceplus.ultraman.oqsengine.calculation.logic.aggregation.tree.ParseTree;
import com.xforceplus.ultraman.oqsengine.task.AbstractTask;
import java.io.Serializable;
import java.util.List;

/**
 * 聚合初始化任务.
 *
 * @author weikai
 * @version 1.0 2021/8/26 15:21
 * @since 1.8
 */
public class AggregationTask extends AbstractTask implements Serializable {

    public static final int DEFAULT_SIZE = 100;

    /**
     * 聚合任务初始化.
     *
     * @param prefix 聚合任务前缀 (appId-version).
     * @param parseTree EntityClass信息（包括聚合对象和被聚合对象的信息）.
     */
    public AggregationTask(String prefix, ParseTree parseTree) {
        this.prefix = prefix;
        this.parseTree = parseTree;
    }

    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }


    public List<Long> getRelationIds() {
        return relationIds;
    }

    public void setRelationIds(List<Long> relationIds) {
        this.relationIds = relationIds;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public void setParseTree(ParseTree parseTree) {
        this.parseTree = parseTree;
    }

    private ParseTree parseTree;

    private List<Long> relationIds;

    @Override
    public String toString() {
        return "AggregationTask{" + "prefix='" + prefix + '\'' + ", parseTree=" + parseTree + ", relationIds=" + relationIds + '}';
    }

    @Override
    public Class runnerType() {
        return AggregationTaskRunner.class;
    }
}
