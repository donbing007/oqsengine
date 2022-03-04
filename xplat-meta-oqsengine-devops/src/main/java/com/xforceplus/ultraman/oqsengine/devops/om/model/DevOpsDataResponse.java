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

    public static String SUCCESS_CODE = "1";
    public static String FAIL_CODE = "0";

    private String code;

    private String message;

    private Object data;

    /**
     * 构造函数.
     *
     * @param message 消息
     */
    public DevOpsDataResponse(String message) {
        this.message = message;
    }

    /**
     * 创建成功响应对象.
     *
     * @param message 消息
     * @return 结果
     */
    public static DevOpsDataResponse ok(String message) {
        DevOpsDataResponse response = new DevOpsDataResponse(message);
        response.setCode(SUCCESS_CODE);
        return response;
    }

    /**
     * 创建失败响应对象.
     *
     * @param message 消息
     * @return 结果
     */
    public static DevOpsDataResponse fail(String message) {
        DevOpsDataResponse response = new DevOpsDataResponse(message);
        response.setCode(FAIL_CODE);
        return response;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
