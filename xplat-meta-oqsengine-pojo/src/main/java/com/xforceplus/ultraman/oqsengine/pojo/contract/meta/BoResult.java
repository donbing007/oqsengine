package com.xforceplus.ultraman.oqsengine.pojo.contract.meta;

import com.xforceplus.ultraman.oqsengine.pojo.dto.Api;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.Field;

import java.io.Serializable;
import java.util.List;

/**
 * 业务对象结果对象.
 * @version 0.1 2020/2/13 15:30
 * @author wangzheng
 * @since 1.8
 */
public class BoResult implements Serializable {
    private Long id;
    private String code;
    private EntityClass entityClass;
    private List<Field> fields;
    private List<Api> apis;
}