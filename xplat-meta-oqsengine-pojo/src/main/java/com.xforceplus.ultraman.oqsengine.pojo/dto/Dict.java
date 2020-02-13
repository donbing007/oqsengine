package com.xforceplus.ultraman.oqsengine.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 字典对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
@Data
public class Dict implements Serializable {
    /**
     * 名称
     */
    private String name;

    /**
     * 编码
     */
    private String code;

    /**
     * 字典详情
     */
    private List<DictDetail> dictDetails;
}