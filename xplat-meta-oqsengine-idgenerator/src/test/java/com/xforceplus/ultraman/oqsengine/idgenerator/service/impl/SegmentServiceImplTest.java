package com.xforceplus.ultraman.oqsengine.idgenerator.service.impl;

import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.config.BusinessIDGeneratorConfiguration;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParser;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserManager;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import com.xforceplus.ultraman.test.tools.core.container.basic.MysqlContainer;
import com.xforceplus.ultraman.test.tools.core.container.basic.RedisContainer;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.annotation.Resource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/20/21 11:26 AM
 * @since 1.8
 */

@ExtendWith({RedisContainer.class, MysqlContainer.class, SpringExtension.class})
@SpringBootTest(classes = BusinessIDGeneratorConfiguration.class)
public class SegmentServiceImplTest {

    @Resource
    private SqlSegmentStorage storage;

    @Resource
    private SegmentServiceImpl service;

    @Resource
    private PatternParserManager manager;

    @Resource
    private PatternParserUtil patternParserUtil;

    @Resource(name = "datePattenParser")
    private PatternParser datePattenParser;

    @Resource(name = "numberPattenParser")
    private PatternParser numberPattenParser;

    SegmentInfo info = SegmentInfo.builder().withBeginId(1l).withBizType("testBiz")
        .withCreateTime(new Timestamp(System.currentTimeMillis()))
        .withMaxId(0l)
        .withPatten("{yyyy}-{MM}-{dd}-{000}")
        .withMode(2)
        .withStep(1000)
        .withUpdateTime(new Timestamp(System.currentTimeMillis()))
        .withVersion(1l)
        .withResetable(0)
        .withPatternKey("")
        .build();

    @BeforeEach
    public void before() throws Exception {

        storage.build(info);
        Thread.sleep(1_000);
    }

    @AfterEach
    public void afterEach() throws Exception {
        storage.delete(info);
    }

    @Test
    public void testGetNextSegmentId() throws SQLException {
        LocalDateTime localDateTime = LocalDateTime.now();
        String ext = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        SegmentId segmentId = service.getNextSegmentId("testBiz");
        String actual = segmentId.getCurrentId().getValue();
        Assertions.assertEquals(ext + "-000", actual);
    }
}
