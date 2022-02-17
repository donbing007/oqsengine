package com.xforceplus.ultraman.oqsengine.devops.om.model;

/**
 * 统一数据运维响应.
 *
 * @copyright: 上海云砺信息科技有限公司
 * @author: youyifan
 * @createTime: 11/3/2021 4:45 PM
 * @description:
 * @history:
 */
public class DevOpsDataResponse {

    private String message;

    /**
     * 构造函数.
     *
     * @param message 消息
     */
    public DevOpsDataResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
