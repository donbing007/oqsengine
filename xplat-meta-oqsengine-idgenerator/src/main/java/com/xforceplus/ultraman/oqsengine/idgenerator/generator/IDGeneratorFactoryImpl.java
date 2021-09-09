package com.xforceplus.ultraman.oqsengine.idgenerator.generator;

import com.alibaba.google.common.collect.Maps;
import com.xforceplus.ultraman.oqsengine.common.metrics.MetricsDefine;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.NamedThreadFactory;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.IDModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl.LocalCacheGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.impl.RedisCacheGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import io.micrometer.core.annotation.Timed;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Resource;
import org.redisson.api.RedissonClient;


/**
 * 业务编号生成器工厂类.
 *
 * @author leo
 */
public class IDGeneratorFactoryImpl implements IDGeneratorFactory {


    @Resource
    private SegmentService segmentService;

    private ExecutorService executorService;

    private Map<String, IDGenerator> generators;
    private Map<String, IDGenerator> distributeGenerators;

    @Resource(name = "redissonClientAutoId")
    private RedissonClient redissonClient;

    /**
     * Constructor.
     */
    public IDGeneratorFactoryImpl() {
        this.generators = Maps.newConcurrentMap();
        this.distributeGenerators = Maps.newConcurrentMap();
        this.executorService = Executors.newSingleThreadExecutor(new NamedThreadFactory("oqs-id-generator"));
    }


    @Override
    @Timed(
        value = MetricsDefine.PROCESS_DELAY_LATENCY_SECONDS,
        extraTags = {"initiator", "innerGenerator", "action", "getGenerator"}
    )
    public IDGenerator getIdGenerator(String bizType) {
        IDModel model = segmentService.getIDModel(bizType);
        if (model.equals(IDModel.TREND_INC)) {
            return generators.computeIfAbsent(bizType, s -> createIdGenerator(s));
        } else if (model.equals(IDModel.LINEAR_INC)) {
            return distributeGenerators.computeIfAbsent(bizType, s -> createDistributeGenerator(s));
        } else {
            throw new IDGeneratorException("不支持的计数类型!");
        }
    }

    protected IDGenerator createIdGenerator(String bizType) {
        return new LocalCacheGenerator(bizType, segmentService, executorService);
    }

    protected IDGenerator createDistributeGenerator(String bizType) {
        return new RedisCacheGenerator(bizType, segmentService, executorService, redissonClient);
    }
}
