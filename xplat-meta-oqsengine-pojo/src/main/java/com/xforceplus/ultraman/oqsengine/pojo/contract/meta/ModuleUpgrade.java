package com.xforceplus.ultraman.oqsengine.pojo.contract.meta;

import com.xforceplus.ultraman.oqsengine.pojo.dto.Bo;

import java.io.Serializable;
import java.util.List;

/**
 * 模块同步对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class ModuleUpgrade implements Serializable {
    private Long id;
    private String version;
    private List<Bo> bos;
}