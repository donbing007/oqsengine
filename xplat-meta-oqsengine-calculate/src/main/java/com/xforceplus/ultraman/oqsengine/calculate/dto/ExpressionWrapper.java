package com.xforceplus.ultraman.oqsengine.calculate.dto;

import com.xforceplus.ultraman.oqsengine.calculate.exception.CalculateExecutionException;
import com.xforceplus.ultraman.oqsengine.pojo.utils.MD5Utils;

/**
 *  表达式.
 *
 *  @author  j.xu
 *  @version 0.1 2021/05/2021/5/10
 *  @since 1.8
 */
public class ExpressionWrapper {

    /**
     * 表达式唯一key,目前由expression的md5编码构成.
     */
    private String code;

    /**
     * 表达式.
     */
    private String expression;

    /**
     * 是否需要在规则引擎中缓存该规则.
     */
    private boolean cached;


    /**
     * 构造函数.
     */
    private ExpressionWrapper() {

    }

    /**
     * 获取code.
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取expression.
     */
    public String getExpression() {
        return expression;
    }

    /**
     * 获取是否缓存.
     */
    public boolean isCached() {
        return cached;
    }

    /**
     * builder.
     */
    public static final class Builder {

        private String code;

        private String expression;

        private Boolean cached;

        private Builder() {
        }

        public static Builder anExpression() {
            return new ExpressionWrapper.Builder();
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withExpression(String expression) {
            this.expression = expression;
            return this;
        }

        public Builder withCached(Boolean cached) {
            this.cached = cached;
            return this;
        }

        /**
         * build.
         */
        public ExpressionWrapper build() {
            if (null == this.expression || this.expression.isEmpty()) {
                throw new CalculateExecutionException("expression can't be null in build function.");
            }
            ExpressionWrapper expressionWrapper = new ExpressionWrapper();

            expressionWrapper.expression = this.expression;
            expressionWrapper.code = (null == this.code || this.code.isEmpty())
                                                         ? MD5Utils.encrypt(this.expression) : this.code;
            expressionWrapper.cached = (null != this.cached && this.cached);

            return expressionWrapper;
        }
    }
}
