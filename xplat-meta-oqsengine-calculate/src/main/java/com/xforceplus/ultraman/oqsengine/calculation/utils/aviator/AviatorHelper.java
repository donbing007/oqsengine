package com.xforceplus.ultraman.oqsengine.calculation.utils.aviator;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExecutionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.dto.ExpressionWrapper;
import com.xforceplus.ultraman.oqsengine.calculation.exception.CalculationException;
import com.xforceplus.ultraman.oqsengine.calculation.function.GetIDFunction;
import com.xforceplus.ultraman.oqsengine.calculation.function.TimeOffsetFunction;
import java.math.MathContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * aviator辅助类.
 */
public class AviatorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AviatorHelper.class);

    private static final String REGEX_META = "(#\\{[^#${}]*\\})";
    private static final String REGEX_ENUM = "(\\$\\{[^#${}]*\\})";

    //  LRU-CACHE最大缓存公式个数
    private static final int CAPACITY = 1024 * 10;
    //  函数内默认最大循环次数Assertions
    private static final int MAX_LOOP_COUNT = 8;
    private static final AviatorEvaluatorInstance INSTANCE;

    static {
        INSTANCE = AviatorEvaluator.getInstance().useLRUExpressionCache(CAPACITY);
        //  允许高精度计算模式
        INSTANCE.setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        //  使用DECIMAL128作为精度标准，即precision为34
        INSTANCE.setOption(Options.MATH_CONTEXT, MathContext.DECIMAL128);
        //  最大循环次数
        INSTANCE.setOption(Options.MAX_LOOP_COUNT, MAX_LOOP_COUNT);

        functionAdd();
    }

    private static void functionAdd() {
        INSTANCE.addFunction(new TimeOffsetFunction());
        INSTANCE.addFunction(new GetIDFunction());
    }

    /**
     * 编译一个函数.
     */
    public static Expression compile(ExpressionWrapper expressionWrapper) {
        String functionBody = AviatorHelper.parseRule(expressionWrapper.getExpression());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Function body : {}", functionBody);
        }
        return INSTANCE.compile(expressionWrapper.getCode(), functionBody, expressionWrapper.isCached());
    }

    /**
     * 编译并执行一个函数.
     */
    public static Object execute(ExecutionWrapper executionWrapper) throws CalculationException {
        Expression expression = compile(executionWrapper.getExpressionWrapper());
        if (null == expression) {
            throw new CalculationException(String.format("compile [expression-%s] failed .",
                executionWrapper.getExpressionWrapper().getExpression()));
        }
        return expression.execute(executionWrapper.getParams());
    }

    /**
     * 注册一个自定义函数.
     *
     * @param function 待添加的函数实例
     */
    public static void addFunction(AviatorFunction function) {
        INSTANCE.addFunction(function);
    }

    /**
     * 将规则转换成aviator可以识别的格式.
     */
    public static String parseRule(String ruleContent) {
        Pattern pattern = Pattern.compile(REGEX_META);
        Pattern enumPatten = Pattern.compile(REGEX_ENUM);
        return parse(parse(ruleContent, pattern), enumPatten);
    }

    private static String parse(String ruleContent, Pattern pattern) {
        Matcher matcher = pattern.matcher(ruleContent);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String matchStr = matcher.group();
            matcher.appendReplacement(sb, Matcher.quoteReplacement(matchStr.substring(2, matchStr.length() - 1)));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
