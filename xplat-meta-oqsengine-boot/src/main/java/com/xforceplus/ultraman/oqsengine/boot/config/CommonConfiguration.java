package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.common.id.SnowflakeLongIdGenerator;
import com.xforceplus.ultraman.oqsengine.storage.index.sphinxql.optimizer.DefaultSphinxQLQueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.query.QueryOptimizer;
import com.xforceplus.ultraman.oqsengine.storage.selector.NumberIndexTableNameHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author dongbin
 * @version 0.1 2020/2/24 17:10
 * @since 1.8
 */
@Configuration
public class CommonConfiguration {


    @Value("{storage.master.shard.tableName}")
    private String masterTableName;

    @Value("{storage.master.shard.size}")
    private int masterSize;

    @Value("${instance.id}")
    private int instanceId;

    @Bean
    public LongIdGenerator longIdGenerator() {
        return new SnowflakeLongIdGenerator(instanceId);
    }

    @Bean
    public Selector<String> tableNameSelector() {
        return new NumberIndexTableNameHashSelector(masterTableName, masterSize);
    }

    @Bean
    public QueryOptimizer indexQueryOptimizer() {
        return new DefaultSphinxQLQueryOptimizer();
    }
}
