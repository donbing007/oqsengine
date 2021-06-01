package com.xforceplus.ultraman.oqsengine.calculate.dto;

import com.xforceplus.ultraman.oqsengine.calculate.exception.CalculateExecutionException;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.Calculator;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;

/**
 *  执行一个表达式的对象表示.
 *
 *  @author  j.xu
 *  @version 0.1 2021/05/2021/5/12
 *  @since 1.8
 */
public class ExecutionWrapper<T> {

    /**
     * 一个执行表达式唯一key,目前由entityFieldCode构成.
     */
    private String code;

    /**
     * 依赖层级.
     */
    private int level;

    /**
     * 返回值类型.
     */
    private Class<T> retClazz;


    /**
     * entityField.
     */
    private IEntityField entityField;

    /**
     * 表达式.
     */
    private ExpressionWrapper expressionWrapper;

    private ExecutionWrapper() {

    }

    public int getLevel() {
        return level;
    }

    public ExpressionWrapper getExpressionWrapper() {
        return expressionWrapper;
    }

    public String getCode() {
        return code;
    }

    public Class<T> getRetClazz() {
        return retClazz;
    }

    public IEntityField getEntityField() {
        return entityField;
    }

    /**
     * builder.
     */
    public static final class Builder<T> {

        private String code;
        private Integer level;
        private Class<T> retClazz;
        private ExpressionWrapper expressionWrapper;
        private IEntityField entityField;

        private Builder() {
        }

        public static ExecutionWrapper.Builder anExecution() {
            return new ExecutionWrapper.Builder();
        }

        public ExecutionWrapper.Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public ExecutionWrapper.Builder witLevel(int level) {
            this.level = level;
            return this;
        }

        public ExecutionWrapper.Builder withRetClass(Class<T> retClazz) {
            this.retClazz = retClazz;
            return this;
        }

        public ExecutionWrapper.Builder withExpressionWrapper(ExpressionWrapper expression) {
            this.expressionWrapper = expression;
            return this;
        }

        public ExecutionWrapper.Builder withIEntityField(IEntityField entityField) {
            this.entityField = entityField;
            return this;
        }


        /**
         * build.
         */
        public ExecutionWrapper<T> build() {

            ExecutionWrapper<T> executionWrapper = new ExecutionWrapper<T>();

            if (null == this.code) {
                throw new CalculateExecutionException("build ExecutionWrapper error, code couldn't be null.");
            }
            executionWrapper.code = this.code;

            if (null == this.level) {
                throw new CalculateExecutionException(
                    String.format("build ExecutionWrapper error, level couldn't be null, code-[%s].", this.code));
            }
            executionWrapper.level = this.level;

            executionWrapper.retClazz = this.retClazz;

            if (null == this.expressionWrapper) {
                throw new CalculateExecutionException(
                    String.format("build ExecutionWrapper error, expressionWrapper couldn't be null, code-[%s].",
                        this.code));
            }
            executionWrapper.expressionWrapper = this.expressionWrapper;

            if (null == this.entityField) {
                throw new CalculateExecutionException(
                    String.format("build ExecutionWrapper error, entityField couldn't be null, code-[%s].", this.code));
            }

            if (null == this.entityField.calculator().getFailedPolicy()) {
                throw new CalculateExecutionException(
                    String.format("failedPolicy can't be null in build function, code-[%s].", this.code));
            }
            if (this.entityField.calculator().getFailedPolicy()
                .equals(Calculator.FailedPolicy.USE_FAILED_DEFAULT_VALUE)) {
                if (null == this.entityField.calculator().getFailedDefaultValue()) {
                    throw new CalculateExecutionException(
                        String.format(
                            "failedDefaultValue can't be null when failedPolicy [USE_FAILED_DEFAULT_VALUE] in build function, code-[%s].",
                            this.code)
                    );
                }
            }

            executionWrapper.entityField = this.entityField;

            return executionWrapper;
        }
    }
}
