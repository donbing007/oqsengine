package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.changelog.ReplayService;
import com.xforceplus.ultraman.oqsengine.changelog.impl.ReplayServiceImpl;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.QueryStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.query.impl.SQLQueryStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.ChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.SnapshotStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl.SQLChangelogStorage;
import com.xforceplus.ultraman.oqsengine.changelog.storage.write.impl.SQLSnapshotStorage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChangelogConfiguration {

    @Bean
    public QueryStorage queryStorage(){
        return new SQLQueryStorage();
    }

    @Bean
    public ChangelogStorage changelogStorage(){
        return new SQLChangelogStorage();
    }

    @Bean
    public SnapshotStorage snapshotStorage(){
        return new SQLSnapshotStorage();
    }

    @Bean
    public ReplayService replayService(){
        return new ReplayServiceImpl();
    }
}
