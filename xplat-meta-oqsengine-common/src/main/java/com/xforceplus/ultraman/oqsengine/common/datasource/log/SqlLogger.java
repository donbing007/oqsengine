package com.xforceplus.ultraman.oqsengine.common.datasource.log;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.FormattedLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于 p6spy 的SQL日志记录器.
 *
 * @author dongbin
 * @version 0.1 2020/11/24 14:22
 * @since 1.8
 */
public class SqlLogger extends FormattedLogger {

    final Logger logger = LoggerFactory.getLogger("sqlLogger");

    @Override
    public void logException(Exception e) {
        logger.error(e.getMessage(), e);
    }

    @Override
    public void logText(String text) {
        if (logger.isDebugEnabled()) {
            logger.debug(text);
        }
    }

    @Override
    public boolean isCategoryEnabled(Category category) {
        if (Category.ERROR.equals(category)) {
            return logger.isErrorEnabled();
        } else if (Category.WARN.equals(category)) {
            return logger.isWarnEnabled();
        } else if (Category.DEBUG.equals(category)) {
            return logger.isDebugEnabled();
        } else {
            return logger.isInfoEnabled();
        }
    }

    @Override
    public void logSQL(
        int connectionId,
        String now,
        long elapsed,
        Category category,
        String prepared,
        String sql,
        String url) {

        if (logger.isDebugEnabled()) {
            final String msg = strategy.formatMessage(connectionId, now, elapsed, category.toString(), prepared, sql, url);
            logger.debug(msg);
        }
    }
}
