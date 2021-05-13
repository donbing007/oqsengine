package com.xforceplus.ultraman.oqsengine.idgenerator.generator;

import com.hazelcast.com.google.common.collect.Maps;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.IDModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl.DistributeCacheGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl.LocalCacheGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.util.HazelcastUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants.GENERATORS;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/7/21 5:22 PM
 */
public  class IDGeneratorFactoryImpl implements IDGeneratorFactory{


    @Autowired
    private SegmentService segmentService;

    private Map<String,IDGenerator> generators;
    private Map<String,IDGenerator> distributeGenerators;

    public IDGeneratorFactoryImpl() {

        this.generators = Maps.newConcurrentMap();
        this.distributeGenerators = Maps.newConcurrentMap();
    }

    @Override
    public IDGenerator getIdGenerator(String bizType) {
        IDModel model = segmentService.getIDModel(bizType);
        if(model.equals(IDModel.TREND_INC)) {
            return generators.computeIfAbsent(bizType, s -> createIdGenerator(s));
        }
        else if(model.equals(IDModel.LINEAR_INC)) {
            return distributeGenerators.computeIfAbsent(bizType,s->createDistributeGenerator(s));
        }
        else {
            throw new IDGeneratorException("不支持的计数类型!");
        }
    }

    protected IDGenerator createIdGenerator(String bizType) {
        return new LocalCacheGenerator(bizType, segmentService);
    }

    protected IDGenerator createDistributeGenerator(String bizType) {
        return new DistributeCacheGenerator(bizType,segmentService);
    }
}
