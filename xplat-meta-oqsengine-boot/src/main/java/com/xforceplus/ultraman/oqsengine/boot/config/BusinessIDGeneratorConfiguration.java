package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/9/21 4:18 PM
 */
@Configuration
public class BusinessIDGeneratorConfiguration {

    /**
     * segment storage
     * @param tableName
     * @param maxQueryTimeMs
     * @return
     */
    @Bean
    public SqlSegmentStorage sqlSegmentStorage(
            @Value("${storage.generator.segment.name:segment}") String tableName,
            @Value("${storage.devOps.maxQueryTimeMs:3000}") long maxQueryTimeMs) {

        SqlSegmentStorage storage = new SqlSegmentStorage();
        storage.setQueryTimeout(maxQueryTimeMs);
        storage.setTable(tableName);
        return storage;
    }

    /**
     * parser manager
     * @return
     */
    @Bean
    public PattenParserManager pattenParserManager() {
        PattenParserManager pattenParserManager = new PattenParserManager();
        return pattenParserManager;
    }
}
