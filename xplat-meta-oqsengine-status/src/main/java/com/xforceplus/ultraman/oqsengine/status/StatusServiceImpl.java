package com.xforceplus.ultraman.oqsengine.status;

import com.xforceplus.ultraman.oqsengine.status.id.RedisIdGenerator;
import com.xforceplus.ultraman.oqsengine.status.table.TimeTable;
import io.lettuce.core.ScoredValue;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * status service implementation
 */
public class StatusServiceImpl implements StatusService {

    private RedisIdGenerator redisIdGenerator;

    private TimeTable timeTable;

    private Long timeBuff = 10L;


    public StatusServiceImpl(RedisIdGenerator redisIdGenerator, TimeTable timeTable) {
        this.redisIdGenerator = redisIdGenerator;
        this.timeTable = timeTable;
    }

    @Override
    public Long getCommitId() {
        return redisIdGenerator.next();
    }

    @Override
    public void saveCommitId(Long id) {
        timeTable.insertWithRemoteTime(id.toString()).block();
    }

    @Override
    public void saveCommitIdWithLocalTime(Long id, Long timeInMilli) {
        timeTable.insertWithLocalTime(id.toString(), timeInMilli).block();
    }

    @Override
    public void invalidateIds(List<Long> ids) {
        timeTable.invalidateIds(ids);
    }

    /**
     * @param windowsTimeRange
     * @return will return -1
     */
    @Override
    public Long getCurrentCommitLowBound(Long windowsTimeRange) {
        //10 is the fixed buff
        return timeTable.queryByWindow(windowsTimeRange, timeBuff).toStream().min(Long::compareTo)
            .orElse(-1L);
    }

    /**
     * @param
     * @return will return -1
     */
    @Override
    public Long getCurrentCommitLowBoundWithLocalTime(Long start, Long end) {
        return timeTable.queryByLocalTime(start, end).toStream().min(Long::compareTo)
            .orElse(-1L);
    }

    @Override
    public StatusMetrics getCurrentStatusMetrics() {
        Flux<ScoredValue<String>> scoredValueFlux = timeTable.queryAllWithScore();

        /**
         * scored is in order
         */
        List<ScoredValue<String>> snapshotList = scoredValueFlux.toStream().collect(Collectors.toList());

        StatusMetrics statusMetrics = new StatusMetrics();

        if (!snapshotList.isEmpty()) {
            ScoredValue<String> first = snapshotList.get(0);
            ScoredValue<String> last = snapshotList.get(snapshotList.size() - 1);

            double firstScore = first.getScore();
            double lastScore = last.getScore();

            BigDecimal firstB = new BigDecimal(firstScore);
            BigDecimal lastB = new BigDecimal(lastScore);

            statusMetrics.setLowBound(firstB.toPlainString());
            statusMetrics.setUpBound(lastB.toPlainString());
        }

        statusMetrics.setSize((long) snapshotList.size());
        statusMetrics.setTransIds(snapshotList.stream()
            .map(x -> Long.parseLong(x.getValue())).collect(Collectors.toList()));

        return statusMetrics;
    }
}