package com.xforceplus.ultraman.oqsengine.changelog.config;

import com.xforceplus.ultraman.oqsengine.changelog.ChangelogService;
import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.domain.Changelog;
import com.xforceplus.ultraman.oqsengine.changelog.impl.DefaultChangelogImpl;
import com.xforceplus.ultraman.oqsengine.changelog.impl.ReplayServiceImpl;
import com.xforceplus.ultraman.oqsengine.changelog.relation.ManyToOneRelationChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.relation.RelationAwareChangelog;
import com.xforceplus.ultraman.oqsengine.changelog.storage.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.common.id.IdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.MetaManager;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import io.vavr.control.Either;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class ChangelogConfiguration {

    @Bean
    public ChangelogExample example(IdGenerator<Long> versionIdGenerator){
        return new ChangelogExample(versionIdGenerator);
    }

    @Bean
    public ChangelogService changelogService(){
        ChangelogService changelogService = new DefaultChangelogImpl();
        return changelogService;
    }

    @Bean
    public ReplayService replayService(){
        ReplayService replayService = new ReplayServiceImpl();
        return replayService;
    }

    @Bean
    public RelationAwareChangelog manyToOne(){
        return new ManyToOneRelationChangelog();
    }

    @Bean
    public LongIdGenerator snowflakeIdGenerator(){

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
    public LongIdGenerator versionIdGenerator(){

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

    //mock Changelog storage
    @Bean
    public ChangelogStorage changelogStorage(ChangelogExample example){
        return new ChangelogStorage() {
            @Override
            public Either<SQLException, Integer> saveBatch(List<Changelog> changeLogs) {
                return null;
            }

            /**
             * TODO
             * @param id
             * @param version
             * @return
             */
            @Override
            public List<Changelog> findById(long id, long version) {
                //TODO
                return example.getChangelogByIdVersion(id, version);
            }
        };
    }

    @Bean
    public MetaManager metaManager(ChangelogExample example){
        return new MetaManager() {
            @Override
            public Optional<IEntityClass> load(long id) {
                return Optional.ofNullable(example.getEntityClassById(id));
            }

            @Override
            public IEntityClass loadHistory(long id, int version) {
                return null;
            }

            @Override
            public int need(String appId, String env) {
                return 0;
            }
        };
    }
}
