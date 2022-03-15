package com.xforceplus.ultraman.oqsengine.cdc.consumer.error;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ErrorCase;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.cdc.mock.CdcInitialization;
import com.xforceplus.ultraman.oqsengine.cdc.testhelp.AbstractCdcHelper;
import com.xforceplus.ultraman.oqsengine.common.mock.InitializationHelper;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import java.util.Collection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 03/2022.
 *
 * @since 1.8
 */
public class DefaultErrorRecorderTest extends AbstractCdcHelper {

    @BeforeEach
    public void before() throws Exception {
        super.init(false, null);
    }

    @AfterEach
    public void after() throws Exception {
        super.clear(false);
    }

    @AfterAll
    public static void afterAll() {
        try {
            InitializationHelper.destroy();
        } catch (Exception e) {

        }
    }

    private static final long expected_batchId = 1001;
    private static final long unexpected_batchId = 1002;

    @Test
    public void recordTest() throws Exception {

        ParseResult parseResult = new ParseResult();

        ErrorCase.errorCases.forEach(
            e -> {
                parseResult.addError(e._1(), e._2(), e._3());
            }
        );

        CdcInitialization.getInstance().getErrorRecorder().record(expected_batchId, parseResult.getErrors());

        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(expected_batchId);

        CdcErrorStorage cdcErrorStorage = CdcInitialization.getInstance().getCdcErrorStorage();
        Collection<CdcErrorTask> cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);

        Assertions.assertEquals(parseResult.getErrors().size(), cdcErrorTaskList.size());

        cdcErrorTaskList.forEach(
            cdcErrorTask -> {
                ParseResult.Error error =
                    parseResult.getErrors().remove(cdcErrorTask.getUniKey());
                Assertions.assertNotNull(error);

                check(error, cdcErrorTask);
            }
        );

        cdcErrorQueryCondition = new CdcErrorQueryCondition();
        cdcErrorQueryCondition.setBatchId(unexpected_batchId);

        cdcErrorTaskList = cdcErrorStorage.queryCdcErrors(cdcErrorQueryCondition);
        Assertions.assertTrue(cdcErrorTaskList.isEmpty());
    }



    private void check(ParseResult.Error error, CdcErrorTask cdcErrorTask) {
        Assertions.assertEquals(error.keyGenerate(), cdcErrorTask.getUniKey());
        Assertions.assertEquals(error.getId(), cdcErrorTask.getId());
        Assertions.assertEquals(error.getCommitId(), cdcErrorTask.getCommitId());
        Assertions.assertEquals(error.getMessage(), cdcErrorTask.getMessage());
    }


}
