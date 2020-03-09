package com.xforceplus.ultraman.oqsengine.storage.index;

import com.xforceplus.ultraman.oqsengine.pojo.dto.conditions.Conditions;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityClass;
import com.xforceplus.ultraman.oqsengine.pojo.dto.EntityRef;
import com.xforceplus.ultraman.oqsengine.pojo.dto.entity.IEntityValue;
import com.xforceplus.ultraman.oqsengine.pojo.dto.sort.Sort;
import com.xforceplus.ultraman.oqsengine.pojo.dto.values.IValue;
import com.xforceplus.ultraman.oqsengine.pojo.page.Page;
import com.xforceplus.ultraman.oqsengine.storage.Storage;

import java.sql.SQLException;
import java.util.Collection;

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
    Collection<EntityRef> select(Conditions conditions, IEntityClass entityClass, Sort sort, Page page)
        throws SQLException;

    /**
     * 替换索引中某些属性的值.
     *
     * @param attribute 需要更新的属性值.
     */
    void replaceAttribute(IEntityValue attribute) throws SQLException;

}
