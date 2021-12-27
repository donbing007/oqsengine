package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Hint;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

/**
 * 表示操作结果.
 *
 * @author xujia 2021/4/8
 * @since 1.8
 */
public class OperationResult implements Serializable {
    private ResultStatus resultStatus;
    private Collection<Hint> hints = Collections.emptyList();
    private String message;

    public static OperationResult unknown() {
        return new OperationResult(ResultStatus.UNKNOWN, ResultStatus.UNKNOWN.name());
    }

    public static OperationResult success() {
        return success(ResultStatus.SUCCESS.name());
    }

    public static OperationResult success(String msg) {
        return new OperationResult(ResultStatus.SUCCESS, msg);
    }

    public static OperationResult conflict() {
        return conflict(ResultStatus.CONFLICT.name());
    }

    public static OperationResult conflict(String msg) {
        return new OperationResult(ResultStatus.CONFLICT, msg);
    }

    public static OperationResult notFound() {
        return notFound(ResultStatus.NOT_FOUND.name());
    }

    public static OperationResult notFound(String msg) {
        return new OperationResult(ResultStatus.NOT_FOUND, msg);
    }

    public static OperationResult unCreated() {
        return unCreated(ResultStatus.UNACCUMULATE.name());
    }

    public static OperationResult unCreated(String msg) {
        return new OperationResult(ResultStatus.UNCREATED, msg);
    }

    public static OperationResult unAccumulate() {
        return unAccumulate(ResultStatus.UNACCUMULATE.name());
    }

    public static OperationResult unAccumulate(String msg) {
        return new OperationResult(ResultStatus.UNACCUMULATE, msg);
    }

    public static OperationResult elevatefailed() {
        return elevatefailed(ResultStatus.ELEVATEFAILED.name());
    }

    public static OperationResult elevatefailed(String msg) {
        return new OperationResult(ResultStatus.ELEVATEFAILED, msg);
    }

    public static OperationResult halfSuccess() {
        return halfSuccess(ResultStatus.HALF_SUCCESS.name());
    }

    public static OperationResult halfSuccess(String msg) {
        return new OperationResult(ResultStatus.HALF_SUCCESS, msg);
    }

    public static OperationResult fieldMust() {
        return fieldMust(ResultStatus.FIELD_MUST.name());
    }

    public static OperationResult fieldMust(String msg) {
        return new OperationResult(ResultStatus.FIELD_MUST, msg);
    }

    public static OperationResult fieldToLong() {
        return fieldToLong(ResultStatus.FIELD_TOO_LONG.name());
    }

    public static OperationResult fieldToLong(String msg) {
        return new OperationResult(ResultStatus.FIELD_TOO_LONG, msg);
    }

    public static OperationResult fieldHighPrecision() {
        return fieldHighPrecision(ResultStatus.FIELD_HIGH_PRECISION.name());
    }

    public static OperationResult fieldHighPrecision(String msg) {
        return new OperationResult(ResultStatus.FIELD_HIGH_PRECISION, msg);
    }

    public static OperationResult fieldNonExist() {
        return fieldNonExist(ResultStatus.FIELD_NON_EXISTENT.name());
    }

    public static OperationResult fieldNonExist(String msg) {
        return new OperationResult(ResultStatus.FIELD_NON_EXISTENT, msg);
    }

    private OperationResult(ResultStatus resultStatus, String message) {
        this.resultStatus = resultStatus;
        this.message = message;
    }

    /**
     * 增加一个提示.
     *
     * @param hint 提示.
     * @return 当前实例.
     */
    public OperationResult addHint(Hint hint) {
        if (Collections.EMPTY_LIST == hints) {
            this.hints = new LinkedList();
        }

        this.hints.add(hint);

        if (ResultStatus.SUCCESS == this.resultStatus) {
            this.resultStatus = ResultStatus.HALF_SUCCESS;
        }

        return this;
    }

    /**
     * 增加多个提示.
     *
     * @param hints 提示列表.
     * @return 当前实例.
     */
    public OperationResult addHints(Collection<Hint> hints) {
        if (Collections.EMPTY_LIST == hints) {
            this.hints = new LinkedList();
        }

        this.hints.addAll(hints);

        if (ResultStatus.SUCCESS == this.resultStatus) {
            this.resultStatus = ResultStatus.HALF_SUCCESS;
        }

        return this;
    }

    public boolean isSuccess() {
        return ResultStatus.SUCCESS == this.resultStatus || ResultStatus.HALF_SUCCESS == this.resultStatus;
    }

    public ResultStatus getResultStatus() {
        return resultStatus;
    }

    public String getMessage() {
        return message;
    }

    public Collection<Hint> getHints() {
        return hints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OperationResult)) {
            return false;
        }
        OperationResult result = (OperationResult) o;
        return resultStatus == result.resultStatus && Objects.equals(hints, result.hints)
            && Objects.equals(message, result.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resultStatus, hints, message);
    }
}
