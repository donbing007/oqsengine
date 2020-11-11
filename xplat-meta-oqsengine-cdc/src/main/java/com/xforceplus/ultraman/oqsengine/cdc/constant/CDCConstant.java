package com.xforceplus.ultraman.oqsengine.cdc.constant;

/**
 * desc :
 * name : CDCConstant
 *
 * @author : xujia
 * date : 2020/11/3
 * @since : 1.8
 */
public class CDCConstant {
    public static final int DAEMON_NODE_ID = 0;
    public static final int EMPTY_BATCH_ID = -1;
    public static final int EMPTY_BATCH_SIZE = 0;
    public static final int EMPTY_COLUMN_SIZE = 0;
    public static final int INIT_ID = -1;
    public static final int ZERO = 0;

    public static final int SINGLE_CONSUMER = 1;

    //  当前CDC connection连接失败后的重连间隔 (默认3S)
    public static final int RECONNECT_WAIT_IN_SECONDS = 3;

    //  当前CDC 中没有同步message时的休眠间隔 (默认1S)
    public static final int FREE_MESSAGE_WAIT_IN_SECONDS = 1;

    //  一次获得的binlog最大量
    public static final int DEFAULT_BATCH_SIZE = 2048;

    //  当前CDC 中freeMessage需要上报指标的阈值 (默认5S)
    public static final int DEFAULT_FREE_MESSAGE_MAX_REPORT_THRESHOLD = 5;

    //  订阅的binlog日志 数据库.表，默认所有库中oqsbigentity开头的表
    public static final String DEFAULT_SUBSCRIBE_FILTER = ".*\\.oqsbigentity.*";

    //  META中的分隔符
    public static final String SPLITTER = "-";
    //  META中的长度
    public static final int SPLIT_META_LENGTH = 2;

}