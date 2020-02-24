package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.storage.selector.DataSourceHashSelector;
import com.xforceplus.ultraman.oqsengine.storage.selector.Selector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.sql.DataSource;

/**
 * 数据源配置.
 * @author dongbin
 * @version 0.1 2020/2/24 16:41
 * @since 1.8
 */
@Configuration
public class DataSourceConfiguration {

    @Resource
    private DataSourcePackage dataSourcePackage;

    @Bean
    public DataSourcePackage dataSourcePackage() {
        return DataSourceFactory.build();
    }

    @Bean
    public Selector<DataSource> indexWriteDataSourceSelector() {
        return new DataSourceHashSelector(dataSourcePackage.getIndexWriter());
    }

    @Bean
    public Selector<DataSource> indexSearchDataSourceSelector() {
        return new DataSourceHashSelector(dataSourcePackage.getIndexSearch());
    }

    @Bean
    public Selector<DataSource> masterDataSourceSelector() {
        return new DataSourceHashSelector(dataSourcePackage.getMaster());
    }
}
