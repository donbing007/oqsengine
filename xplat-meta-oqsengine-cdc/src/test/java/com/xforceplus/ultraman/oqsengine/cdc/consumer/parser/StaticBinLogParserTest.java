package com.xforceplus.ultraman.oqsengine.cdc.consumer.parser;

import static com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.StaticCanalEntryRepo.CASE_STATIC_ALL_IN_ONE;
import static com.xforceplus.ultraman.oqsengine.cdc.testhelp.repo.StaticCanalEntryRepo.CASE_STATIC_MAINTAIN;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.parser.helper.ParseResultCheckHelper;
import com.xforceplus.ultraman.oqsengine.cdc.context.ParserContext;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.DynamicCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.cases.StaticCanalEntryCase;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.DynamicCanalEntryGenerator;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.generator.StaticCanalEntryGenerator;
import com.xforceplus.ultraman.oqsengine.metadata.mock.MetaInitialization;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.metrics.CDCMetrics;
import com.xforceplus.ultraman.oqsengine.storage.master.mock.MasterDBInitialization;
import com.xforceplus.ultraman.oqsengine.storage.pojo.OriginalEntity;
import io.vavr.Tuple2;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class StaticBinLogParserTest extends AbstractCdcHelper {

    static List<Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase>> expected =
        Arrays.asList(CASE_STATIC_ALL_IN_ONE, CASE_STATIC_MAINTAIN);

    static ParserContext parserContext;
    static ParseResult parseResult;

    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);

        parserContext = new ParserContext(-1, true, new CDCMetrics(), MetaInitialization.getInstance().getMetaManager(),
            MasterDBInitialization.getInstance().getMasterStorage());

        parseResult = new ParseResult();
    }

    @AfterEach
    public void after() throws Exception {
        parserContext = null;
        parseResult = null;
        super.clear(false);
    }

    @Test
    public void testParse() throws InvalidProtocolBufferException {
        for (Tuple2<DynamicCanalEntryCase, StaticCanalEntryCase> dys : expected) {
            Assertions.assertTrue(initDynamic(dys._1()));
            Assertions.assertTrue(parse(dys._2()));

            OriginalEntity entity =
                parseResult.getFinishEntries().get(dys._1().getId());

            ParseResultCheckHelper.staticCheck(dys, entity, true);
        }

        Assertions.assertEquals(2, parseResult.getFinishEntries().size());
        Assertions.assertEquals(0, parseResult.getOperationEntries().size());
        Assertions.assertEquals(4, parseResult.getPos());
        Assertions.assertEquals(expected.get(1)._1().getId(), parseResult.getLastId());

        Assertions.assertEquals(2, parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds().size());

        int i = 0;
        for (Long commitId : parserContext.getCdcMetrics().getCdcUnCommitMetrics().getUnCommitIds()) {
            Assertions.assertEquals(expected.get(i)._1().getCommitId(), commitId);
            i++;
        }
    }

    protected boolean parse(StaticCanalEntryCase entryCase) throws InvalidProtocolBufferException {
        StaticBinLogParser staticBinLogParser = new StaticBinLogParser();

        CanalEntry.Entry entry = StaticCanalEntryGenerator.buildRowDataEntry(entryCase);
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        List<CanalEntry.Column> columns = rowChange.getRowDatasList().get(0).getAfterColumnsList();

        staticBinLogParser.parse(columns, parserContext, parseResult);
        return true;
    }

    private boolean initDynamic(DynamicCanalEntryCase entryCase) throws InvalidProtocolBufferException {
        DynamicBinLogParser dynamicBinLogParser = new DynamicBinLogParser();

        CanalEntry.Entry entry = DynamicCanalEntryGenerator.buildRowDataEntry(entryCase);
        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        List<CanalEntry.Column> columns = rowChange.getRowDatasList().get(0).getAfterColumnsList();

        dynamicBinLogParser.parse(columns, parserContext, parseResult);

        return true;
    }
}
