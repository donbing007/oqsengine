package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import static com.xforceplus.ultraman.oqsengine.boot.grpc.utils.EntityHelper.toEntityClass;
import static com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant.UN_KNOW_ID;

import akka.NotUsed;
import akka.grpc.javadsl.Metadata;
import akka.stream.javadsl.Source;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.condition.CdcErrorQueryCondition;
import com.xforceplus.ultraman.oqsengine.cdc.cdcerror.dto.ErrorType;
import com.xforceplus.ultraman.oqsengine.core.service.DevOpsManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntityManagementService;
import com.xforceplus.ultraman.oqsengine.core.service.EntitySearchService;
import com.xforceplus.ultraman.oqsengine.devops.rebuild.model.DevOpsTaskInfo;
import com.xforceplus.ultraman.oqsengine.pojo.cdc.constant.CDCConstant;
import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.devops.CdcErrorTask;
import com.xforceplus.ultraman.oqsengine.pojo.devops.FixedStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.impl.Entity;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.sdk.CdcErrorCond;
import com.xforceplus.ultraman.oqsengine.sdk.CdcErrorTaskInfo;
import com.xforceplus.ultraman.oqsengine.sdk.CdcRecover;
import com.xforceplus.ultraman.oqsengine.sdk.CdcRecoverSubmit;
import com.xforceplus.ultraman.oqsengine.sdk.CommitIdMaxMin;
import com.xforceplus.ultraman.oqsengine.sdk.CommitIdUp;
import com.xforceplus.ultraman.oqsengine.sdk.EntityRebuildServicePowerApi;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUp;
import com.xforceplus.ultraman.oqsengine.sdk.EntityUpList;
import com.xforceplus.ultraman.oqsengine.sdk.OperationResult;
import com.xforceplus.ultraman.oqsengine.sdk.QueryPage;
import com.xforceplus.ultraman.oqsengine.sdk.RebuildRequest;
import com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo;
import com.xforceplus.ultraman.oqsengine.sdk.RepairRequest;
import com.xforceplus.ultraman.oqsengine.sdk.ShowTask;
import com.xforceplus.ultraman.oqsengine.storage.define.OperationType;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * rebuild oqs.
 */
@Component
public class EntityRebuildServiceOqs implements EntityRebuildServicePowerApi {

    @Autowired
    private DevOpsManagementService devOpsManagementService;

    @Autowired
    private EntityManagementService entityManagementService;

    @Autowired
    private EntitySearchService entitySearchService;

    @Resource(name = "ioThreadPool")
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
                Optional<DevOpsTaskInfo> devOpsTaskInfo = devOpsManagementService.rebuildIndex(
                    entityClass,
                    LocalDateTime.parse(in.getStart(), dateTimeFormatter),
                    LocalDateTime.parse(in.getEnd(), dateTimeFormatter));

                return devOpsTaskInfo.map(this::toTaskInfo).orElse(empty);
            } catch (Exception e) {
                logger.error("{}", e);
                return empty;
            }
        });
    }

    @Override
    public Source<RebuildTaskInfo, NotUsed> showProgress(ShowTask in, Metadata metadata) {
        return Source.tick(Duration.ofSeconds(1), Duration.ofSeconds(3), in.getId())
            .map(i -> devOpsManagementService.syncTask(Long.toString(i)))
            .takeWhile(x -> x.isPresent())
            .map(x -> toTaskInfo(x.get()))
            .mapMaterializedValue(x -> NotUsed.getInstance());
    }

    /**
     * list all active tasks.
     *
     */
    @Override
    public Source<RebuildTaskInfo, NotUsed> listActiveTasks(QueryPage in, Metadata metadata) {
        try {
            return Source.from(devOpsManagementService.listActiveTasks(new Page(in.getNumber(), in.getSize())))
                .map(this::toTaskInfo);
        } catch (SQLException e) {
            return Source
                .single(RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial());
        }
    }

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
            return Source.from(devOpsManagementService.listAllTasks(new Page(in.getNumber(), in.getSize())))
                .map(this::toTaskInfo);
        } catch (SQLException e) {
            logger.error("{}", e);
            return Source
                .single(RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial());
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

    @Override
    @Deprecated
    public CompletionStage<OperationResult> entityRepair(EntityUpList in, Metadata metadata) {
        return async(() -> {
            try {
                throw new IllegalAccessException("not support interface.");
            } catch (Exception ex) {
                return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION).setMessage(ex.getMessage())
                    .build();
            }
        });
    }

    @Override
    @Deprecated
    public CompletionStage<OperationResult> cancelEntityRepair(RepairRequest in, Metadata metadata) {
        return async(() -> {
            try {
                throw new IllegalAccessException("not support interface.");
            } catch (Exception ex) {
                return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION).setMessage(ex.getMessage())
                    .build();
            }
        });
    }

    @Override
    @Deprecated
    public CompletionStage<OperationResult> clearRepairedInfos(RepairRequest in, Metadata metadata) {
        return async(() -> {
            try {
                throw new IllegalAccessException("not support interface.");
            } catch (Exception ex) {
                return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION).setMessage(ex.getMessage())
                    .build();
            }
        });
    }

    @Override
    @Deprecated
    public Source<RebuildTaskInfo, NotUsed> repairedInfoList(RepairRequest in, Metadata metadata) {
        try {
            throw new IllegalAccessException("not support interface.");
        } catch (Exception e) {
            logger.error("{}", e);
            return Source
                .single(RebuildTaskInfo.newBuilder().setErrCode("-1").setMessage(e.getMessage()).buildPartial());
        }
    }

    @Override
    @Deprecated
    public CompletionStage<OperationResult> isEntityRepaired(RepairRequest in, Metadata metadata) {
        return async(() -> {
            try {
                throw new IllegalAccessException("not support interface.");
            } catch (Exception ex) {
                return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION).setMessage(ex.getMessage())
                    .build();
            }

        });
    }

    @Override
    public CompletionStage<OperationResult> removeCommitIds(RepairRequest in, Metadata metadata) {
        return async(() -> {
            devOpsManagementService.removeCommitIds(in.getRidList().stream().toArray(Long[]::new));
            return OperationResult.newBuilder().setCode(OperationResult.Code.OK).build();
        });
    }

    @Override
    public CompletionStage<OperationResult> initNewCommitId(RepairRequest in, Metadata metadata) {
        return async(() -> {
            if (in.getRidCount() > 0) {
                try {
                    devOpsManagementService.initNewCommitId(Optional.ofNullable(in.getRid(0)));
                    return OperationResult.newBuilder().setCode(OperationResult.Code.OK).build();
                } catch (SQLException ex) {
                    return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION)
                        .setMessage(ex.getMessage()).build();
                }
            }
            return OperationResult.newBuilder().setCode(OperationResult.Code.OK).build();
        });
    }

    @Override
    public CompletionStage<OperationResult> cdcSendErrorRecover(CdcRecoverSubmit cdcRecoverSubmit, Metadata metadata) {
        return async(() -> {
            try {
                devOpsManagementService
                    .cdcSendErrorRecover(cdcRecoverSubmit.getSeqNo(), cdcRecoverSubmit.getRecoverObjectString());
                return OperationResult.newBuilder().setCode(OperationResult.Code.OK).build();
            } catch (SQLException ex) {
                return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION).setMessage(ex.getMessage())
                    .build();
            }
        });
    }

    @Override
    public CompletionStage<OperationResult> cdcRecoverOk(CdcRecover cdcRecover, Metadata metadata) {
        return async(() -> {
            try {
                Optional<CdcErrorTask> cdcErrorTaskOp = devOpsManagementService.queryOne(cdcRecover.getSeqNo());
                FixedStatus fixedStatus = FixedStatus.FIX_ERROR;
                if (cdcErrorTaskOp.isPresent()) {
                    CdcErrorTask task = cdcErrorTaskOp.get();
                    if (task.getErrorType() == ErrorType.DATA_FORMAT_ERROR.getType()
                        && task.getOp() > OperationType.UNKNOWN.getValue()
                        && task.getEntity() > UN_KNOW_ID
                        && task.getId() > UN_KNOW_ID) {

                        Optional<IEntity> entityOp =
                            entitySearchService.selectOne(task.getId(), new EntityClassRef(task.getEntity(), ""));

                        com.xforceplus.ultraman.oqsengine.core.service.pojo.OperationResult operationResult = null;
                        if (entityOp.isPresent()) {
                            IEntity entity = entityOp.get();
                            operationResult = entityManagementService.replace(entity);
                        } else {
                            operationResult =
                                entityManagementService.delete(Entity.Builder.anEntity()
                                    .withId(task.getId())
                                    .withVersion(task.getVersion())
                                    .build());
                        }

                        if (operationResult.getResultStatus().equals(ResultStatus.SUCCESS)) {
                            fixedStatus = FixedStatus.FIXED;
                        }
                    }
                }

                devOpsManagementService.cdcUpdateStatus(cdcRecover.getSeqNo(), fixedStatus);

                return OperationResult.newBuilder().setCode(OperationResult.Code.OK).build();
            } catch (SQLException ex) {
                return OperationResult.newBuilder().setCode(OperationResult.Code.EXCEPTION).setMessage(ex.getMessage())
                    .build();
            }
        });
    }

    @Override
    public Source<CdcErrorTaskInfo, NotUsed> queryCdcError(CdcErrorCond cdcErrorCond, Metadata metadata) {
        try {
            return Source.from(devOpsManagementService.queryCdcError(toCdcErrorQueryCondition(cdcErrorCond))
                .stream().map(this::toCdcErrorTaskInfo).collect(Collectors.toList()));
        } catch (SQLException e) {
            logger.error("{}", e);
            return Source.single(CdcErrorTaskInfo.newBuilder().setMessage(e.getMessage()).buildPartial());
        }

    }

    @Override
    public CompletionStage<CommitIdMaxMin> rangeOfCommits(CommitIdUp commitIdUp, Metadata metadata) {
        return async(() -> {
            long[] ids = devOpsManagementService.rangeOfCommitId();
            return CommitIdMaxMin.newBuilder().setMax(ids[0]).setMin(ids[1]).build();
        });
    }

    @Override
    public CompletionStage<OperationResult> cleanLessThan(CommitIdUp commitIdUp, Metadata metadata) {
        return async(() -> {
            devOpsManagementService.cleanLessThan(commitIdUp.getCommitId());
            return OperationResult.newBuilder().setCode(OperationResult.Code.OK).build();
        });
    }

    RebuildTaskInfo toTaskInfo(DevOpsTaskInfo taskInfo) {
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

    private CdcErrorTaskInfo toCdcErrorTaskInfo(CdcErrorTask cdcErrorTask) {
        return CdcErrorTaskInfo.newBuilder()
            .setSeqNo(cdcErrorTask.getSeqNo())
            .setBatchId(cdcErrorTask.getBatchId())
            .setId(cdcErrorTask.getId())
            .setEntity(cdcErrorTask.getEntity())
            .setVersion(cdcErrorTask.getVersion())
            .setOp(cdcErrorTask.getOp())
            .setCommitId(cdcErrorTask.getCommitId())
            .setErrorType(cdcErrorTask.getErrorType())
            .setStatus(cdcErrorTask.getStatus())
            .setOperationObject(cdcErrorTask.getOperationObject())
            .setExecuteTime(cdcErrorTask.getExecuteTime())
            .setFixedTime(cdcErrorTask.getFixedTime())
            .setMessage(cdcErrorTask.getMessage())
            .build();
    }

    private CdcErrorQueryCondition toCdcErrorQueryCondition(CdcErrorCond cond) {
        CdcErrorQueryCondition cdcErrorQueryCondition = new CdcErrorQueryCondition();
        if (cond.getSeqNo() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setSeqNo(cond.getSeqNo());
        }

        if (cond.getBatchId() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setBatchId(cond.getBatchId());
        }

        if (cond.getId() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setId(cond.getId());
        }

        if (cond.getEntity() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setEntity(cond.getEntity());
        }

        if (cond.getType() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setType(cond.getType());
        }

        if (cond.getStatus() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setStatus(cond.getStatus());
            cdcErrorQueryCondition.setEqualStatus(cond.getEqualStatus());
        }

        if (cond.getRangeLEExecuteTime() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setRangeLeExecuteTime(cond.getRangeLEExecuteTime());
        }

        if (cond.getRangeGeExecuteTime() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setRangeGeExecuteTime(cond.getRangeGeExecuteTime());
        }

        if (cond.getRangeLEFixedTime() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setRangeLeFixedTime(cond.getRangeLEFixedTime());
        }

        if (cond.getRangeGeFixedTime() > CDCConstant.ZERO) {
            cdcErrorQueryCondition.setRangeGeFixedTime(cond.getRangeGeFixedTime());
        }

        return cdcErrorQueryCondition;
    }
}
