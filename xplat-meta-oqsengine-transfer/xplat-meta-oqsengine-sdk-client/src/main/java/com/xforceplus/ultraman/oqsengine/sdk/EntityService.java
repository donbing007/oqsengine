
// Generated by Akka gRPC. DO NOT EDIT.
package com.xforceplus.ultraman.oqsengine.sdk;

import akka.grpc.ProtobufSerializer;
import akka.grpc.javadsl.GoogleProtobufSerializer;


public interface EntityService {
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> begin(com.xforceplus.ultraman.oqsengine.sdk.TransactionUp in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> build(com.xforceplus.ultraman.oqsengine.sdk.EntityUp in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> replace(com.xforceplus.ultraman.oqsengine.sdk.EntityUp in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> replaceByCondition(com.xforceplus.ultraman.oqsengine.sdk.SelectByCondition in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> remove(com.xforceplus.ultraman.oqsengine.sdk.EntityUp in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> selectOne(com.xforceplus.ultraman.oqsengine.sdk.EntityUp in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> selectByConditions(com.xforceplus.ultraman.oqsengine.sdk.SelectByCondition in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> commit(com.xforceplus.ultraman.oqsengine.sdk.TransactionUp in);
  
  
  java.util.concurrent.CompletionStage<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> rollBack(com.xforceplus.ultraman.oqsengine.sdk.TransactionUp in);
  

  static String name = "EntityService";
  static akka.grpc.ServiceDescription description = new akka.grpc.internal.ServiceDescriptionImpl(name, EntityResourceProto.getDescriptor());

  public static class Serializers {
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.TransactionUp> TransactionUpSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.TransactionUp.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.EntityUp> EntityUpSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.EntityUp.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.SelectByCondition> SelectByConditionSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.SelectByCondition.class);
    
      public static ProtobufSerializer<com.xforceplus.ultraman.oqsengine.sdk.OperationResult> OperationResultSerializer = new GoogleProtobufSerializer<>(com.xforceplus.ultraman.oqsengine.sdk.OperationResult.class);
    
  }
}
