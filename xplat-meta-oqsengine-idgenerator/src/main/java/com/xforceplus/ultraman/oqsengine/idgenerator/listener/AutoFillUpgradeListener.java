package com.xforceplus.ultraman.oqsengine.idgenerator.listener;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AutoFillUpgradePayload;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * .
 *
 * @author leo
 * @version 0.1 5/13/21 10:42 PM
 * @since 1.8
 */
public class AutoFillUpgradeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AutoFillUpgradeListener.class);

    @Resource
    private EventBus eventBus;

    @Resource
    private SegmentStorage storage;

    @PostConstruct
    public void init() {
        eventBus.watch(EventType.AUTO_FILL_UPGRADE, event -> {
            try {
                this.handleAutoFillUpgrade((ActualEvent) event);
            } catch (SQLException e) {
                throw new IDGeneratorException("Failed to operate the segment !", e);
            }
        });
    }

    /**
     * handel the event.
     *
     * @param event ActualEvent
     */
    public void handleAutoFillUpgrade(ActualEvent event) throws SQLException {
        LOGGER.info("Receive event :{}", event);
        if (event.payload().isPresent()) {
            AutoFillUpgradePayload payload = (AutoFillUpgradePayload) event.payload().get();
            String bizType = String.valueOf(payload.getEntityField().id());
            Optional<SegmentInfo> segmentInfo = storage.query(bizType);
            Calculator calculator = payload.getEntityField().calculator();
            if (!segmentInfo.isPresent()) {
                SegmentInfo info = SegmentInfo.builder().withVersion(0L)
                    .withCreateTime(new Timestamp(System.currentTimeMillis()))
                    .withUpdateTime(new Timestamp(System.currentTimeMillis()))
                    .withStep(calculator.getStep())
                    .withPatten(calculator.getPatten())
                    .withMode(Integer.valueOf(calculator.getModel()))
                    .withMaxId(0L).withBizType(bizType)
                    .withPatternKey("").withResetable(0)
                    .withBeginId(1L).build();
                storage.build(info);
            } else {
                SegmentInfo info = segmentInfo.get();
                info.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                info.setStep(calculator.getStep());
                info.setPattern(calculator.getPatten());
                info.setMode(Integer.valueOf(calculator.getModel()));
                storage.udpate(info);
            }
        }
    }
}
