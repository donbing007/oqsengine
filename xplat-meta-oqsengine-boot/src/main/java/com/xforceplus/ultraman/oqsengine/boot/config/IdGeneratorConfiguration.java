package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.RedisOrderContinuousLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.KubernetesStatefulsetNodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.node.StaticNodeIdGenerator;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * id 生成器配置.
 *
 * @author dongbin
 * @version 0.1 2020/11/13 15:06
 * @since 1.8
 */
@Configuration
public class IdGeneratorConfiguration {

    @ConditionalOnProperty(name = "instance.type", havingValue = "statefulset", matchIfMissing = false)
    @Bean("nodeIdGenerator")
    public NodeIdGenerator kubernetesStatefulsetNodeIdGenerator() {
        return new KubernetesStatefulsetNodeIdGenerator();
    }

    @ConditionalOnProperty(name = "instance.type", havingValue = "static", matchIfMissing = true)
    @Bean("nodeIdGenerator")
    public NodeIdGenerator staticNodeIdGenerator(@Value("${instance.id:0}") int instanceId) {
        return new StaticNodeIdGenerator(instanceId);
    }

    /**
     * 偏序,但不连续的long类型ID生成器.
     *
     * @param nodeIdGenerator 结点ID生成器.
     * @return 实例.
     */
    @Bean("longNoContinuousPartialOrderIdGenerator")
    public LongIdGenerator longNoContinuousPartialOrderIdGenerator(@Qualifier("nodeIdGenerator") NodeIdGenerator nodeIdGenerator) {
        return new SnowflakeLongIdGenerator(nodeIdGenerator);
    }

    /**
     * 连续且偏序的long类型ID生成器.
     * 这里设置了默认的命名空间,此命名空间专门供给commitId生成.
     * 调用时一定需要设置namespace.
     *
     * @param redisClient redis客户端.
     * @return 实例.
     */
    @Bean("longContinuousPartialOrderIdGenerator")
    public LongIdGenerator longContinuousPartialOrderIdGenerator(@Qualifier("redisClientState") RedisClient redisClient) {
        return new RedisOrderContinuousLongIdGenerator(redisClient);
    }
}
