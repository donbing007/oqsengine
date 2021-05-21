package com.xforceplus.ultraman.oqsengine.boot.config;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xforceplus.ultraman.oqsengine.changelog.ChangelogHandler;
import com.xforceplus.ultraman.oqsengine.changelog.ChangelogService;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.SnapshotService;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.Gateway;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.impl.DefaultChangelogGateway;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogCommandHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.DefaultChangelogCommandHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.PersistentEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.PropagationEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.SnapshotEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.VersionEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.impl.DefaultChangelogImpl;
import com.xforceplus.ultraman.oqsengine.changelog.impl.DefaultSnapshotServiceImpl;
import com.xforceplus.ultraman.oqsengine.changelog.impl.RedisChangelogHandler;
import com.xforceplus.ultraman.oqsengine.changelog.impl.ReplayServiceImpl;
import com.xforceplus.ultraman.oqsengine.changelog.listener.EventLifecycleAware;
import com.xforceplus.ultraman.oqsengine.changelog.listener.flow.FlowRegistry;
import com.xforceplus.ultraman.oqsengine.changelog.listener.impl.RedisEventLifecycleHandler;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.impl.SQLQueryStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.SnapshotStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl.SQLChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl.SQLSnapshotStorage;
import com.xforceplus.ultraman.oqsengine.common.id.node.NodeIdGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import io.lettuce.core.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * changelog configuration.
 */
@ConditionalOnProperty("changelog.enabled")
@Configuration
public class ChangelogConfiguration {

    @Bean
    public QueryStorage queryStorage() {
        return new SQLQueryStorage();
    }

    @Bean
    public ChangelogStorage changelogStorage() {
        return new SQLChangelogStorage();
    }

    @Bean
    public SnapshotStorage snapshotStorage() {
        return new SQLSnapshotStorage();
    }

    @Bean
    public ReplayService replayService() {
        return new ReplayServiceImpl();
    }

    @Bean
    public ChangelogCommandHandler changelogCommandHandler() {
        return new DefaultChangelogCommandHandler();
    }

    @Bean
    public PersistentEventHandler persistentEventHandler() {
        return new PersistentEventHandler();
    }

    @Bean
    public PropagationEventHandler propagationEventHandler() {
        return new PropagationEventHandler();
    }

    @Bean
    public SnapshotEventHandler snapshotEventHandler() {
        return new SnapshotEventHandler();
    }

    @Bean
    public VersionEventHandler versionEventHandler() {
        return new VersionEventHandler();
    }

    @Bean
    public SnapshotService snapshotService() {
        return new DefaultSnapshotServiceImpl();
    }

    @Bean
    public Gateway gateway() {
        return new DefaultChangelogGateway();
    }

    /**
     * change log handler.
     */
    @Bean
    public ChangelogHandler changelogHandler(@Value("changelog.queue") String queueName,
                                             NodeIdGenerator nodeIdGenerator,
                                             RedisClient redisClientChangeLog,
                                             Gateway gateway,
                                             ObjectMapper mapper) {
        RedisChangelogHandler redisChangelogHandler =
            new RedisChangelogHandler(nodeIdGenerator.next().toString(), queueName, redisClientChangeLog, gateway,
                mapper);
        redisChangelogHandler.prepareConsumer();
        return redisChangelogHandler;
    }

    @Bean
    public ChangelogService changelogService() {
        return new DefaultChangelogImpl();
    }

    /**
     * RedisEventLifecycleHandler(RedisClient redisClient
     * , ChangelogHandler changelogHandler, ObjectMapper mapper) {.
     */
    @Bean
    public EventLifecycleAware eventLifecycleAware(RedisClient redisClient, ChangelogHandler changelogHandler,
                                                   ObjectMapper mapper, MetaManager manager,
                                                   FlowRegistry flowRegistry) {
        return new RedisEventLifecycleHandler(redisClient, changelogHandler, mapper, flowRegistry, manager);
    }

    @Bean
    public FlowRegistry flowRegistry(ActorMaterializer mat) {
        return new FlowRegistry(mat, 10000);
    }

    @Bean(destroyMethod = "terminate")
    @ConditionalOnMissingBean(ActorSystem.class)
    public ActorSystem actorSystem() {
        return ActorSystem.create("grpc-server");
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(ActorMaterializer.class)
    public ActorMaterializer mat(ActorSystem system) {
        return ActorMaterializer.create(system);
    }
}
