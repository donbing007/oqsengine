package com.xforceplus.ultraman.oqsengine.storage.index;

import com.xforceplus.ultraman.oqsengine.core.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.Storage;

import java.util.List;

/**
 * 索引储存实现.
 *
 * @author dongbin
 * @version 0.1 2020/2/13 19:44
 * @since 1.8
 */
public interface IndexStorage extends Storage {

    /**
     * 条件搜索Entity指针.
     *
     * @param conditions 搜索条件.
     * @return
     */
     List<EntityRef> select(Conditions conditions, Page page);

}
