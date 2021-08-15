package com.xforceplus.ultraman.oqsengine.core.service.integration.grpc.devops.mock;

import java.util.List;

/**
 * Created by justin.xu on 08/2021.
 *
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
