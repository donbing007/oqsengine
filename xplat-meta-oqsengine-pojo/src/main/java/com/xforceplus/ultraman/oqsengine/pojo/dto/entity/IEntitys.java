package com.xforceplus.ultraman.oqsengine.pojo.dto.entity;

/**
 * 帮助工具.
 *
 * @author dongbin
 * @version 0.1 2022/1/5 11:35
 * @since 1.8
 */
public class IEntitys {

    /**
     * 资源表示.
     *
     * @param id 数据标识.
     * @return 资源表示.
     */
    public static String resource(long id) {
        return "entity.".concat(Long.toString(id));
    }
}
