package com.xforceplus.ultraman.oqsengine.calculation.listener;

import com.xforceplus.ultraman.oqsengine.event.ActualEvent;
import com.xforceplus.ultraman.oqsengine.event.EventBus;
import com.xforceplus.ultraman.oqsengine.event.EventType;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AggregationTreePayload;
import com.xforceplus.ultraman.oqsengine.event.payload.calculator.AutoFillUpgradePayload;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SegmentStorage;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.calculation.AutoFill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * 监听元数据初始化构建聚合计算书.
 *
 * @className: AggregationTreeBuilderListener
 * @author: wangzheng
 * @date: 2021/8/30 14:57
 */
public class AggregationTreeBuilderListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AggregationTreeBuilderListener.class);

    @Resource
    private EventBus eventBus;

    @PostConstruct
    public void init() {
        eventBus.watch(EventType.AGGREGATION_TREE_UPGRADE, event -> {
            try {
                this.handleAggregationTreeUpgrade((ActualEvent) event);
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
    public void handleAggregationTreeUpgrade(ActualEvent event) throws SQLException {
        LOGGER.info("Receive event :{}", event);
        if (event.payload().isPresent()) {
            AggregationTreePayload payload = (AggregationTreePayload) event.payload().get();

        }
    }
}
