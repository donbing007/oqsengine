package com.xforceplus.ultraman.oqsengine.boot.grpc.utils;

import com.xforceplus.ultraman.devops.service.common.exception.DiscoverClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by justin.xu on 11/2021.
 *
 * @since 1.8
 */
public class PrintErrorHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(PrintErrorHelper.class);

    /**
     * 打印异常.
     */
    public static void exceptionHandle(String businessMessage, Exception e) {
        String error = String.format("%s, message : %s", businessMessage, e.getMessage());
        LOGGER.warn(error);
        throw new DiscoverClientException(error);
    }
}
