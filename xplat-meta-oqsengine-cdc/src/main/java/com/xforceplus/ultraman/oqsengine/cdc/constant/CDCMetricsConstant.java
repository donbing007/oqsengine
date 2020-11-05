package com.xforceplus.ultraman.oqsengine.cdc.constant;

import static com.xforceplus.ultraman.oqsengine.cdc.constant.CDCConstant.DEFAULT_FREE_MESSAGE_MAX_REPORT_THRESHOLD;

/**
 * desc :
 * name : CDCMetricsConstant
 *
 * @author : xujia
 * date : 2020/11/5
 * @since : 1.8
 */
public class CDCMetricsConstant {
    public static final String POOL_NAME = "cdcCallBackPool";

    public static final int THREAD_POOL_SIZE = 1;

    public static final int MAX_QUEUE_SIZE = 1024;


    private static final int FREE_MESSAGE_MAX_REPORT_THRESHOLD = DEFAULT_FREE_MESSAGE_MAX_REPORT_THRESHOLD;
}
