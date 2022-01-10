package com.xforceplus.ultraman.oqsengine.idgenerator.service.impl;

import com.alibaba.google.common.cache.CacheBuilder;
import com.alibaba.google.common.cache.CacheLoader;
import com.alibaba.google.common.cache.LoadingCache;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.Constants;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.constant.IDModel;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.PatternValue;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentId;
import com.xforceplus.ultraman.oqsengine.idgenerator.common.entity.SegmentInfo;
import com.xforceplus.ultraman.oqsengine.idgenerator.exception.IDGeneratorException;
import com.xforceplus.ultraman.oqsengine.idgenerator.parser.PatternParserUtil;
import com.xforceplus.ultraman.oqsengine.idgenerator.service.SegmentService;
import com.xforceplus.ultraman.oqsengine.idgenerator.storage.SqlSegmentStorage;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明:
 * 作者(@author): liwei
 * 创建时间: 5/8/21 10:04 PM
 */

public class SegmentServiceImpl implements SegmentService {

    @Resource
    SqlSegmentStorage sqlSegmentStorage;


    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentServiceImpl.class);

    private static final Long MAX_CACHE_TIME_IN_SECONDS = 600L;

    private static final Long MAX_SIZE = 10000L;

    protected LoadingCache<String, SegmentInfo> cache = CacheBuilder.newBuilder().maximumSize(MAX_SIZE)
        .expireAfterWrite(MAX_CACHE_TIME_IN_SECONDS, TimeUnit.SECONDS)
        .build(new CacheLoader<String, SegmentInfo>() {
            @Override
            public SegmentInfo load(String key) throws SQLException {
                return innerGetSegmentInfo(key);
            }
        });


    @Override
    public SegmentId getNextSegmentId(String bizType) throws SQLException {
        // 获取nextTinyId的时候，有可能存在version冲突，需要重试
        for (int i = 0; i < Constants.RETRY; i++) {
            SegmentInfo segmentInfo = innerGetSegmentInfo(bizType);
            Long newMaxId = segmentInfo.getMaxId() + segmentInfo.getStep();
            int row = sqlSegmentStorage.udpate(segmentInfo);
            if (row == 1) {
                segmentInfo.setMaxId(newMaxId);
                SegmentId segmentId = convert(segmentInfo);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getNextSegmentId success segmentInfo:{} current:{}", segmentInfo, segmentId);
                }
                return segmentId;
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getNextSegmentId conflict segmentInfo:{}", segmentInfo);
                }
            }
        }
        throw new IDGeneratorException("get next segment conflict");
    }

    @Override
    public boolean resetSegment(String bizType, String patternKey) throws SQLException {
        SegmentInfo segmentInfo = innerGetSegmentInfo(bizType);
        segmentInfo.setPatternKey(patternKey);
        int row = sqlSegmentStorage.reset(segmentInfo);
        return row > 0;
    }

    private SegmentInfo innerGetSegmentInfo(String bizType) {
        Optional<SegmentInfo> targetSegmentInfo = null;
        try {
            targetSegmentInfo = sqlSegmentStorage.query(bizType);
        } catch (SQLException e) {
            throw new IDGeneratorException("query bizType failed!", e);
        }
        if (!targetSegmentInfo.isPresent()) {
            throw new IDGeneratorException("Can not find bizType:" + bizType);
        }
        return targetSegmentInfo.get();
    }

    @Override
    public SegmentInfo getSegmentInfo(String bizType) {
        try {
            return cache.get(bizType);
        } catch (ExecutionException e) {
            String errorMsg = String.format("Get segment : %s failed!", bizType);
            LOGGER.error(errorMsg, e);
            throw new IDGeneratorException(errorMsg);
        }
    }


    @Override
    public IDModel getIDModel(String bizType) {
        IDModel model = null;
        try {
            SegmentInfo segmentInfo = cache.get(bizType);
            model = getIDModel(segmentInfo);
        } catch (CacheLoader.InvalidCacheLoadException e) {
            LOGGER.warn(String.format("Get invalidCache : %s", bizType));
        } catch (Exception e) {
            String errorMsg = String.format("Get IdModel : %s failed!", bizType);
            LOGGER.error(errorMsg, e);
            throw new IDGeneratorException(errorMsg);
        }
        return model;
    }


    private IDModel getIDModel(SegmentInfo segmentInfo) {
        return IDModel.fromValue(segmentInfo.getMode());
    }

    /**
     * convert segmentInfo to segmentId.
     *
     * @param idInfo segmentInfo entity
     * @return SegmentId
     */
    public SegmentId convert(SegmentInfo idInfo) {
        SegmentId segmentId = new SegmentId();
        long id = idInfo.getMaxId() - idInfo.getStep();
        String value = PatternParserUtil.getInstance().parse(idInfo.getPattern(), id);
        PatternValue pattenValue = new PatternValue(id, value);
        segmentId.setCurrentId(pattenValue);
        segmentId.setMaxId(idInfo.getMaxId());
        segmentId.setPattern(idInfo.getPattern());
        segmentId.setResetable(idInfo.getResetable());
        // 默认30%加载
        segmentId.setLoadingId(segmentId.getCurrentId().getId() + idInfo.getStep() * Constants.LOADING_PERCENT / 100);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Loading Id set to {}", segmentId.getLoadingId());
        }
        return segmentId;
    }
}
