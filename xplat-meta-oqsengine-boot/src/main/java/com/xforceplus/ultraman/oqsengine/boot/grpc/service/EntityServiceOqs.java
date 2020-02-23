package com.xforceplus.ultraman.oqsengine.boot.grpc.service;

import akka.grpc.javadsl.Metadata;
import com.xforceplus.ultraman.oqsengine.sdk.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class EntityServiceOqs implements EntityServicePowerApi {

    @Override
    public CompletionStage<OperationResult> begin(TransactionUp in, Metadata metadata) {
        return null;
    }

    @Override
    public CompletionStage<OperationResult> build(EntityUp in, Metadata metadata) {


        return null;
    }

    @Override
    public CompletionStage<OperationResult> replace(EntityUp in, Metadata metadata) {
        return null;
    }

    @Override
    public CompletionStage<OperationResult> remove(EntityUp in, Metadata metadata) {
        return null;
    }

    @Override
    public CompletionStage<OperationResult> selectOne(EntityUp in, Metadata metadata) {

        OperationResult result = OperationResult.newBuilder()
                            .setCode(OperationResult.Code.EXCEPTION)
                            .setMessage("还没有实现")
                            .buildPartial();
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletionStage<OperationResult> selectByConditions(SelectByCondition in, Metadata metadata) {
        return null;
    }

    @Override
    public CompletionStage<OperationResult> commit(TransactionUp in, Metadata metadata) {
        return null;
    }

    @Override
    public CompletionStage<OperationResult> rollBack(TransactionUp in, Metadata metadata) {
        return null;
    }

    private Optional<Long> extractTransaction(Metadata metadata){
        Optional<String> transactionId =  metadata.getText("transaction-id");
        return transactionId.map(x -> Long.valueOf(x));
    }
}
