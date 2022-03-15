package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.helper.ParseResultCheckHelper;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.DynamicCanalEntryGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.DynamicCanalEntryRepo;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OqsEngineEntity;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class DynamicBinLogParserTest extends AbstractCdcHelper {

    static DynamicCanalEntryCase[] expected = { DynamicCanalEntryRepo.CASE_NORMAL_2, DynamicCanalEntryRepo.CASE_MAINTAIN };
    static ParserContext parserContext;

    @BeforeEach
    public void before() throws Exception {
        parserContext = new ParserContext(-1, true, new CDCMetrics(), MetaInitialization.getInstance().getMetaManager());
        super.init(false, null);
    }

    @AfterEach
    public void after() throws Exception {
        parserContext = null;
        super.clear(false);
    }

    @AfterAll
    public static void afterAll() {
        try {
            InitializationHelper.destroy();
        } catch (Exception e) {

        }
    }

    @Test
    public void parseTest() throws InvalidProtocolBufferException, JsonProcessingException {

        DynamicBinLogParser dynamicBinLogParser = new DynamicBinLogParser();
        ParseResult parseResult = new ParseResult();

        for (DynamicCanalEntryCase entryCase : expected) {
            CanalEntry.Entry entry = DynamicCanalEntryGenerator.buildRowDataEntry(entryCase);
            CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            List<CanalEntry.Column> columns = rowChange.getRowDatasList().get(0).getAfterColumnsList();

            dynamicBinLogParser.parse(columns, parserContext, parseResult);
        }

        //  check commitId size.
        Assertions.assertEquals(
            Arrays.stream(expected).map(DynamicCanalEntryCase::getCommitId).collect(Collectors.toSet()).size(), parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());

        for (int i = 0; i < expected.length; i++) {
            Assertions.assertTrue(parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().contains(expected[i].getCommitId()));

            OqsEngineEntity oqsEngineEntity =
                parseResult.getFinishEntries().get(expected[i].getId());
            Assertions.assertNotNull(oqsEngineEntity);
            ParseResultCheckHelper.dynamicCheck(expected[i], oqsEngineEntity);
        }
    }

}
