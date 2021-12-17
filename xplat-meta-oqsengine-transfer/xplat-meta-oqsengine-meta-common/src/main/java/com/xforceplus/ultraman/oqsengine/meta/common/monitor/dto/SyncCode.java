package com.xforceplus.ultraman.oqsengine.meta.common.monitor.dto;

/**
 * Created by justin.xu on 12/2021.
 *
 * @since 1.8
 */
public enum SyncCode {
    /**
     * success by infoLogs
     */
    REGISTER_OK,
    RESET_ENV_OK,
    SYNC_DATA_OK,

    /**
     * failed by errorLogs
     */
    INIT_OBSERVER_ERROR,
    RESET_ENV_ERROR,
    SEND_REQUEST_ERROR,
    REGISTER_ERROR,
    SYNC_DATA_ERROR,
    KEEP_ALIVE_ERROR,
    INTERNAL_CHECK_ERROR,
    SERVICE_INACTIVE_ERROR,


    PULL_DATA_FAILED,
    PUSH_DATA_CHECK_FAILED,
    SEND_PUSH_DATA_FAILED,
    PUSH_DATA_FAILED;
}
