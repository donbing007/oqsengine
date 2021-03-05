package com.xforceplus.ultraman.oqsengine.changelog.domain;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.oqs.OqsRelation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EntityAggDomain {

    private IEntity rootIEntity;

    private Map<OqsRelation, List<EntityAggDomain>> graph = new HashMap<>();

    public IEntity getRootIEntity() {
        return rootIEntity;
    }

    public void setRootIEntity(IEntity rootIEntity) {
        this.rootIEntity = rootIEntity;
    }

    public Map<OqsRelation, List<EntityAggDomain>> getGraph() {
        return graph;
    }

    public void setGraph(Map<OqsRelation, List<EntityAggDomain>> graph) {
        this.graph = graph;
    }

    public void put(OqsRelation oqsRelation, EntityAggDomain entityAggDomain){
        List<EntityAggDomain> entityAggDomains = graph.computeIfAbsent(oqsRelation, k -> new LinkedList<>());
        entityAggDomains.add(entityAggDomain);
    }

    @Override
    public String toString() {
        return "EntityAggDomain{" +
                "rootIEntity=" + rootIEntity +
                ", graph=" + graph +
                '}';
    }
}
