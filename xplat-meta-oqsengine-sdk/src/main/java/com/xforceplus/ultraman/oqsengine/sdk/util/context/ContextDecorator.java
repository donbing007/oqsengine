package com.xforceplus.ultraman.oqsengine.sdk.util.context;

import com.xforceplus.xplat.galaxy.framework.context.ContextService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Supplier;

/**
 * ContextDecorator
 */
public class ContextDecorator {

    static Logger logger = LoggerFactory.getLogger(ContextDecorator.class);

    public static <T> Supplier<T> decorateSupplier(ContextService contextService, Supplier<T> supplier) {
        Thread buildThread = Thread.currentThread();
        Map<String, Object> all = null;
        if (contextService != null) {
            all = contextService.getAll();
        }
        Map<String, Object> finalAll = all;
        return () -> {
            //  contextService
            Thread runningThread = Thread.currentThread();
            try {
                if (runningThread != buildThread) {
                    //copy
                    if (contextService != null && finalAll != null) {
                        logger.info("Moving Context from {} to {}", buildThread, runningThread);
                        contextService.fromMap(finalAll);
                    }
                }
                T result = supplier.get();

                return result;

            } catch (RuntimeException ex) {
                ex.printStackTrace();
                return null;
            } finally {
                if (runningThread != buildThread) {
                    //copy
                    if (contextService != null) {
                        logger.info("Cleaning Context which come from {} to {}", buildThread, runningThread);
                        contextService.clear();
                    }
                }
            }
        };
    }
}
