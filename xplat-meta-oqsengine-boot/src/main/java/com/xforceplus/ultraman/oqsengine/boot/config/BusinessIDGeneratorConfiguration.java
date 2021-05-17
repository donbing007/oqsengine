package com.xforceplus.ultraman.oqsengine.boot.config;

import com.xforceplus.ultraman.oqsengine.idgenerator.client.BizIDGenerator;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactory;
import com.xforceplus.ultraman.oqsengine.idgenerator.generator.IDGeneratorFactoryImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.listener.AutoFillUpgradeListener;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PattenParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.impl.SegmentServiceImpl;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import org.checkerframework.checker.units.qual.A;
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
    public PattenParserManager pattenParserManager() {
        PattenParserManager pattenParserManager = new PattenParserManager();
        return pattenParserManager;
    }

    /**
     * pattenParserUtil.
     *
     * @return PattenParserUtil
     */
    @Bean
    public PattenParserUtil pattenParserUtil() {
        PattenParserUtil pattenParserUtil = new PattenParserUtil();
        return pattenParserUtil;
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
}
