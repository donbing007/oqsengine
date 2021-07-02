package com.xforceplus.ultraman.oqsengine.changelog.config;

import com.xforceplus.ultraman.oqsengine.changelog.ChangelogService;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.SnapshotService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.ChangeSnapshot;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.Gateway;
import com.xforceplus.ultraman.oqsengine.changelog.gateway.impl.DefaultChangelogGateway;
import com.xforceplus.ultraman.oqsengine.changelog.handler.ChangelogCommandHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.DefaultChangelogCommandHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.PersistentEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.PropagationEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.handler.impl.SnapshotEventHandler;
import com.xforceplus.ultraman.oqsengine.changelog.impl.DefaultChangelogImpl;
import com.xforceplus.ultraman.oqsengine.changelog.impl.DefaultSnapshotServiceImpl;
import com.xforceplus.ultraman.oqsengine.changelog.impl.ReplayServiceImpl;
import com.xforceplus.ultraman.oqsengine.changelog.relation.ManyToOneRelationChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.relation.RelationAwareChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.SnapshotStorage;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.vavr.control.Either;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class ChangelogConfiguration {

    @Bean
    public RedisClient redisClient() {

        RedisClient redisClient = null;
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        redisClient = RedisClient.create(RedisURI.Builder.redis(redisIp, redisPort).build());
        return redisClient;
    }

    @Bean
    public SnapshotEventHandler snapshotEventHandler() {
        return new SnapshotEventHandler();
    }

    @Bean
    public SnapshotService snapshotService() {
        return new DefaultSnapshotServiceImpl();
    }

    @Bean
    public Gateway gateway() {
        return new DefaultChangelogGateway();
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
    public ChangelogExample example(IdGenerator<Long> versionIdGenerator) {
        return new ChangelogExample(versionIdGenerator);
    }

    @Bean
    public ChangelogService changelogService() {
        ChangelogService changelogService = new DefaultChangelogImpl();
        return changelogService;
    }

    @Bean
    public ReplayService replayService() {
        ReplayService replayService = new ReplayServiceImpl();
        return replayService;
    }

    @Bean
    public RelationAwareChangelog manyToOne() {
        return new ManyToOneRelationChangelog();
    }

    @Bean
    public LongIdGenerator snowflakeIdGenerator() {

        AtomicLong atomicLong = new AtomicLong(0);
        return new LongIdGenerator() {
            @Override
            public boolean isContinuous() {
                return false;
            }

            @Override
            public boolean isPartialOrder() {
                return false;
            }

            @Override
            public Long next() {
                return atomicLong.getAndIncrement();
            }
        };
    }

    @Bean
    public LongIdGenerator versionIdGenerator() {

        AtomicLong atomicLong = new AtomicLong(10000);
        return new LongIdGenerator() {
            @Override
            public boolean isContinuous() {
                return false;
            }

            @Override
            public boolean isPartialOrder() {
                return false;
            }

            @Override
            public Long next() {
                return atomicLong.getAndIncrement();
            }
        };
    }

    @Bean
    public SnapshotStorage snapshotStorage() {
        return new SnapshotStorage() {
            @Override
            public Either<SQLException, Integer> saveSnapshot(ChangeSnapshot changeSnapshot) {
                return null;
            }

            @Override
            public Optional<ChangeSnapshot> query(long objId, long version) {
                return Optional.empty();
            }
        };
    }

    //mock Changelog storage
    @Bean
    public ChangelogStorage changelogStorage(ChangelogExample example) {
        return new ChangelogStorage() {
            @Override
            public Either<SQLException, Integer> saveBatch(List<Changelog> changeLogs) {
                return null;
            }

            @Override
            public List<Changelog> findById(long id, long endVersion, long startVersion) {
                return example.getChangelogByIdVersion(id, endVersion);
            }
        };
    }

    @Bean
    public MetaManager metaManager(ChangelogExample example) {
        return new MetaManager() {
            @Override
            public Optional<IEntityClass> load(long id) {
                return Optional.ofNullable(example.getEntityClassById(id));
            }

            @Override
            public Optional<IEntityClass> load(long id, String profile) {
                return Optional.ofNullable(example.getEntityClassById(id));
            }

            @Override
            public Optional<IEntityClass> loadHistory(long id, int version) {
                return Optional.empty();
            }

            @Override
            public int need(String appId, String env) {
                return 0;
            }

            @Override
            public void invalidateLocal() {

            }
        };
    }
}
