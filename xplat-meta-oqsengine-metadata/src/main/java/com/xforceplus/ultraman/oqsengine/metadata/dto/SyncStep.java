package com.xforceplus.ultraman.oqsengine.metadata.dto;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public class SyncStep<T> {
    private StepDefinition stepDefinition;
    private String message;

    private T data;

    private SyncStep() {
    }

    public StepDefinition getStepDefinition() {
        return stepDefinition;
    }

    public void setStepDefinition(StepDefinition stepDefinition) {
        this.stepDefinition = stepDefinition;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public String toPersistentMessage() {
        return stepDefinition.name() + ":" + message;
    }

    public void setData(T data) {
        this.data = data;
    }

    /**
     * 返回一个成功的包装对象.
     */
    public static SyncStep ok() {
        SyncStep step = new SyncStep();
        step.stepDefinition = StepDefinition.SUCCESS;
        step.message = "step ok";
        return step;
    }

    /**
     * 返回一个成功的包装对象.
     */
    public static <T> SyncStep<T> ok(T data) {
        SyncStep step = ok();
        step.data = data;
        step.stepDefinition = StepDefinition.SUCCESS;
        return step;
    }

    /**
     * 返回一个失败的包装对象.
     */
    public static SyncStep failed(StepDefinition stepDefinition, String message) {
        SyncStep step = new SyncStep();
        step.stepDefinition = stepDefinition;
        step.message = message;
        return step;
    }

    /**
     * StepDefinition.
     */
    public static enum StepDefinition {
        UNKNOWN,
        SYNC_CLIENT_FAILED,
        DUPLICATE_PREPARE_FAILED,
        QUERY_VERSION_FAILED,
        PARSER_PROTO_BUF_FAILED,
        SAVE_ENTITY_CLASS_STORAGE_FAILED,
        BUILD_EVENT_FAILED,
        SUCCESS
    }

}
