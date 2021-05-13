package com.xforceplus.ultraman.oqsengine.idgenerator.common.entity;

/**
 * 项目名称: 票易通
 * JDK 版本: JDK1.8
 * 说明: 表示ID的获取结果，带有状态信息编码
 * 作者(@author): liwei
 * 创建时间: 5/7/21 3:10 PM
 */
public class IDResult {

    private ResultCode code;
    private String id;

    public IDResult(ResultCode code,String id) {
        this.code = code;
        this.id = id;
    }

    public ResultCode getCode() {
        return code;
    }

    public void setCode(ResultCode code) {
        this.code = code;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
