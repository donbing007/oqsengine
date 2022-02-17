package com.xforceplus.ultraman.oqsengine.cdc.cdcerrors.condition;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class CdcErrorQueryConditionTest {

    private Long seqNo = 1001L;
    private String uniKey = "uniKey";
    private Long batchId = 10L;
    private Long id = 10001L;
    private Long entity = 20002L;
    private Long commitId = 2L;
    private Integer status = 1;
    private Boolean isEqualStatus = true;
    private Integer type = 1;
    private Long rangeLeExecuteTime = 1L;
    private Long rangeGeExecuteTime = 2L;
    private Long rangeLeFixedTime = 3L;
    private Long rangeGeFixedTime = 4L;

    @Test
    public void conditionTest() {
        CdcErrorQueryCondition condition = toCondition();

        Assertions.assertEquals(
            "seqno=? AND unikey=? AND batchid=? AND id=? AND entity=? AND commitid=? AND type=? AND status=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        isEqualStatus = false;
        condition = toCondition();
        Assertions.assertEquals(
            "seqno=? AND unikey=? AND batchid=? AND id=? AND entity=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        seqNo = null;
        condition = toCondition();
        Assertions.assertEquals(
            "unikey=? AND batchid=? AND id=? AND entity=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        uniKey = null;
        condition = toCondition();
        Assertions.assertEquals(
            "batchid=? AND id=? AND entity=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        batchId = null;
        condition = toCondition();
        Assertions.assertEquals(
            "id=? AND entity=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        id = null;
        condition = toCondition();
        Assertions.assertEquals(
            "entity=? AND commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        entity = null;
        condition = toCondition();
        Assertions.assertEquals(
            "commitid=? AND type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        commitId = null;
        condition = toCondition();
        Assertions.assertEquals(
            "type=? AND status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        type = null;
        condition = toCondition();
        Assertions.assertEquals(
            "status!=? AND executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        status = null;
        condition = toCondition();
        Assertions.assertEquals(
            "executetime<=? AND executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );


        rangeLeExecuteTime = null;
        condition = toCondition();
        Assertions.assertEquals(
            "executetime>=? AND fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        rangeGeExecuteTime = null;
        condition = toCondition();
        Assertions.assertEquals(
            "fixedtime<=? AND fixedtime>=?",
            condition.conditionToQuerySql()
        );

        rangeLeFixedTime = null;
        condition = toCondition();
        Assertions.assertEquals(
            "fixedtime>=?",
            condition.conditionToQuerySql()
        );

        rangeGeFixedTime = null;
        condition = toCondition();
        Assertions.assertEquals("", condition.conditionToQuerySql());
    }


    private CdcErrorQueryCondition toCondition() {
        CdcErrorQueryCondition condition = new CdcErrorQueryCondition();
        condition.setSeqNo(seqNo);
        condition.setUniKey(uniKey);
        condition.setBatchId(batchId);
        condition.setId(id);
        condition.setEntity(entity);
        condition.setCommitId(commitId);
        condition.setStatus(status);
        condition.setEqualStatus(isEqualStatus);
        condition.setType(type);
        condition.setRangeLeExecuteTime(rangeLeExecuteTime);
        condition.setRangeGeExecuteTime(rangeGeExecuteTime);
        condition.setRangeLeFixedTime(rangeLeFixedTime);
        condition.setRangeGeFixedTime(rangeGeFixedTime);

        return condition;
    }
}
