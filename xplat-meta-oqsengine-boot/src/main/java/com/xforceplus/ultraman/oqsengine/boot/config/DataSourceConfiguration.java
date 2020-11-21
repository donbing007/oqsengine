package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc.HashPreciseShardingAlgorithm;
import com.xforceplus.ultraman.oqsengine.common.datasource.shardjdbc.SuffixNumberHashPreciseShardingAlgorithm;
import com.xforceplus.ultraman.oqsengine.common.selector.HashSelector;
import com.xforceplus.ultraman.oqsengine.common.selector.Selector;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 数据源配置.
 *
 * @author dongbin
 * @version 0.1 2020/2/24 16:41
 * @since 1.8
 */
@Configuration
public class DataSourceConfiguration {

    @Bean
    public DataSourcePackage dataSourcePackage() {
        return DataSourceFactory.build();
    }

    @Bean
    @DependsOn("dataSourcePackage")
    public Selector<DataSource> indexWriteDataSourceSelector(DataSourcePackage dataSourcePackage) {
        return new HashSelector(dataSourcePackage.getIndexWriter());
    }

    @Bean
    @DependsOn("dataSourcePackage")
    public DataSource indexSearchDataSource(DataSourcePackage dataSourcePackage) {
        return dataSourcePackage.getIndexSearch().get(0);
    }

    @Bean
    @DependsOn("dataSourcePackage")
    public DataSource devOpsDataSource(DataSourcePackage dataSourcePackage) {
        return dataSourcePackage.getDevOps();
    }

    @Bean
    @DependsOn("dataSourcePackage")
    public DataSource masterDataSource(DataSourcePackage dataSourcePackage,
                                       @Value("${storage.master.name:oqsbigentity}") String baseName,
                                       @Value("${storage.master.shard.table.enabled:false}") boolean shard,
                                       @Value("${storage.master.shard.table.size:1}") int shardSize
    ) throws SQLException {
        if (!shard) {
            return dataSourcePackage.getMaster().get(0);
        } else {
            AtomicInteger index = new AtomicInteger(0);
            Map<String, DataSource> dsMap = dataSourcePackage.getMaster().stream().collect(Collectors.toMap(
                d -> "ds" + index.getAndIncrement(), d -> d));

            int dsSize = dsMap.size();

            TableRuleConfiguration tableRuleConfiguration = new TableRuleConfiguration(
                baseName, String.format("ds${0..%d}.%s${0..%d}", dsSize - 1, baseName, shardSize - 1));
            tableRuleConfiguration.setDatabaseShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("id", new HashPreciseShardingAlgorithm()));
            tableRuleConfiguration.setTableShardingStrategyConfig(
                new StandardShardingStrategyConfiguration("id", new SuffixNumberHashPreciseShardingAlgorithm()));


            ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
            shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfiguration);

            return ShardingDataSourceFactory.createDataSource(dsMap, shardingRuleConfig, new Properties());
        }
    }
}
