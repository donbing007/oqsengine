package com.xforceplus.ultraman.oqsengine.metadata.mock.generator;

import java.util.List;

/**
 * @author j.xu
 * @version 0.1 2021/05/2021/5/14
 * @since 1.8
 */
public class ExpectedEntityStorage {
    private List<Long> ancestors;
    private Long self;
    private Long father;
    private List<Long> relationIds;

    /**
     * 实例.
     */
    public ExpectedEntityStorage(Long self, Long father, List<Long> ancestors, List<Long> relationIds) {
        this.self = self;
        this.father = father;
        this.ancestors = ancestors;
        this.relationIds = relationIds;
    }

    public List<Long> getAncestors() {
        return ancestors;
    }

    public Long getSelf() {
        return self;
    }

    public Long getFather() {
        return father;
    }

    public List<Long> getRelationIds() {
        return relationIds;
    }
}
