package com.xforceplus.ultraman.oqsengine.idgenerator.config;

import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourceFactory;
import com.xforceplus.ultraman.oqsengine.common.datasource.DataSourcePackage;
import com.xforceplus.ultraman.oqsengine.event.DefaultEventBus;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.storage.EventStorage;
import com.xforceplus.ultraman.oqsengine.event.storage.MemoryEventStorage;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import java.sql.Connection;
import java.sql.Statement;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * BusinessIDGeneratorConfiguration.
 *
 * @author leo
 * @version 0.1 2021-05-13
 * @since 1.8
 */
@Configuration
public class BusinessIDGeneratorConfiguration {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 主库存连接池.
     */
    @Bean
    public DataSource segmentDataSource() throws Exception {
        System.setProperty(
            "MYSQL_JDBC_ID",
            String.format(
                "jdbc:mysql://%s:%s/oqsengine?useUnicode=true&serverTimezone=GMT&useSSL=false&characterEncoding=utf8",
                System.getProperty("MYSQL_HOST"), System.getProperty("MYSQL_PORT")));


        return buildDataSource("./src/test/resources/generator.conf");
    }

    private DataSource buildDataSource(String file) {
        System.setProperty(DataSourceFactory.CONFIG_FILE, file);
        DataSourcePackage dataSourcePackage = DataSourceFactory.build(true);

        return dataSourcePackage.getMaster().get(0);
    }

    /**
     * segment storage.
     *
     * @param tableName      表名.
     * @param maxQueryTimeMs 超时时间 .
     * @return SqlSegmentStorage .
     */
    @Bean
    public SqlSegmentStorage sqlSegmentStorage(
        @Value("${storage.generator.segment.name:segment}") String tableName,
        @Value("${storage.devOps.maxQueryTimeMs:3000}") long maxQueryTimeMs) throws Exception {

        SqlSegmentStorage storage = new SqlSegmentStorage();
        storage.setQueryTimeout(maxQueryTimeMs);
        storage.setTable(tableName);
        return storage;
    }

    /**
     * parser manager.
     *
     * @return PattenParserManager
     */
    @Bean
    public PatternParserManager pattenParserManager() {
        PatternParserManager patternParserManager = new PatternParserManager();
        return patternParserManager;
    }

    /**
     * pattenParserUtil.
     *
     * @return PattenParserUtil
     */
    @Bean
    public PatternParserUtil pattenParserUtil() {
        PatternParserUtil patternParserUtil = new PatternParserUtil();
        return patternParserUtil;
    }

    /**
     * bizIDGenerator.
     *
     * @return BizIDGenerator
     */
    @Bean
    public BizIDGenerator bizIDGenerator() {
        BizIDGenerator bizIDGenerator = new BizIDGenerator();
        return bizIDGenerator;
    }

    /**
     * idGeneratorFactory.
     *
     * @return IDGeneratorFactory
     */
    @Bean
    public IDGeneratorFactory idGeneratorFactory() {
        IDGeneratorFactory factory = new IDGeneratorFactoryImpl();
        return factory;
    }

    /**
     * segmentService.
     *
     * @return SegmentService
     */
    @Bean
    public SegmentService segmentService() {
        SegmentService segmentService = new SegmentServiceImpl();
        return segmentService;
    }

    /**
     * redis client.
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisIp = System.getProperty("REDIS_HOST");
        int redisPort = Integer.parseInt(System.getProperty("REDIS_PORT"));
        config.useSingleServer().setAddress(String.format("redis://%s:%s", redisIp, redisPort));
        return Redisson.create(config);
    }

    @Bean("eventBus")
    public EventBus eventBus(EventStorage eventStorage) {
        return new DefaultEventBus(eventStorage, Executors.newFixedThreadPool(5));
    }

    @Bean("eventStorage")
    public EventStorage eventStorage() {
        return new MemoryEventStorage();
    }

    /**
     * datePattenParser.
     *
     * @return datePattenParser
     */
    @Bean("datePattenParser")
    public PatternParser datePattenParser() {
        return new DatePatternParser();
    }

    /**
     * numberPattenParser.
     *
     * @return numberPattenParser
     */
    @Bean("numberPattenParser")
    public PatternParser numberPattenParser() {
        return new NumberPatternParser();
    }
}
