package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.CrossSearchConfig;
import java.sql.SQLException;
import java.util.Collection;

/**
 * 支持条件全文搜索.
 *
 * @author dongbin
 * @version 0.1 2021/05/13 17:10
 * @since 1.8
 */
public interface ConditionsSearchStorage {

    /**
     * 全文搜索.
     *
     * @param code 目标字段code.
     * @param text 搜索目标内容.
     * @param config 搜索配置.
     * @return 搜索结果.
     * @throws SQLException 搜索发生异常.
     */
    Collection<EntityRef> search(String code, String text, CrossSearchConfig config) throws SQLException;
}
