package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import akka.NotUsed;
import akka.grpc.javadsl.Metadata;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.IDevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityHelper.toEntityClass;

/**
 * rebuild oqs
 */
@Component
public class EntityRebuildServiceOqs implements EntityRebuildServicePowerApi {

    @Autowired
    private DevOpsManagementService devOpsManagementService;

    @Resource(name = "callRebuildThreadPool")
    private ExecutorService asyncDispatcher;

    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private static RebuildTaskInfo empty = RebuildTaskInfo.newBuilder().setTid(0).buildPartial();

    private Logger logger = LoggerFactory.getLogger(EntityRebuildServiceOqs.class);

    private <T> CompletableFuture<T> async(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, asyncDispatcher);
    }

    @Override
    public CompletionStage<RebuildTaskInfo> rebuildIndex(RebuildRequest in, Metadata metadata) {
        return async(() -> {
            EntityUp entityUp = in.getEntity();

            IEntityClass entityClass = toEntityClass(entityUp);

            try {
                Optional<IDevOpsTaskInfo> iDevOpsTaskInfo = devOpsManagementService.rebuildIndex(
                        entityClass
                        , LocalDateTime.parse(in.getStart(), dateTimeFormatter)
                        , LocalDateTime.parse(in.getEnd(), dateTimeFormatter));

                return iDevOpsTaskInfo.map(this::toTaskInfo).orElse(empty);
            } catch (Exception e) {
                logger.error("{}", e);
                return empty;
            }
        });
    }

    @Override
    public Source<RebuildTaskInfo, NotUsed> showProgress(ShowTask in, Metadata metadata) {
        return Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(3), in.getId())
                .map(i -> devOpsManagementService.SyncTask(Long.toString(i)))
                .takeWhile(x -> x.isPresent())
                .map(x -> toTaskInfo(x.get()))
                .mapMaterializedValue(x -> NotUsed.getInstance());
    }

    /**
     * list all active tasks
     * @param in
     * @param metadata
     * @return
     */
    @Override
    public Source<RebuildTaskInfo, NotUsed> listActiveTasks(QueryPage in, Metadata metadata) {
        try {
            return Source.from(devOpsManagementService.listActiveTasks(new Page(in.getNumber(), in.getSize()))).map(this::toTaskInfo);
        } catch (SQLException e) {
            return Source.single(RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial());
        }
    }

    /**
     *
     * @param in
     * @param metadata
     * @return
     */
    @Override
    public CompletionStage<RebuildTaskInfo> getActiveTask(EntityUp in, Metadata metadata) {
        return async(() -> {
            try {
                return devOpsManagementService.getActiveTask(toEntityClass(in)).map(this::toTaskInfo).orElse(empty);
            } catch (SQLException e) {
                logger.error("{}", e);
                return RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial();
            }
        });
    }

    @Override
    public Source<RebuildTaskInfo, NotUsed> listAllTasks(QueryPage in, Metadata metadata) {
        try {
            return Source.from(devOpsManagementService.listAllTasks(new Page(in.getNumber(), in.getSize()))).map(this::toTaskInfo);
        } catch (SQLException e) {
            logger.error("{}", e);
            return Source.single(RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial());
        }
    }

    @Override
    public CompletionStage<RebuildTaskInfo> cancelTask(ShowTask in, Metadata metadata) {
        return async(() -> {
            try {
                devOpsManagementService.cancel(Long.toString(in.getId()));
                return RebuildTaskInfo.newBuilder().setErrCode("1").setMessage("ok").buildPartial();
            } catch (SQLException e) {
                logger.error("{}", e);
                return RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial();
            }
        });
    }

    RebuildTaskInfo toTaskInfo(IDevOpsTaskInfo taskInfo){
        return RebuildTaskInfo.newBuilder()
                .setTid(Long.parseLong(taskInfo.id()))
                .setIsCancel(taskInfo.isCancel())
                .setIsDone(taskInfo.isDone())
                .setStatus(taskInfo.status().name())
                .setBatchSize(taskInfo.getBatchSize())
                .setEntityId(taskInfo.getEntity())
                .setFinishSize(taskInfo.getFinishSize())
                .setPercentage(taskInfo.getProgressPercentage())
                .setStarts(taskInfo.getStarts())
                .setEnds(taskInfo.getEnds())
                .build();
    }
}