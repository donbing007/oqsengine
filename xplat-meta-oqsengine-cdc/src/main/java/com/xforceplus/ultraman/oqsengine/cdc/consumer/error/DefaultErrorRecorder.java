package com.xforceplus.ultraman.oqsengine.cdc.consumer.error;

import static com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType.DATA_FORMAT_ERROR;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_ID;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_OP;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_VERSION;

import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.CdcErrorStorage;
import com.xforceplus.ultraman.oqsengine.cdc.consumer.dto.ParseResult;
import com.xforceplus.ultraman.oqsengine.common.id.LongIdGenerator;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 02/2022.
 *
 * @since 1.8
 */
public class DefaultErrorRecorder implements ErrorRecorder {

    final Logger logger = LoggerFactory.getLogger(DefaultErrorRecorder.class);

    final static int MAX_MESSAGE_LENGTH = 500;

    @Resource
    private CdcErrorStorage cdcErrorStorage;

    @Resource(name = "longNoContinuousPartialOrderIdGenerator")
    private LongIdGenerator seqNoGenerator;

    @Override
    public void record(long batchId, Map<String, ParseResult.Error> errors) {

        List<String> keys = new ArrayList<>(errors.keySet());

        List<CdcErrorTask> toInsertErrors = new ArrayList<>();
        try {
            //  查询已存在的记录
            Collection<CdcErrorTask> errorTasks = cdcErrorStorage.queryCdcErrors(keys);

            //  将已存在的去重
            if (!errorTasks.isEmpty()) {
                for (CdcErrorTask cdcErrorTask : errorTasks) {
                    errors.remove(cdcErrorTask.getUniKey());
                }
            }

            //  新建错误任务
            errors.forEach(
                (key, value) -> {
                    toInsertErrors.add(
                        CdcErrorTask
                            .buildErrorTask(seqNoGenerator.next(), key, batchId, value.getId(),
                                UN_KNOW_ID, UN_KNOW_VERSION, UN_KNOW_OP, value.getCommitId(),
                                DATA_FORMAT_ERROR.getType(),
                                "{}",
                                (null == value.getMessage()) ? "unKnow error." :
                                    ((value.getMessage().length() <= MAX_MESSAGE_LENGTH) ?
                                        value.getMessage() : value.getMessage().substring(0, MAX_MESSAGE_LENGTH)))
                    );
                }
            );

            //  批量写入
            cdcErrorStorage.batchInsert(toInsertErrors);
        } catch (Exception e) {
            logger.error("record-error failed, batchId : {}", e.getMessage());
        }
    }
}
