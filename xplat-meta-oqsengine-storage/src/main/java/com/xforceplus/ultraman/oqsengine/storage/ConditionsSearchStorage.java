package com.xforceplus.ultraman.oqsengine.storage;

import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.storage.pojo.search.SearchConfig;
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
     * @param config 搜索配置.
     * @param entityClasses 需要的元信息.
     * @return 搜索结果.
     * @throws SQLException 搜索发生异常.
     */
    Collection<EntityRef> search(SearchConfig config, IEntityClass ...entityClasses) throws SQLException;
}
