package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.boot.config.redis.LettuceConfiguration;
import com.xforceplus.ultraman.oqsengine.boot.util.RedisConfigUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.listener.AutoFillUpgradeListener;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.DatePatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.impl.NumberPatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
     * segment storage.
     *
     * @param tableName      表名.
     * @param maxQueryTimeMs 超时时间 .
     * @return SqlSegmentStorage .
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
     * autoFillUpgradeListener.
     *
     * @return AutoFillUpgradeListener
     */
    @Bean
    public AutoFillUpgradeListener autoFillUpgradeListener() {
        AutoFillUpgradeListener autoFillUpgradeListener = new AutoFillUpgradeListener();
        return autoFillUpgradeListener;
    }

    /**
     * datePattenParser.
     *
     * @return datePattenParser
     */
    @Bean
    public PatternParser datePattenParser() {
        return new DatePatternParser();
    }

    /**
     * numberPattenParser.
     *
     * @return numberPattenParser
     */
    @Bean
    public PatternParser numberPattenParser() {
        return new NumberPatternParser();
    }

    /**
     * redissonClient.
     *
     * @param lettuceConfiguration lettuceConfiguration
     * @return RedissonClient
     */
    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient(LettuceConfiguration lettuceConfiguration) {
        Config config = new Config();
        config.useSingleServer()
            .setAddress(lettuceConfiguration.uriWithIDGenerator());
        String url = lettuceConfiguration.uriWithIDGenerator();
        String password = RedisConfigUtil.getRedisUrlPassword(url);
        logger.info("Business redis password{}", password);
        if (!StringUtils.isBlank(password)) {
            if (password.startsWith(":")) {
                password = password.substring(1);
            }
            config.useSingleServer().setPassword(password);
        }
        return Redisson.create(config);
    }

}
