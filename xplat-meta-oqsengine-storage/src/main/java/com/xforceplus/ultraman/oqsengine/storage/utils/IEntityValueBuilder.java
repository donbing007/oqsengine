package com.xforceplus.ultraman.oqsengine.storage.utils;

import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityField;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;

import java.sql.SQLException;
import java.util.Map;

/**
 * entityValue构造.
 *
 * @param <SOURCE> 数据源类型.
 * @author dongbin
 * @version 0.1 2020/11/5 15:36
 * @since 1.8
 */
public interface IEntityValueBuilder<SOURCE> {

    /**
     * 构造一个新的IEntityValue.
     *
     * @param id         所属于的entity实例id.
     * @param fieldTable 字段速查表.
     * @param source     数据源.
     * @return 实例.
     * @throws SQLException 产生异常.
     */
    IEntityValue build(long id, Map<String, IEntityField> fieldTable, SOURCE source) throws SQLException;
}
