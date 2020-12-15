
// Generated by Akka gRPC. DO NOT EDIT.
package com.xforceplus.ultraman.oqsengine.sdk;

import akka.grpc.ProtobufSerializer;
import akka.grpc.javadsl.GoogleProtobufSerializer;
import akka.grpc.javadsl.Metadata;
import akka.grpc.GrpcServiceException;
import io.grpc.Status;

/*
 * Generated by Akka gRPC. DO NOT EDIT.
 */
public interface EntityRebuildServicePowerApi extends EntityRebuildService {
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> rebuildIndex(com.xforceplus.ultraman.oqsengine.sdk.RebuildRequest in, Metadata metadata);
  
  akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> showProgress(com.xforceplus.ultraman.oqsengine.sdk.ShowTask in, Metadata metadata);
  
  akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> listActiveTasks(com.xforceplus.ultraman.oqsengine.sdk.QueryPage in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> getActiveTask(com.xforceplus.ultraman.oqsengine.sdk.EntityUp in, Metadata metadata);
  
  akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> listAllTasks(com.xforceplus.ultraman.oqsengine.sdk.QueryPage in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> cancelTask(com.xforceplus.ultraman.oqsengine.sdk.ShowTask in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> entityRepair(com.xforceplus.ultraman.oqsengine.sdk.EntityUpList in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> cancelEntityRepair(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> clearRepairedInfos(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in, Metadata metadata);
  
  akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> repairedInfoList(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> isEntityRepaired(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> removeCommitIds(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in, Metadata metadata);
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> initNewCommitId(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in, Metadata metadata);
  

  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> rebuildIndex(com.xforceplus.ultraman.oqsengine.sdk.RebuildRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> showProgress(com.xforceplus.ultraman.oqsengine.sdk.ShowTask in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> listActiveTasks(com.xforceplus.ultraman.oqsengine.sdk.QueryPage in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> getActiveTask(com.xforceplus.ultraman.oqsengine.sdk.EntityUp in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> listAllTasks(com.xforceplus.ultraman.oqsengine.sdk.QueryPage in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> cancelTask(com.xforceplus.ultraman.oqsengine.sdk.ShowTask in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> entityRepair(com.xforceplus.ultraman.oqsengine.sdk.EntityUpList in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> cancelEntityRepair(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> clearRepairedInfos(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default akka.stream.javadsl.Source<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo, akka.NotUsed> repairedInfoList(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> isEntityRepaired(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> removeCommitIds(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  
  default java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> initNewCommitId(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest in) {
    throw new GrpcServiceException(Status.UNIMPLEMENTED);
  }
  

  static String name = "EntityRebuildService";

  public static class Serializers {
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.EntityUpList> EntityUpListSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.EntityUpList.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.RebuildRequest> RebuildRequestSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.RebuildRequest.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.ShowTask> ShowTaskSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.ShowTask.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.QueryPage> QueryPageSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.QueryPage.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo> RebuildTaskInfoSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.RebuildTaskInfo.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.EntityUp> EntityUpSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.EntityUp.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.RepairRequest> RepairRequestSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.RepairRequest.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> OperationResultSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.OperationResult.class);
    
  }
}
