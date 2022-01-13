package com.xforceplus.ultraman.oqsengine.core.service.pojo;

import com.xforceplus.ultraman.oqsengine.pojo.contract.ResultStatus;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.EntityClassRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Hint;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntity;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

/**
 * 表示操作结果.
 *
 * @author xujia 2021/4/8
 * @since 1.8
 */
public class OqsResult<V> implements Serializable {
    private ResultStatus resultStatus;
    private V value;
    private Collection<Hint> hints;
    private String message;

    public static OqsResult unknown() {
        return new OqsResult(ResultStatus.UNKNOWN, ResultStatus.UNKNOWN.name());
    }

    public static OqsResult success() {
        return success("");
    }

    public static OqsResult success(String msg) {
        return new OqsResult(ResultStatus.SUCCESS, msg);
    }

    public static OqsResult<IEntity> success(IEntity entity) {
        return new OqsResult(ResultStatus.SUCCESS, entity, null);
    }

    public static OqsResult<Long> success(long value) {
        return new OqsResult<>(ResultStatus.SUCCESS, value, null);
    }

    public static OqsResult<Collection<IEntity>> success(Collection<IEntity> entities) {
        return new OqsResult<Collection<IEntity>>(ResultStatus.SUCCESS, entities, null);
    }

    public static OqsResult conflict() {
        return conflict(null);
    }

    public static OqsResult conflict(String msg) {
        return new OqsResult(ResultStatus.CONFLICT, msg);
    }

    public static OqsResult conflict(long entityId) {
        return notFound(String.format("A conflict occurred for entity %d..", entityId));
    }

    public static OqsResult notFound() {
        return notFound(null);
    }

    public static OqsResult notFound(String msg) {
        return new OqsResult(ResultStatus.NOT_FOUND, msg);
    }

    public static OqsResult notFound(long entityId) {
        return notFound(String.format("Entity %d was not found.", entityId));
    }

    public static OqsResult unCreated() {
        return new OqsResult(ResultStatus.UNCREATED, "The entity was not created successfully.");
    }

    public static OqsResult unAccumulate() {
        return unAccumulate(null);
    }

    public static OqsResult unAccumulate(String msg) {
        return new OqsResult(ResultStatus.UNACCUMULATE, msg);
    }

    public static OqsResult elevatefailed() {
        return elevatefailed(null);
    }

    public static OqsResult elevatefailed(String msg) {
        return new OqsResult(ResultStatus.ELEVATEFAILED, msg);
    }

    public static OqsResult halfSuccess() {
        return halfSuccess(null);
    }

    public static OqsResult halfSuccess(String msg) {
        return new OqsResult(ResultStatus.HALF_SUCCESS, msg);
    }

    /**
     * 缺少必须字段.
     */
    public static OqsResult fieldMust(IEntityField field) {
        return new OqsResult(ResultStatus.FIELD_MUST, String.format("The field %s is required.", field.name()));
    }

    /**
     * 字段过长.
     */
    public static OqsResult fieldTooLong(IEntityField field) {
        return new OqsResult(ResultStatus.FIELD_TOO_LONG,
            String.format("Field %s is too long. The maximum allowed length is %d.",
                field.name(), field.config().getLen()));
    }

    /**
     * 精度过高.
     */
    public static OqsResult fieldHighPrecision(IEntityField field) {
        return new OqsResult(ResultStatus.FIELD_HIGH_PRECISION,
            String.format("Field %s is too precise. The maximum accuracy allowed is %d.",
                field.name(), field.config().getPrecision()));
    }

    /**
     * 字段不存在.
     */
    public static OqsResult fieldNonExist(IEntityField field) {
        return new OqsResult(ResultStatus.FIELD_NON_EXISTENT,
            String.format("The field %s does not exist.", field.name()));
    }

    public static OqsResult notExistMeta() {
        return new OqsResult(ResultStatus.NOT_EXIST_META, "Unexpected meta information.");
    }

    /**
     * 元信息不存在.
     */
    public static OqsResult notExistMeta(EntityClassRef ref) {
        if (ref.getProfile() != null) {
            return new OqsResult(ResultStatus.NOT_EXIST_META,
                String.format("Meta message %s-%s does not exist.", ref.getCode(), ref.getProfile()));
        } else {
            return new OqsResult(ResultStatus.NOT_EXIST_META,
                String.format("Meta message %s does not exist.", ref.getCode()));
        }
    }

    private OqsResult(ResultStatus resultStatus, String message) {
        this(resultStatus, null, message);
    }

    private OqsResult(ResultStatus resultStatus, V value, String message) {
        this.resultStatus = resultStatus;
        this.message = message;
        this.value = value;
    }

    public OqsResult copy(Object value) {
        return new OqsResult(this.resultStatus, value, this.message);
    }

    /**
     * 增加一个提示.
     *
     * @param hint 提示.
     * @return 当前实例.
     */
    public OqsResult addHint(Hint hint) {
        if (this.hints == null) {
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
    public OqsResult addHints(Collection<Hint> hints) {
        if (this.hints == null) {
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

    public Optional<V> getValue() {
        return Optional.ofNullable(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OqsResult)) {
            return false;
        }
        OqsResult<?> result = (OqsResult<?>) o;
        return getResultStatus() == result.getResultStatus() && Objects.equals(getValue(), result.getValue())
            && Objects.equals(getHints(), result.getHints()) && Objects.equals(getMessage(),
            result.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResultStatus(), getValue(), getHints(), getMessage());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OperationResult{");
        sb.append("hints=").append(hints);
        sb.append(", message='").append(message).append('\'');
        sb.append(", resultStatus=").append(resultStatus);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }

}
